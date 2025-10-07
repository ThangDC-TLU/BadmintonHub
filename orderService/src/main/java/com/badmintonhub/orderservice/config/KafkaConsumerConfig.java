package com.badmintonhub.orderservice.config;

import com.badmintonhub.orderservice.dto.event.consumer.PaymentCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }
//
//    // InventoryFailedEvent
//    @Bean
//    public ConsumerFactory<String, InventoryFailedEvent> inventoryFailedConsumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(
//                baseProps("order-service-inventory-failed"),
//                new StringDeserializer(),
//                new JsonDeserializer<>(InventoryFailedEvent.class, false)
//        );
//    }

//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> inventoryFailedKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, InventoryFailedEvent> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(inventoryFailedConsumerFactory());
//        return factory;
//    }

    // PaymentCompletedEvent
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentCompletedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                baseProps("order-service-payment-completed"),
                new StringDeserializer(),
                new JsonDeserializer<>(PaymentCompletedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentCompletedConsumerFactory());
        return factory;
    }

//    // PaymentFailedEvent
//    @Bean
//    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(
//                baseProps("order-service-payment-failed"),
//                new StringDeserializer(),
//                new JsonDeserializer<>(PaymentFailedEvent.class, false)
//        );
//    }

//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(paymentFailedConsumerFactory());
//        return factory;
//    }
}
