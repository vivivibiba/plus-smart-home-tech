#!/usr/bin/env bash

set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
EUREKA_URL="${EUREKA_URL:-http://localhost:8761/eureka/apps}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'FAIL: required command is missing: %s\n' "$1" >&2
    exit 1
  fi
}

request() {
  curl --fail --silent --show-error --connect-timeout 5 --max-time 30 "$@"
}

json_request() {
  request -H 'Content-Type: application/json' "$@"
}

new_uuid() {
  tr '[:upper:]' '[:lower:]' < /proc/sys/kernel/random/uuid
}

require_command curl
require_command jq

printf '=== Eureka registrations ===\n'
for service in SHOPPING-STORE SHOPPING-CART WAREHOUSE ORDER PAYMENT DELIVERY API-GATEWAY; do
  request "$EUREKA_URL/$service" >/dev/null
  printf 'UP      %s\n' "$service"
done

printf '\n=== Gateway and warehouse ===\n'
WAREHOUSE_ADDRESS="$(request "$GATEWAY_URL/warehouse/address")"
jq -e '.street == "ADDRESS_1" or .street == "ADDRESS_2"' <<<"$WAREHOUSE_ADDRESS" >/dev/null
printf 'OK      warehouse address: %s\n' "$(jq -r '.street' <<<"$WAREHOUSE_ADDRESS")"

PRODUCT_ID="$(new_uuid)"
USERNAME="sprint22-$(new_uuid)"

PRODUCT_REQUEST="$(jq -cn \
  --arg productId "$PRODUCT_ID" \
  '{productId:$productId,productName:"Sprint 22 test product",description:"End-to-end test product",imageSrc:"https://example.invalid/product.png",quantityState:"MANY",productState:"ACTIVE",price:100.00,productCategory:"LIGHTING"}')"
PRODUCT="$(json_request -X PUT "$GATEWAY_URL/shopping-store" --data "$PRODUCT_REQUEST")"
jq -e --arg id "$PRODUCT_ID" '.productId == $id and .price == 100' <<<"$PRODUCT" >/dev/null
printf 'OK      product created: %s\n' "$PRODUCT_ID"

NEW_WAREHOUSE_PRODUCT="$(jq -cn \
  --arg productId "$PRODUCT_ID" \
  '{productId:$productId,fragile:true,dimension:{width:1.0,height:1.0,depth:5.0},weight:5.0}')"
json_request -X PUT "$GATEWAY_URL/warehouse" --data "$NEW_WAREHOUSE_PRODUCT" >/dev/null
json_request -X POST "$GATEWAY_URL/warehouse/add" \
  --data "$(jq -cn --arg productId "$PRODUCT_ID" '{productId:$productId,quantity:10}')" >/dev/null
printf 'OK      warehouse stock added\n'

CART="$(json_request -X PUT "$GATEWAY_URL/shopping-cart?username=$USERNAME" \
  --data "$(jq -cn --arg productId "$PRODUCT_ID" '{($productId):2}')")"
CART_ID="$(jq -er '.shoppingCartId' <<<"$CART")"
jq -e --arg productId "$PRODUCT_ID" '.products[$productId] == 2' <<<"$CART" >/dev/null
printf 'OK      cart created: %s\n' "$CART_ID"

DELIVERY_ADDRESS='{"country":"RU","city":"Moscow","street":"Sprint Street","house":"22","flat":"1"}'
ORDER_REQUEST="$(jq -cn \
  --argjson shoppingCart "$CART" \
  --argjson deliveryAddress "$DELIVERY_ADDRESS" \
  '{shoppingCart:$shoppingCart,deliveryAddress:$deliveryAddress}')"
ORDER="$(json_request -X PUT "$GATEWAY_URL/order" --data "$ORDER_REQUEST")"
ORDER_ID="$(jq -er '.orderId' <<<"$ORDER")"
DELIVERY_ID="$(jq -er '.deliveryId' <<<"$ORDER")"
jq -e '.state == "NEW"' <<<"$ORDER" >/dev/null
printf 'OK      order created: %s\n' "$ORDER_ID"
printf 'OK      delivery created: %s\n' "$DELIVERY_ID"

ORDER="$(json_request -X POST "$GATEWAY_URL/order/calculate/delivery" --data "\"$ORDER_ID\"")"
DELIVERY_PRICE="$(jq -er '.deliveryPrice' <<<"$ORDER")"
jq -e '.deliveryPrice == 20.4 or .deliveryPrice == 27.6' <<<"$ORDER" >/dev/null
printf 'OK      delivery price: %s\n' "$DELIVERY_PRICE"

ORDER="$(json_request -X POST "$GATEWAY_URL/order/calculate/total" --data "\"$ORDER_ID\"")"
jq -e '.productPrice == 200 and (.totalPrice == 240.4 or .totalPrice == 247.6)' <<<"$ORDER" >/dev/null
printf 'OK      product and total price: %s / %s\n' \
  "$(jq -r '.productPrice' <<<"$ORDER")" "$(jq -r '.totalPrice' <<<"$ORDER")"

ORDER="$(json_request -X POST "$GATEWAY_URL/order/assembly" --data "\"$ORDER_ID\"")"
jq -e '.state == "ASSEMBLED" and .deliveryWeight == 10 and .deliveryVolume == 10 and .fragile == true' \
  <<<"$ORDER" >/dev/null
printf 'OK      order assembled\n'

PAYMENT="$(json_request -X POST "$GATEWAY_URL/payment" --data "$ORDER")"
PAYMENT_ID="$(jq -er '.paymentId' <<<"$PAYMENT")"
jq -e '.totalPayment == 240.4 or .totalPayment == 247.6' <<<"$PAYMENT" >/dev/null
printf 'OK      payment created: %s\n' "$PAYMENT_ID"

json_request -X POST "$GATEWAY_URL/payment/refund" --data "\"$PAYMENT_ID\"" >/dev/null
ORDER="$(request "$GATEWAY_URL/order?username=$USERNAME" | jq -ce --arg id "$ORDER_ID" '.[] | select(.orderId == $id)')"
jq -e '.state == "PAID"' <<<"$ORDER" >/dev/null
printf 'OK      payment callback changed order to PAID\n'

json_request -X POST "$GATEWAY_URL/delivery/picked" --data "\"$DELIVERY_ID\"" >/dev/null
json_request -X POST "$GATEWAY_URL/delivery/successful" --data "\"$DELIVERY_ID\"" >/dev/null
ORDER="$(request "$GATEWAY_URL/order?username=$USERNAME" | jq -ce --arg id "$ORDER_ID" '.[] | select(.orderId == $id)')"
jq -e '.state == "DELIVERED"' <<<"$ORDER" >/dev/null
printf 'OK      delivery callback changed order to DELIVERED\n'

ORDER="$(json_request -X POST "$GATEWAY_URL/order/completed" --data "\"$ORDER_ID\"")"
jq -e '.state == "COMPLETED"' <<<"$ORDER" >/dev/null
printf 'OK      order completed\n'

printf '\nSPRINT 22 END-TO-END SMOKE TEST PASSED\n'
