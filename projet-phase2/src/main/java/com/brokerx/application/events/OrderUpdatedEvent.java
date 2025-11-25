package com.brokerx.application.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderUpdatedEvent(
    UUID orderId, UUID accountId, String status, int version, Long qty, BigDecimal limitPrice) {}
