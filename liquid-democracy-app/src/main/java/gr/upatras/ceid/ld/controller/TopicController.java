package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.service.AuthorizationService;
import gr.upatras.ceid.ld.service.TopicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/topics")
public class TopicController {
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.addAll(List.of(Role.values()));
    }

    private final TopicService topicService;

    private final AuthorizationService authorizationService;

    public TopicController(TopicService topicService, AuthorizationService authorizationService) {
        this.topicService = topicService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/getTopics")
    public ResponseEntity<Object> getTopics(@RequestParam("username") String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            Map<Long, String> topics = topicService.getTopics();
            return ResponseEntity.status(HttpStatus.OK).body(topics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
