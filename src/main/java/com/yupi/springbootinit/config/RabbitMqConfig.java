package com.yupi.springbootinit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    public static final String BI_DLX_QUEUE_NAME = "bi_dlx_queue";
    public static final String BI_DLX_EXCHANGE_NAME = "bi_dlx_exchange";
    public static final String BI_DLX_ROUTING_KEY = "bi_dlx_routing_key";

    public static final String BI_QUEUE_NAME = "bi_queue";
    public static final String BI_EXCHANGE_NAME = "bi_exchange";
    public static final String BI_ROUTING_KEY = "bi_routing_key";

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(BI_DLX_EXCHANGE_NAME);
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue(BI_DLX_QUEUE_NAME, true);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(BI_DLX_ROUTING_KEY);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(BI_EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", BI_DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", BI_DLX_ROUTING_KEY);
        return new Queue(BI_QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(BI_ROUTING_KEY);
    }
}
