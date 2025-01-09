package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.dto.*;
import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.service.AuthorizationService;
import gr.upatras.ceid.ld.service.VotingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/votings")
public class VotingController {
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();
    private static final Set<Role> ALLOWED_ROLES_FOR_CREATION = new HashSet<>();

    static {
        ALLOWED_ROLES.add(Role.VOTER);
        ALLOWED_ROLES.add(Role.REPRESENTATIVE);

        ALLOWED_ROLES_FOR_CREATION.add(Role.ELECTORAL_COMMITTEE);
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
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            List<SuggestedVotingDto> suggestedVotings = votingService.getSuggestedVotings();
            return ResponseEntity.status(HttpStatus.OK).body(suggestedVotings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getVotings")
    public ResponseEntity<Object> getVotings() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            List<VotingDto> votings = votingService.getVotings(authorizedUsername);
            return ResponseEntity.status(HttpStatus.OK).body(votings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
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
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            VotingDetailsDto votingDetails = votingService.getVotingDetails(authorizedUsername, votingId);
            return ResponseEntity.status(HttpStatus.OK).body(votingDetails);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/vote")
    public ResponseEntity<Map<String, String>> castVote(@RequestBody VoteDto voteDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            votingService.castVote(authorizedUsername, voteDto.votingId().longValue(), voteDto.vote());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Vote cast successfully!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/createVoting")
    public ResponseEntity<String> createVoting(@RequestBody VotingCreationDto votingCreationDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();  //TODO: Check if the specific user is allowed to create this voting.
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES_FOR_CREATION);

            votingService.createVoting(authorizedUsername, votingCreationDto);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Voting created successfully!");
    }

    @GetMapping("/getDiscussion")
    public ResponseEntity<Object> getDiscussion(@RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            List<DiscussionDto> votings = votingService.getDiscussions(authorizedUsername, votingId);
            return ResponseEntity.status(HttpStatus.OK).body(votings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
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
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            votingService.reactToMessage(reactionDto.messageId(), authorizedUsername, reactionDto.action());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reaction saved successfully!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/comment")
    public ResponseEntity<Map<String, String>> comment(@RequestBody CommentDto commentDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            //TODO: Length & special chars? validation
            votingService.addComment(authorizedUsername, commentDto.votingId(), commentDto.message());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Comment saved successfully!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, String>> feedback(@RequestBody CommentDto commentDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            //TODO: Length & special chars? validation
            votingService.addFeedback(authorizedUsername, commentDto.votingId(), commentDto.message());
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AuthorizationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Feedback saved successfully!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
