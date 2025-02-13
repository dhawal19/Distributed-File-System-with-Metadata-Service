package com.dhawal.publisher;

import com.dhawal.model.OutboxEvent;
import com.dhawal.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public OutboxPublisher(OutboxEventRepository outboxEventRepository, RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByPublishedAtIsNullOrStatusEquals("Failed");
        for(OutboxEvent event : events) {
            processEvent(event);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processEvent(OutboxEvent event) {
        try{
            rabbitTemplate.convertAndSend("outbox-events", event.getEventType(), event);
            log.debug("Published event: " + event.getEventType() + " with id: " + event.getId());
            event.setPublishedAt(LocalDateTime.now());
            event.setStatus("Successful");
        }
        catch(Exception e){
            String errorMessage = "Error publishing event ID " + event.getId() + ": " + e.getMessage();
            log.error(errorMessage,e);
            event.setStatus("Failed");
        }
        finally{
            outboxEventRepository.save(event);
        }
    }
}
