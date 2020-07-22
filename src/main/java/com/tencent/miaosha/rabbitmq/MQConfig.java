package com.tencent.miaosha.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";
    public static final String MIAOSHA_QUEUE = "miaosha.queue";

    @Bean
    public Queue queue(){
        return new Queue(MIAOSHA_QUEUE, true);
    }
}
