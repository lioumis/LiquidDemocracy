package gr.upatras.ceid.ld.voting.controller;

import gr.upatras.ceid.ld.common.authorization.AuthorizationService;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.common.exception.VotingCreationException;
import gr.upatras.ceid.ld.voting.dto.*;
import gr.upatras.ceid.ld.voting.service.VotingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/votings")
public class VotingController {
    private static final String AUTHORIZATION_ERROR_MESSAGE = "Δεν έχετε άδεια να εκτελέσετε αυτήν την ενέργεια";
    private static final String ERROR_KEYWORD = "error";
    private static final String MESSAGE_KEYWORD = "message";
    private static final Set<Role> VOTING_ROLES = new HashSet<>();
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();
    private static final Set<Role> ALLOWED_ROLES_FOR_CREATION = new HashSet<>();
    private static final Set<Role> ALLOWED_ROLES_FOR_UPDATE = new HashSet<>();

    static {
        VOTING_ROLES.add(Role.VOTER);
        VOTING_ROLES.add(Role.REPRESENTATIVE);

        ALLOWED_ROLES.add(Role.VOTER);
        ALLOWED_ROLES.add(Role.REPRESENTATIVE);
        ALLOWED_ROLES.add(Role.ELECTORAL_COMMITTEE);

        ALLOWED_ROLES_FOR_CREATION.add(Role.SYSTEM_ADMIN);

        ALLOWED_ROLES_FOR_UPDATE.add(Role.ELECTORAL_COMMITTEE);
    }

    private final VotingService votingService;

    private final AuthorizationService authorizationService;

    public VotingController(VotingService votingService, AuthorizationService authorizationService) {
        this.votingService = votingService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/getSuggestedVotings")
    public ResponseEntity<Object> getSuggestedVotings() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, VOTING_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<SuggestedVotingDto> suggestedVotings = votingService.getSuggestedVotings();
            return ResponseEntity.status(HttpStatus.OK).body(suggestedVotings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getVotings")
    public ResponseEntity<Object> getVotings(@RequestParam("selectedRole") String selectedRoleString) {
        try {
            Role selectedRole = Role.fromName(selectedRoleString);
            if (!ALLOWED_ROLES.contains(selectedRole)) {
                throw new AuthorizationException("Ο επιλεγμένος ρόλος δεν μπορεί να πραγματοποιήσει αυτή την ενέργεια");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, Set.of(selectedRole));

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<VotingDto> votings = votingService.getVotings(authorizedUsername, selectedRole);
            return ResponseEntity.status(HttpStatus.OK).body(votings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getVotingDetails")
    public ResponseEntity<Object> getVotingDetails(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            VotingDetailsDto votingDetails = votingService.getVotingDetails(authorizedUsername, votingId);
            return ResponseEntity.status(HttpStatus.OK).body(votingDetails);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getVotingTitles")
    public ResponseEntity<Object> getVotingTitles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<VotingTitleDto> votingTitles = votingService.getVotingTitles();
            return ResponseEntity.status(HttpStatus.OK).body(votingTitles);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/vote")
    public ResponseEntity<Map<String, String>> castVote(@RequestBody VoteDto voteDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, VOTING_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.castVote(authorizedUsername, voteDto.votingId().longValue(), voteDto.votes());
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
        response.put(MESSAGE_KEYWORD, "Η ψήφος καταχωρήθηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getRequests")
    public ResponseEntity<Object> getRequests(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES_FOR_UPDATE);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<ParticipationRequestDto> participationRequests = votingService.getRequests(authorizedUsername, votingId);
            return ResponseEntity.status(HttpStatus.OK).body(participationRequests);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/processRequest")
    public ResponseEntity<Map<String, String>> processRequest(@RequestBody RequestProcessDto requestProcessDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES_FOR_UPDATE);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.processRequest(authorizedUsername, requestProcessDto.requestId(), requestProcessDto.approve());
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
        if (requestProcessDto.approve()) {
            response.put(MESSAGE_KEYWORD, "Το αίτημα εγκρίθηκε επιτυχώς");
        } else {
            response.put(MESSAGE_KEYWORD, "Το αίτημα απορρίφθηκε επιτυχώς");
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/requestAccessToVoting")
    public ResponseEntity<Map<String, String>> requestAccessToVoting(@RequestBody AccessRequestDto accessRequestDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, VOTING_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.requestAccess(authorizedUsername, accessRequestDto.votingId());
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
        response.put(MESSAGE_KEYWORD, "Το αίτημα συμμετοχής καταχωρήθηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/hasAccessToVoting")
    public ResponseEntity<Object> hasAccessToVoting(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            VotingAccessDto votingAccess = votingService.hasAccess(authorizedUsername, votingId, true);
            return ResponseEntity.status(HttpStatus.OK).body(votingAccess);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/initializeVoting")
    public ResponseEntity<Map<String, String>> initializeVoting(@RequestBody VotingInitializationDto votingInitializationDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES_FOR_CREATION);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.initializeVoting(authorizedUsername, votingInitializationDto);
        } catch (VotingCreationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEYWORD, e.getMessage());
            e.getMetadata().forEach((index, message) -> errorResponse.put("member" + index, message));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
        response.put(MESSAGE_KEYWORD, "Η ψηφοφορία δημιουργήθηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/editVoting")
    public ResponseEntity<Map<String, String>> editVoting(@RequestBody VotingCreationDto votingCreationDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES_FOR_UPDATE);

            votingService.editVoting(authorizedUsername, votingCreationDto);
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
        response.put(MESSAGE_KEYWORD, "Η ψηφοφορία τροποποιήθηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getDiscussion")
    public ResponseEntity<Object> getDiscussion(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<DiscussionDto> votings = votingService.getDiscussions(authorizedUsername, votingId);
            return ResponseEntity.status(HttpStatus.OK).body(votings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/react")
    public ResponseEntity<Map<String, String>> react(@RequestBody ReactionDto reactionDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.reactToMessage(reactionDto.messageId(), authorizedUsername, reactionDto.action());
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
        response.put(MESSAGE_KEYWORD, "Η αντίδραση αποθηκεύτηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/comment")
    public ResponseEntity<Map<String, String>> comment(@RequestBody CommentDto commentDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.addComment(authorizedUsername, commentDto.votingId(), commentDto.message());
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
        response.put(MESSAGE_KEYWORD, "Το σχόλιο αποθηκεύτηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, String>> feedback(@RequestBody CommentDto commentDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, VOTING_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            votingService.addFeedback(authorizedUsername, commentDto.votingId(), commentDto.message());
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
        response.put(MESSAGE_KEYWORD, "Η ανατροφοδότηση αποθηκεύτηκε επιτυχώς");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getFeedback")
    public ResponseEntity<Object> getFeedback(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES_FOR_UPDATE);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            List<FeedbackDto> feedbackDtoList = votingService.getFeedback(authorizedUsername, votingId);
            return ResponseEntity.status(HttpStatus.OK).body(feedbackDtoList);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
