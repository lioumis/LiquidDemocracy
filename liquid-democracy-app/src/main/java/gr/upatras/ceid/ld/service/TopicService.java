package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.TopicDto;
import gr.upatras.ceid.ld.entity.TopicEntity;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.TopicRepository;
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
        if (topicRepository.existsByTitleIgnoreCase(title)) {
            throw new ValidationException("Το θέμα υπάρχει ήδη");
        }

        TopicEntity topic = new TopicEntity(title);
        topicRepository.save(topic);
    }

}
