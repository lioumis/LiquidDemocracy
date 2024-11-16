package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.service.AuthorizationService;
import gr.upatras.ceid.ld.service.DelegationService;
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
@RequestMapping("/delegations")
public class DelegationController {
    private static final Set<String> ALLOWED_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.add("ADMIN");
        ALLOWED_ROLES.add("USER");
    }

    private final DelegationService delegationService;

    private final AuthorizationService authorizationService;

    public DelegationController(DelegationService delegationService, AuthorizationService authorizationService) {
        this.delegationService = delegationService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/delegate")
    public ResponseEntity<String> delegateVote(@RequestParam("delegatorId") Long delegatorId, @RequestParam("delegateId") Long delegateId,
                                               @RequestParam("topicId") Long topicId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long userId = authorizationService.isUserAuthorized(username, ALLOWED_ROLES);

            if (!delegatorId.equals(userId)) {
                throw new AuthorizationException("You do not have permission to delegate this topic");
            }

            delegationService.delegateVote(delegatorId, delegateId, topicId);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Vote delegated successfully!");
    }

    @PostMapping("/removeDelegation")
    public ResponseEntity<String> removeDelegation(@RequestParam("delegatorId") Long delegatorId, @RequestParam("topicId") Long topicId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long userId = authorizationService.isUserAuthorized(username, ALLOWED_ROLES);

            if (!delegatorId.equals(userId)) {
                throw new AuthorizationException("You do not have permission to remove this delegation");
            }
            delegationService.removeDelegation(delegatorId, topicId);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Vote delegation removed successfully!");
    }
}
