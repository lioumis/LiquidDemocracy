package gr.upatras.ceid.ld.topic.service;

import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.topic.dto.TopicDto;
import gr.upatras.ceid.ld.topic.entity.TopicEntity;
import gr.upatras.ceid.ld.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {
    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<TopicDto> getTopics() {
        List<TopicEntity> topics = topicRepository.findAll();

        return topics.stream().map(topic ->
                new TopicDto(topic.getId().intValue(), topic.getTitle())).toList();
    }

    public void createTopic(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Το θέμα είναι κενό");
        }

        if (title.length() > 255) {
            throw new ValidationException("Ο τίτλος είναι υπερβολικά μεγάλος");
        }

        if (topicRepository.existsByTitleIgnoreCase(title)) {
            throw new ValidationException("Το θέμα υπάρχει ήδη");
        }

        TopicEntity topic = new TopicEntity(title);
        topicRepository.save(topic);
    }

}
