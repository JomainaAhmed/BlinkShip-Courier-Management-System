package com.capg.delivery.producer;

import com.capg.delivery.config.RabbitMQConfig;
import com.capg.delivery.dto.TrackingMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TrackingProducer {

    private final RabbitTemplate rabbitTemplate;

    public TrackingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTrackingEvent(TrackingMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
    }
}
