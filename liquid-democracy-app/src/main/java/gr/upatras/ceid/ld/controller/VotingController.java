package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.dto.SuggestedVotingDto;
import gr.upatras.ceid.ld.dto.VotingCreationDto;
import gr.upatras.ceid.ld.dto.VotingDetailsDto;
import gr.upatras.ceid.ld.dto.VotingDto;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity<Object> getSuggestedVotings(@RequestParam("username") String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
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
    public ResponseEntity<Object> getVotings(@RequestParam("username") String username) { //TODO: SearchParams & validation
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            List<VotingDto> votings = votingService.getVotings(username);
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
    public ResponseEntity<Object> getVotingDetails(@RequestParam("username") String username, @RequestParam("voting") Long votingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            VotingDetailsDto votingDetails = votingService.getVotingDetails(username, votingId);
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

}
