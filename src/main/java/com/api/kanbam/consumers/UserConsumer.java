package com.api.kanbam.consumers;

import com.api.kanbam.domain.dtos.event.UserCreatedEventDTO;
import com.api.kanbam.domain.entities.UserLocal;
import com.api.kanbam.domain.repositories.events.UserLocalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserConsumer.class);
    private final UserLocalRepository userLocalRepository;

//    @KafkaListener(topics = "${app.kafka.topics.user-created}", groupId = "${spring.kafka.consumer.group-id}")
@KafkaListener(
        topics = "${app.kafka.topics.user-created:user-created-topic}",
        groupId = "${spring.kafka.consumer.group-id:kanban-service-group-v2}"
)
    @Transactional
    public void consumeUserCreated(UserCreatedEventDTO event) {
        log.info("Evento recebido do Kafka para criação de usuário no Kanban: ID={}, Name={}", event.id(), event.name());

        if (userLocalRepository.existsById(event.id())) {
            log.warn("Usuário com ID {} já existe na base do Kanban. Ignorando evento duplicado.", event.id());
            return;
        }

        UserLocal user = new UserLocal(event.id(), event.name(), event.email());
        userLocalRepository.save(user);

        log.info("Usuário {} sincronizado com sucesso na base de dados do Kanban!", user.getId());
    }
}
