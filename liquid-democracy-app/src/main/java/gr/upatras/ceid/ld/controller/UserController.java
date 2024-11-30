package gr.upatras.ceid.ld.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class UserController {
    private static final Set<Role> ALLOWED_ROLES = new HashSet<>();

    static {
        ALLOWED_ROLES.addAll(List.of(Role.values()));
    }

    private final UserService userService;

    private final AuthorizationService authorizationService;

    public UserController(UserService userService, AuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam("username") String username, @RequestParam("password") String password,
                                           @RequestParam("name") String name, @RequestParam("surname") String surname,
                                           @RequestParam("email") String email, @RequestParam("securityQuestion") String securityQuestion,
                                           @RequestParam("securityAnswer") String securityAnswer) {
        try {
            userService.registerUser(username, email, name, surname, password, securityQuestion, securityAnswer);
            return ResponseEntity.status(HttpStatus.OK).body("User registered successfully");
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getSecurityQuestion")
    public ResponseEntity<String> getSecurityQuestion(@RequestParam("username") String username, @RequestParam("email") String email) {
        try {
            String securityQuestion = userService.getSecurityQuestion(username, email); //TODO: Maybe wrap in object to provide as JSON
            return ResponseEntity.status(HttpStatus.OK).body(securityQuestion);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getUserDetails")
    public ResponseEntity<Object> getUserDetails(@RequestParam("username") String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            UserInformationDto userDetails = userService.getUserDetails(username);
            return ResponseEntity.status(HttpStatus.OK).body(userDetails);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam("username") String username, @RequestParam("email") String email,
                                                @RequestParam("securityAnswer") String securityAnswer, @RequestParam("newPassword") String newPassword) {
        try {
            userService.resetPassword(username, email, securityAnswer, newPassword);
            return ResponseEntity.status(HttpStatus.OK).body("Password was reset successfully");
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestParam("username") String username, @RequestParam("oldPassword") String oldPassword,
                                                 @RequestParam("newPassword") String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String usernameFromToken = authentication.getName();
            String authorizedUsername = authorizationService.getAuthorizedUser(usernameFromToken, ALLOWED_ROLES);

            if (!username.equals(authorizedUsername)) {
                throw new AuthorizationException("You do not have permission to perform this action");
            }

            userService.changePassword(username, oldPassword, newPassword);
            return ResponseEntity.status(HttpStatus.OK).body("Password was changed successfully");
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
