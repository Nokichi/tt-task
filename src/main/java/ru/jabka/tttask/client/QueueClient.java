package ru.jabka.tttask.client;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.jabka.tttask.configuration.RabbitConfigurationProperties;
import ru.jabka.tttask.model.history.TaskHistory;

@Component
@RequiredArgsConstructor
public class QueueClient {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfigurationProperties properties;

    public void sendTaskHistory(final TaskHistory taskHistory) {
        rabbitTemplate.convertAndSend(properties.getExchange(), properties.getQueueTasks(), taskHistory);
    }
}