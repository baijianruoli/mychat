package com.zut.lpf;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DockerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerApplication.class, args);
    }
 /*   @Bean
    TopicExchange exchange() {

        return new TopicExchange("mychat-topic");

    }
    @Bean
    FanoutExchange fanoutExchange() {

        return new FanoutExchange("mychat-fanout");

    }*/

}
