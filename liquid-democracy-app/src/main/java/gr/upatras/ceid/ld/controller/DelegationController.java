package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.dto.DelegationDto;
import gr.upatras.ceid.ld.dto.DelegationRequestDto;
import gr.upatras.ceid.ld.dto.ReceivedDelegationDto;
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

import java.util.*;

@RestController
@RequestMapping("/delegations")
public class DelegationController {
    private static final String AUTHORIZATION_ERROR_MESSAGE = "Δεν έχετε άδεια να εκτελέσετε αυτήν την ενέργεια";
    private static final String ERROR_KEYWORD = "error";
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();
    private static final Set<Role> ALLOWED_FOR_DELEGATION = new HashSet<>();

    static {
        ALLOWED_ROLES.add(Role.VOTER);
        ALLOWED_ROLES.add(Role.REPRESENTATIVE);

        ALLOWED_FOR_DELEGATION.add(Role.REPRESENTATIVE);
    }

    private final DelegationService delegationService;

    private final AuthorizationService authorizationService;

    public DelegationController(DelegationService delegationService, AuthorizationService authorizationService) {
        this.delegationService = delegationService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/delegate")
    public ResponseEntity<Map<String, String>> delegateVote(@RequestBody DelegationRequestDto delegationRequestDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!delegationRequestDto.delegator().equals(authorizedUsername)) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            delegationService.delegateVote(delegationRequestDto.delegator(), delegationRequestDto.delegateName(),
                    delegationRequestDto.delegateSurname(), delegationRequestDto.votingId());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Η ψήφος ανατέθηκε με επιτυχία");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getDelegations")
    public ResponseEntity<Object> getDelegations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
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
    public ResponseEntity<Object> getReceivedDelegations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_FOR_DELEGATION);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<ReceivedDelegationDto> delegations = delegationService.getReceivedDelegations(authorizedUsername);
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
