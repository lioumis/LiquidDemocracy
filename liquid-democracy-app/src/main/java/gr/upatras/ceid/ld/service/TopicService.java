package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.TopicEntity;
import gr.upatras.ceid.ld.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TopicService {
    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public Map<Long, String> getTopics() {
        List<TopicEntity> topics = topicRepository.findAll();

        Map<Long, String> topicMap = new HashMap<>();
        topics.forEach(topic -> topicMap.put(topic.getId(), topic.getTitle()));

        return topicMap;
    }

}
