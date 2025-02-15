package gr.upatras.ceid.ld.user.controller;

import gr.upatras.ceid.ld.common.authorization.AuthorizationService;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.user.dto.*;
import gr.upatras.ceid.ld.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
public class UserController {
    private static final String AUTHORIZATION_ERROR_MESSAGE = "Δεν έχετε άδεια να εκτελέσετε αυτήν την ενέργεια";
    private static final String ERROR_KEYWORD = "error";
    private static final String MESSAGE_KEYWORD = "message";
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();
    private static final Set<Role> MANAGEMENT_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.addAll(List.of(Role.values()));
        MANAGEMENT_ROLES.add(Role.SYSTEM_ADMIN);
    }

    private final UserService userService;

    private final AuthorizationService authorizationService;

    public UserController(UserService userService, AuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegistrationDto registrationDto) {
        try {
            userService.registerUser(registrationDto.username(), registrationDto.email(), registrationDto.name(),
                    registrationDto.surname(), registrationDto.password(), registrationDto.securityQuestion(),
                    registrationDto.securityAnswer());
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEYWORD, "Ο χρήστης καταχωρήθηκε επιτυχώς");
            return ResponseEntity.status(HttpStatus.OK).body(response);
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

    @GetMapping("/getSecurityQuestion")
    public ResponseEntity<Map<String, String>> getSecurityQuestion(@RequestParam("username") String username, @RequestParam("email") String email) {
        try {
            String securityQuestion = userService.getSecurityQuestion(username, email);
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEYWORD, securityQuestion);
            return ResponseEntity.status(HttpStatus.OK).body(response);
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

    @GetMapping("/getUserDetails")
    public ResponseEntity<Object> getUserDetails() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            UserInformationDto userDetails = userService.getUserDetails(authorizedUsername);
            return ResponseEntity.status(HttpStatus.OK).body(userDetails);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getAllUserDetails")
    public ResponseEntity<Object> getAllUserDetails() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            authorizationService.getAuthorizedUser(usernameFromToken, MANAGEMENT_ROLES);

            List<UserInformationDto> userDetails = userService.getAllUserDetails();
            return ResponseEntity.status(HttpStatus.OK).body(userDetails);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/addRole")
    public ResponseEntity<Map<String, String>> addRole(@RequestBody RoleDto roleDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            userService.addRole(authorizedUsername, roleDto.userId(), roleDto.role());
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEYWORD, "Ο ρόλος ανατέθηκε επιτυχώς");
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetDto resetDto) {
        try {
            userService.resetPassword(resetDto.username(), resetDto.email(), resetDto.securityAnswer(), resetDto.newPassword());
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEYWORD, "Η επαναφορά του κωδικού πρόσβασης ολοκληρώθηκε");
            return ResponseEntity.status(HttpStatus.OK).body(response);
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

    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (authorizedUsername == null) {
                throw new AuthorizationException(AUTHORIZATION_ERROR_MESSAGE);
            }

            userService.changePassword(authorizedUsername, changePasswordDto.oldPassword(), changePasswordDto.newPassword());
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEYWORD, "Ο κωδικός πρόσβασης άλλαξε με επιτυχία");
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
    }
}
