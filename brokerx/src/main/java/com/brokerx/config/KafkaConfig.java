package com.brokerx.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

  @Bean
  public NewTopic topicAccountCreated() {
    return TopicBuilder.name("compte.cree").partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic topicDepositValidated() {
    return TopicBuilder.name("depot.valide").partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic topicOrderPlaced() {
    return TopicBuilder.name("ordre.place").partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic topicExecutionCreated() {
    return TopicBuilder.name("execution.cree").partitions(1).replicas(1).build();
  }
}
