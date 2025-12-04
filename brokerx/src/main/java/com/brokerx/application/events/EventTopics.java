package com.brokerx.application.events;

public final class EventTopics {
  private EventTopics() {}

  public static final String ACCOUNT_CREATED = "compte.cree";
  public static final String DEPOSIT_VALIDATED = "depot.valide";
  public static final String ORDER_PLACED = "ordre.place";
  public static final String EXECUTION_CREATED = "execution.cree";
}
