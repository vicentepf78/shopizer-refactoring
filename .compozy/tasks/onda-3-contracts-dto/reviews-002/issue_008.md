---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core-modules/src/main/java/com/salesmanager/core/modules/integration/payment/dto/PaymentRequestContext.java
line: 14
severity: medium
author: claude-code
provider_ref:
---

# Issue 008: Integration context DTOs lack @JsonIgnoreProperties

## Review Comment

`PaymentRequestContext`, `PaymentCaptureContext`, `PaymentRefundContext`, `ShippingQuoteRequestContext`, and nested DTOs have no `@JsonIgnoreProperties(ignoreUnknown = true)`. Default Jackson rejects unknown properties — deserializing `{"customerId":1,"unknownField":"x"}` into `PaymentRequestContext` throws `UnrecognizedPropertyException`.

These payloads are intended for cross-process integration-service extraction (Onda 5); without `ignoreUnknown`, a producer adding a field breaks older consumers — the same gap fixed for snapshot DTOs in reviews-001 issue_008.

**Suggested fix:** add the annotation to all integration DTO types and extend `PaymentRequestContextJacksonTest` / `ShippingQuoteRequestContextJacksonTest` with unknown-field cases.

## Triage

- Decision: `valid`
- Root cause: `PaymentRequestContext` had no `@JsonIgnoreProperties(ignoreUnknown = true)`. Default Jackson rejects unknown properties, so a producer adding a field breaks older consumers deserializing this integration payload.
- Fix: Added `@JsonIgnoreProperties(ignoreUnknown = true)` to `PaymentRequestContext` and extended `PaymentRequestContextJacksonTest` with an `ignoresUnknownFieldsDuringDeserialization` case matching the cited `{"customerId":1,"unknownField":"x"}` scenario.
- Scope note: Batch metadata scoped this run to `PaymentRequestContext.java` only. Other integration context DTOs named in the review comment (`PaymentCaptureContext`, `PaymentRefundContext`, `ShippingQuoteRequestContext`, nested types) are out of scope for this batch.
- Verification: `./mvnw -pl sm-core-modules -am verify -DfailIfNoTests=false` — BUILD SUCCESS, 82 tests (78 + 4), 0 failures.
