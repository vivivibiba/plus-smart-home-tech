#!/usr/bin/env bash

set -u

cd "$(dirname "$0")"

errors=0

ok() {
  printf 'OK      %s\n' "$1"
}

fail() {
  printf 'FAIL    %s\n' "$1"
  errors=$((errors + 1))
}

check_file() {
  if [[ -f "$1" ]]; then
    ok "$1"
  else
    fail "missing file: $1"
  fi
}

check_text() {
  local file="$1"
  local pattern="$2"
  local label="$3"
  if [[ -f "$file" ]] && grep -Fq -- "$pattern" "$file"; then
    ok "$label"
  else
    fail "$label"
  fi
}

printf '=== Branch ===\n'
branch="$(git branch --show-current 2>/dev/null || true)"
if [[ "$branch" == "9-gateway-microservices" ]]; then
  ok "branch 9-gateway-microservices"
else
  fail "current branch is '$branch', expected 9-gateway-microservices"
fi

if git show-ref --verify --quiet refs/heads/main \
  && git merge-base --is-ancestor main HEAD; then
  ok "branch contains main"
else
  fail "branch is not based on local main"
fi

printf '\n=== Required modules and configuration ===\n'
required_files=(
  smoke-test-sprint22.sh
  commerce/interaction-api/pom.xml
  commerce/shopping-store/pom.xml
  commerce/shopping-cart/pom.xml
  commerce/warehouse/pom.xml
  commerce/order/pom.xml
  commerce/payment/pom.xml
  commerce/delivery/pom.xml
  infra/config-server/pom.xml
  infra/discovery-server/pom.xml
  infra/api-gateway/pom.xml
  infra/config-repo/commerce/order.yml
  infra/config-repo/commerce/payment.yml
  infra/config-repo/commerce/delivery.yml
  infra/config-repo/commerce/api-gateway.yml
)
for file in "${required_files[@]}"; do
  check_file "$file"
done

if bash -n smoke-test-sprint22.sh; then
  ok 'smoke-test-sprint22.sh syntax'
else
  fail 'smoke-test-sprint22.sh syntax'
fi

check_text commerce/pom.xml '<module>order</module>' 'commerce includes order'
check_text commerce/pom.xml '<module>payment</module>' 'commerce includes payment'
check_text commerce/pom.xml '<module>delivery</module>' 'commerce includes delivery'
check_text infra/pom.xml '<module>api-gateway</module>' 'infra includes api-gateway'

check_text infra/discovery-server/src/main/resources/application.yml 'port: 8761' 'Eureka uses port 8761'
check_text commerce/order/src/main/resources/application.yaml 'name: order' 'order Eureka name'
check_text commerce/payment/src/main/resources/application.yaml 'name: payment' 'payment Eureka name'
check_text commerce/delivery/src/main/resources/application.yaml 'name: delivery' 'delivery Eureka name'
check_text infra/api-gateway/src/main/resources/application.yml 'name: api-gateway' 'Gateway application name'
for service in order payment delivery warehouse shopping-store shopping-cart; do
  check_text "commerce/$service/src/main/resources/application.yaml" \
    'configserver:http://localhost:8888' "$service loads external configuration"
done

printf '\n=== OpenAPI paths from the task ===\n'
order_api=commerce/interaction-api/src/main/java/ru/yandex/practicum/commerce/api/OrderApi.java
for mapping in \
  '@GetMapping("/api/v1/order")' \
  '@PutMapping("/api/v1/order")' \
  '@PostMapping("/api/v1/order/return")' \
  '@PostMapping("/api/v1/order/payment")' \
  '@PostMapping("/api/v1/order/payment/failed")' \
  '@PostMapping("/api/v1/order/delivery")' \
  '@PostMapping("/api/v1/order/delivery/failed")' \
  '@PostMapping("/api/v1/order/completed")' \
  '@PostMapping("/api/v1/order/calculate/total")' \
  '@PostMapping("/api/v1/order/calculate/delivery")' \
  '@PostMapping("/api/v1/order/assembly")' \
  '@PostMapping("/api/v1/order/assembly/failed")'; do
  check_text "$order_api" "$mapping" "order mapping $mapping"
done

payment_api=commerce/interaction-api/src/main/java/ru/yandex/practicum/commerce/api/PaymentApi.java
for mapping in \
  '@PostMapping("/api/v1/payment")' \
  '@PostMapping("/api/v1/payment/totalCost")' \
  '@PostMapping("/api/v1/payment/refund")' \
  '@PostMapping("/api/v1/payment/productCost")' \
  '@PostMapping("/api/v1/payment/failed")'; do
  check_text "$payment_api" "$mapping" "payment mapping $mapping"
