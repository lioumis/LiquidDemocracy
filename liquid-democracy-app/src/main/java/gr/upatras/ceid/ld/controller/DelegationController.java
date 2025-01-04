package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.dto.DelegationDto;
import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.service.AuthorizationService;
import gr.upatras.ceid.ld.service.DelegationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/delegations")
public class DelegationController {
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.add(Role.VOTER);
        ALLOWED_ROLES.add(Role.REPRESENTATIVE); //TODO: Secure the services. Only for delegators?
    }

    private final DelegationService delegationService;

    private final AuthorizationService authorizationService;

    public DelegationController(DelegationService delegationService, AuthorizationService authorizationService) {
        this.delegationService = delegationService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/delegate")
    public ResponseEntity<String> delegateVote(@RequestParam("delegator") String delegator, @RequestParam("delegateName") String delegateName,
                                               @RequestParam("delegateSurname") String delegateSurname, @RequestParam("topicId") Long topicId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!delegator.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }


            delegationService.delegateVote(delegator, delegateName, delegateSurname, topicId);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Vote delegated successfully!");
    }

    @PostMapping("/removeDelegation") //TODO: Should not be possible. Clarify and remove
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

    @GetMapping("/getDelegations")
    public ResponseEntity<Object> getDelegations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            List<DelegationDto> delegations = delegationService.getDelegations(authorizedUsername);
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getReceivedDelegations")
    public ResponseEntity<Object> getReceivedDelegations(@RequestParam("username") String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            Map<String, Integer> delegations = delegationService.getReceivedDelegations(username);
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
