package com.brokerx.application.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(
    UUID orderId,
    UUID accountId,
    String side,
    String type,
    String symbol,
    long qty,
    BigDecimal limitPrice) {}
