package gr.upatras.ceid.ld.topic.controller;

import gr.upatras.ceid.ld.common.authorization.AuthorizationService;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.topic.dto.TopicDto;
import gr.upatras.ceid.ld.topic.service.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/topics")
public class TopicController {
    private static final String AUTHORIZATION_ERROR_MESSAGE = "Δεν έχετε άδεια να εκτελέσετε αυτήν την ενέργεια";
    private static final String ERROR_KEYWORD = "error";
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();
    private static final Set<Role> TOPIC_CREATION_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.addAll(List.of(Role.values()));

        TOPIC_CREATION_ROLES.add(Role.SYSTEM_ADMIN);
    }

    private final TopicService topicService;

    private final AuthorizationService authorizationService;

    public TopicController(TopicService topicService, AuthorizationService authorizationService) {
        this.topicService = topicService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/getTopics")
    public ResponseEntity<Object> getTopics() {
        try {
            List<TopicDto> topics = topicService.getTopics();
            return ResponseEntity.status(HttpStatus.OK).body(topics);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/createTopic")
    public ResponseEntity<Map<String, String>> createTopic(@RequestBody TopicDto topicDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, TOPIC_CREATION_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            topicService.createTopic(authorizedUsername, topicDto.name());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Το θέμα δημιουργήθηκε με επιτυχία");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
