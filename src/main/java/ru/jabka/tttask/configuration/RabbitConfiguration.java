package ru.jabka.tttask.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

    private final RabbitConfigurationProperties configurationProperties;

    @Bean
    public Queue tasksHistoryQueue() {
        return new Queue(configurationProperties.getQueueTasks(), true);
    }

    @Bean
    public DirectExchange historyExchange() {
        return new DirectExchange(configurationProperties.getExchange());
    }

    @Bean
    public Binding binding(final Queue queue, final DirectExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .withQueueName();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}