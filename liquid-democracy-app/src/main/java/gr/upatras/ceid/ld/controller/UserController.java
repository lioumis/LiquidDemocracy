package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.dto.ChangePasswordDto;
import gr.upatras.ceid.ld.dto.RegistrationDto;
import gr.upatras.ceid.ld.dto.ResetDto;
import gr.upatras.ceid.ld.dto.UserInformationDto;
import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.service.AuthorizationService;
import gr.upatras.ceid.ld.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserController {
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
            response.put("message", "User registered successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getSecurityQuestion")
    public ResponseEntity<Map<String, String>> getSecurityQuestion(@RequestParam("username") String username, @RequestParam("email") String email) {
        try {
            String securityQuestion = userService.getSecurityQuestion(username, email);
            Map<String, String> response = new HashMap<>();
            response.put("message", securityQuestion);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetDto resetDto) {
        try {
            userService.resetPassword(resetDto.username(), resetDto.email(), resetDto.securityAnswer(), resetDto.newPassword());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password was reset successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ValidationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
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
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            userService.changePassword(authorizedUsername, changePasswordDto.oldPassword(), changePasswordDto.newPassword());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ο κωδικός πρόσβασης άλλαξε με επιτυχία");
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
    }
}
