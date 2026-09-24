# Payment Approval Proxy Extension

## Overview

The **Payment Approval Proxy Extension** (`PaymentApprovalExtension`) exposes three management API
endpoints on the EDC connector so the **provider UI can list, confirm, and reject paid contract
agreements** without requiring direct access to the contract service.

This is the companion EDC-side integration for the `PENDING_PAYMENT` flow introduced in contract
service v2.9.0. See the [contract service documentation](../../contract/docs/PAYMENT_APPROVAL.md)
for the full flow description.

## Why this extension exists

When a consumer requests a paid offering the EDC negotiation is held at **`VERIFIED`** by the
`SignContractNegotiationPendingGuard` until the contract service releases it. The `PENDING_PAYMENT`
status lives entirely inside the contract service — EDC has no concept of it. Without this
extension the UI would need a direct route to the contract service, which requires new Tier-1 proxy
rules and a separate API key. This extension proxies the three relevant contract-service endpoints
through the EDC management API, which the UI already calls.

## Endpoints

All endpoints are served on the EDC **management API port** (default `29193`) under the
`/management/v3/payment` path, consistent with EDC's standard management API versioning.
Authentication is whatever the UI already uses for the management API.

### List agreements awaiting payment confirmation

```
GET /management/v3/payment/pending
```

Proxies `GET {contractmanager.url}/agreements/pending-payment`.

Returns the list of `ContractAgreementTO` objects currently in `PENDING_PAYMENT` status. The UI
can compute the deadline as `pendingPaymentSince + expirationDays` (configured on the contract
service side, default 7 days).

**Example response:**
```json
[
  {
    "contractAgreementId": "0f0e...uuid",
    "assetId": "5900b594-...",
    "consumerId": "did:web:consumer...",
    "status": "PENDING_PAYMENT",
    "pendingPaymentSince": "2026-06-10T09:15:00"
  }
]
```

### Confirm payment → sign and release the negotiation

```
POST /management/v3/payment/{contractAgreementId}/confirm
```

Proxies `POST {contractmanager.url}/agreements/{contractAgreementId}/payment/confirm`.

Use after the operator has verified out-of-band that the consumer has paid. The contract service
signs the agreement and notifies the connector, which finalises the negotiation (status becomes
`CREATED`, then `FINALIZED`).

### Reject payment → terminate the negotiation

```
POST /management/v3/payment/{contractAgreementId}/reject
```

Proxies `POST {contractmanager.url}/agreements/{contractAgreementId}/payment/reject`.

Use when the payment was not received or refused. The negotiation is terminated (status becomes
`TERMINATED`).

### Error responses

Errors from the contract service are forwarded as-is (HTTP status code + body). Common codes:

| HTTP | When |
|------|------|
| `404` | No agreement with that id. |
| `400` | The agreement is not in `PENDING_PAYMENT` (already confirmed, rejected, or expired). |
| `500` | The contract service could not be reached. |

## Configuration

The extension reuses the `contractmanager.*` settings already required by
`ContractSignCallbackEndpointExtension`. No additional configuration is needed.

| Setting | Description |
|---------|-------------|
| `contractmanager.extension.enabled` | Must be `true` for the extension to register its endpoints. Only enable on the **provider** connector. |
| `contractmanager.url` | Base URL of the contract service (e.g. `http://contract:8080/contract/v1`). |
| `contractmanager.apikey` | API key sent as `x-api-key` to the contract service. |

## Suggested UI workflow

1. Poll `GET /payment/pending` to display the queue of paid agreements awaiting confirmation.
2. Show the consumer, asset, and computed deadline (`pendingPaymentSince + expirationDays`).
3. The operator verifies payment out-of-band, then:
   - **Confirm** → `POST /payment/{id}/confirm`. The row disappears; the negotiation finalises.
   - **Reject** → `POST /payment/{id}/reject`. The negotiation is terminated.
4. Items not actioned within `expirationDays` are auto-rejected by the contract service; the UI
   should tolerate a row disappearing on its own.

## Sequence diagram

```
UI                    EDC (provider)               Contract Service
 |                         |                               |
 | GET /payment/pending    |                               |
 |------------------------>|                               |
 |                         | GET .../pending-payment       |
 |                         |------------------------------>|
 |                         |        [PENDING_PAYMENT list] |
 |        [list of items]  |<------------------------------|
 |<------------------------|                               |
 |                         |                               |
 | POST /payment/{id}/confirm                              |
 |------------------------>|                               |
 |                         | POST .../payment/confirm      |
 |                         |------------------------------>|
 |                         |   signs + notifies connector  |
 |                         |<------------------------------|
 |       [updated TO]      |                               |
 |<------------------------|                               |
```
