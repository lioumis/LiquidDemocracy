package gr.upatras.ceid.ld.delegation.controller;

import gr.upatras.ceid.ld.common.authorization.AuthorizationService;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.delegation.dto.DelegateDto;
import gr.upatras.ceid.ld.delegation.dto.DelegationDto;
import gr.upatras.ceid.ld.delegation.dto.DelegationRequestDto;
import gr.upatras.ceid.ld.delegation.dto.ReceivedDelegationDto;
import gr.upatras.ceid.ld.delegation.service.DelegationService;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/delegations")
public class DelegationController {
    private static final String AUTHORIZATION_ERROR_MESSAGE = "Δεν έχετε άδεια να εκτελέσετε αυτήν την ενέργεια";
    private static final String ERROR_KEYWORD = "error";
    private static final String MESSAGE_KEYWORD = "message";
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();
    private static final Set<Role> ALLOWED_FOR_DELEGATION = new HashSet<>();
    private static final Set<Role> ALLOWED_FOR_EDIT = new HashSet<>();

    static {
        ALLOWED_ROLES.add(Role.VOTER);
        ALLOWED_ROLES.add(Role.REPRESENTATIVE);
        ALLOWED_FOR_DELEGATION.add(Role.REPRESENTATIVE);
        ALLOWED_FOR_EDIT.add(Role.ELECTORAL_COMMITTEE);
    }

    private final DelegationService delegationService;

    private final AuthorizationService authorizationService;

    public DelegationController(DelegationService delegationService, AuthorizationService authorizationService) {
        this.delegationService = delegationService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/addDelegate")
    public ResponseEntity<Map<String, String>> addDelegate(@RequestBody DelegateDto delegateDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_FOR_EDIT);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            delegationService.addDelegate(delegateDto.delegate(), authorizedUsername, delegateDto.votingId());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE_KEYWORD, "Ο αντιπρόσωπος προστέθηκε με επιτυχία");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/removeDelegate")
    public ResponseEntity<Map<String, String>> removeDelegate(@RequestBody DelegateDto delegateDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_FOR_EDIT);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            delegationService.removeDelegate(delegateDto.delegate(), authorizedUsername, delegateDto.votingId());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE_KEYWORD, "Η ψήφος αφαιρέθηκε με επιτυχία");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getDelegates")
    public ResponseEntity<Object> getDelegates(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_FOR_EDIT);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<DelegationDto> delegations = delegationService.getDelegates(votingId);
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getPotentialDelegates")
    public ResponseEntity<Object> getPotentialDelegates() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_FOR_EDIT);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<DelegationDto> delegations = delegationService.getPotentialDelegates();
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/delegate")
    public ResponseEntity<Map<String, String>> delegateVote(@RequestBody DelegationRequestDto delegationRequestDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            UserEntity authorizedUser = authorizationService.getAuthorizedUser(usernameFromToken);

            if (!delegationRequestDto.delegator().equals(authorizedUser.getUsername())) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            authorizationService.checkRoles(authorizedUser, ALLOWED_ROLES);

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
            log.error(e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE_KEYWORD, "Η ψήφος ανατέθηκε με επιτυχία");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getDelegations")
    public ResponseEntity<Object> getDelegations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            UserEntity authorizedUser = authorizationService.getAuthorizedUser(usernameFromToken);

            if (authorizedUser == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            authorizationService.checkRoles(authorizedUser, ALLOWED_ROLES);

            List<DelegationDto> delegations = delegationService.getDelegations(authorizedUser.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getReceivedDelegations")
    public ResponseEntity<Object> getReceivedDelegations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();

            UserEntity authorizedUser = authorizationService.getAuthorizedUser(usernameFromToken);

            if (authorizedUser == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            authorizationService.checkRoles(authorizedUser, ALLOWED_FOR_DELEGATION);

            List<ReceivedDelegationDto> delegations = delegationService.getReceivedDelegations(authorizedUser.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(delegations);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
