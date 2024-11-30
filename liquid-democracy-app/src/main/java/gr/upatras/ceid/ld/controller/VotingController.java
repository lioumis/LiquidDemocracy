package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.service.AuthorizationService;
import gr.upatras.ceid.ld.service.VotingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/votings")
public class VotingController {
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.add(Role.VOTER);
        ALLOWED_ROLES.add(Role.REPRESENTATIVE);
    }

    private final VotingService votingService;

    private final AuthorizationService authorizationService;

    public VotingController(VotingService votingService, AuthorizationService authorizationService) {
        this.votingService = votingService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/vote")
    public ResponseEntity<String> castVote(@RequestParam("voterId") Long voterId, @RequestParam("topicId") Long topicId, @RequestParam("voteChoice") String voteChoice) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long userId = authorizationService.isUserAuthorized(username, ALLOWED_ROLES);

            if (!voterId.equals(userId)) {
                throw new AuthorizationException("You do not have permission to vote");
            }

            votingService.castVote(voterId, topicId, voteChoice);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Vote cast successfully!");
    }

}
