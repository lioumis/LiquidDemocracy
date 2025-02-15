package gr.upatras.ceid.ld.topic.service;

import gr.upatras.ceid.ld.common.auditlog.service.LoggingService;
import gr.upatras.ceid.ld.common.enums.Action;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.topic.dto.TopicDto;
import gr.upatras.ceid.ld.topic.entity.TopicEntity;
import gr.upatras.ceid.ld.topic.repository.TopicRepository;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    private final TopicRepository topicRepository;

    private final LoggingService loggingService;

    private final UserRepository userRepository;

    public TopicService(TopicRepository topicRepository, LoggingService loggingService, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.loggingService = loggingService;
        this.userRepository = userRepository;
    }

    public List<TopicDto> getTopics() {
        List<TopicEntity> topics = topicRepository.findAll();

        return topics.stream().map(topic ->
                new TopicDto(topic.getId().intValue(), topic.getTitle())).toList();
    }

    public void createTopic(String username, String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Το θέμα είναι κενό");
        }

        if (title.length() > 255) {
            throw new ValidationException("Ο τίτλος είναι υπερβολικά μεγάλος");
        }

        if (topicRepository.existsByTitleIgnoreCase(title)) {
            throw new ValidationException("Το θέμα υπάρχει ήδη");
        }

        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if (byUsername.isEmpty()) {
            throw new ValidationException("Ο χρήστης δεν βρέθηκε");
        }

        TopicEntity topic = new TopicEntity(title);
        topicRepository.save(topic);
        loggingService.log(byUsername.get(), Action.DIRECT_VOTE, "Ο χρήστης " + username + " δημιούργησε νέο θέμα με τίτλο " + title + ".");
    }

}
