package com.brokerx.application.events;

import com.brokerx.application.MatchOrders;
import java.util.List;
import java.util.UUID;

public record OrderMatchedEvent(
    UUID orderId, String status, long remainingQty, List<MatchOrders.ExecutionEvent> executions) {}