done

delivery_api=commerce/interaction-api/src/main/java/ru/yandex/practicum/commerce/api/DeliveryApi.java
for mapping in \
  '@PutMapping("/api/v1/delivery")' \
  '@PostMapping("/api/v1/delivery/successful")' \
  '@PostMapping("/api/v1/delivery/picked")' \
  '@PostMapping("/api/v1/delivery/failed")' \
  '@PostMapping("/api/v1/delivery/cost")'; do
  check_text "$delivery_api" "$mapping" "delivery mapping $mapping"
done

warehouse_api=commerce/interaction-api/src/main/java/ru/yandex/practicum/commerce/api/WarehouseApi.java
for mapping in \
  '@PutMapping("/api/v1/warehouse")' \
  '@PostMapping("/api/v1/warehouse/add")' \
  '@PostMapping("/api/v1/warehouse/check")' \
  '@PostMapping("/api/v1/warehouse/shipped")' \
  '@PostMapping("/api/v1/warehouse/return")' \
  '@PostMapping("/api/v1/warehouse/assembly")' \
  '@GetMapping("/api/v1/warehouse/address")'; do
  check_text "$warehouse_api" "$mapping" "warehouse mapping $mapping"
done

printf '\n=== HTTP error contract ===\n'
check_text commerce/order/src/main/java/ru/yandex/practicum/commerce/order/controller/OrderExceptionHandler.java \
  'HttpStatus.UNAUTHORIZED' 'blank order username returns 401'
check_text commerce/order/src/main/java/ru/yandex/practicum/commerce/order/controller/OrderExceptionHandler.java \
  'ResponseEntity.badRequest()' 'missing order returns 400'
check_text commerce/warehouse/src/main/java/ru/yandex/practicum/commerce/warehouse/controller/WarehouseExceptionHandler.java \
  'ResponseEntity.badRequest()' 'warehouse domain errors return 400'

printf '\n=== Feign clients ===\n'
check_text commerce/order/src/main/java/ru/yandex/practicum/commerce/order/client/DeliveryClient.java '@FeignClient(name = "delivery")' 'order -> delivery'
check_text commerce/order/src/main/java/ru/yandex/practicum/commerce/order/client/PaymentClient.java '@FeignClient(name = "payment")' 'order -> payment'
check_text commerce/order/src/main/java/ru/yandex/practicum/commerce/order/client/WarehouseClient.java '@FeignClient(name = "warehouse")' 'order -> warehouse'
check_text commerce/payment/src/main/java/ru/yandex/practicum/commerce/payment/client/OrderClient.java '@FeignClient(name = "order")' 'payment -> order'
check_text commerce/payment/src/main/java/ru/yandex/practicum/commerce/payment/client/ShoppingStoreClient.java '@FeignClient(name = "shopping-store")' 'payment -> shopping-store'
check_text commerce/delivery/src/main/java/ru/yandex/practicum/commerce/delivery/client/OrderClient.java '@FeignClient(name = "order")' 'delivery -> order'
check_text commerce/delivery/src/main/java/ru/yandex/practicum/commerce/delivery/client/WarehouseClient.java '@FeignClient(name = "warehouse")' 'delivery -> warehouse'

printf '\n=== Gateway and LoadBalancer ===\n'
gateway_config=infra/config-repo/commerce/api-gateway.yml
for service in shopping-store shopping-cart warehouse order payment delivery; do
  check_text "$gateway_config" "uri: lb://$service" "Gateway route $service"
done
check_text "$gateway_config" 'PrefixPath=/api/v1' 'Gateway adds /api/v1'
check_text infra/api-gateway/pom.xml 'spring-cloud-starter-loadbalancer' 'Gateway uses LoadBalancer'

printf '\n=== Database per service ===\n'
for database in shopping-store-db shopping-cart-db warehouse-db order-db payment-db delivery-db; do
  check_text compose.yaml "  $database:" "compose service $database"
done

printf '\n=== Git diff ===\n'
if git diff --check; then
  ok 'git diff --check'
else
  fail 'git diff --check'
fi

if (( errors > 0 )); then
  printf '\nSTATIC CHECK FAILED: %d problem(s)\n' "$errors"
  exit 1
fi

printf '\n=== Maven build and tests ===\n'
if ! command -v mvn >/dev/null 2>&1; then
  fail 'Maven is not installed or is not available in PATH'
  exit 1
fi

if mvn clean verify; then
  ok 'mvn clean verify'
else
  fail 'mvn clean verify'
  exit 1
fi

printf '\nSPRINT 22 STATIC CHECK AND MAVEN TESTS PASSED\n'
