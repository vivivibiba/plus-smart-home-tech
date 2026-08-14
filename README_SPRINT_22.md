# Sprint 22: Order, Payment, Delivery and API Gateway

The project contains the three new commerce services required by the task:

- `commerce/order` — order lifecycle, prices, assembly and returns;
- `commerce/payment` — product cost, VAT, payment persistence and callbacks;
- `commerce/delivery` — delivery persistence, cost and status callbacks.

The existing `warehouse` service now supports assembly, shipment and returns. Shared REST contracts and DTOs are located in `commerce/interaction-api`.

`infra/api-gateway` routes requests through Eureka using `lb://` URIs. Gateway routes and all service database settings are stored in `infra/config-repo/commerce`.

## Local infrastructure

Start PostgreSQL and the existing Kafka infrastructure:

```bash
docker compose up -d
```

Start applications in this order:

1. `infra/discovery-server` (port `8761`);
2. `infra/config-server` (port `8888`);
3. commerce services (`shopping-store`, `shopping-cart`, `warehouse`, `order`, `payment`, `delivery`);
4. `infra/api-gateway` (port `8080`).

Commerce services use random ports and register in Eureka. Gateway examples:

- `POST http://localhost:8080/delivery/cost`
- `POST http://localhost:8080/payment/productCost`
- `GET http://localhost:8080/order?username=user`

The gateway adds `/api/v1` before forwarding each request to the target service.

## Verification

Run the structural checks and the complete Maven build from the repository root:

```bash
./verify-sprint22.sh
```

After PostgreSQL, Eureka, Config Server, all commerce services and API Gateway are running, run the end-to-end test:

```bash
./smoke-test-sprint22.sh
```

The smoke test uses only the public Gateway routes. It creates isolated test data with random identifiers and verifies the product, warehouse, cart, order, payment and delivery flow.
