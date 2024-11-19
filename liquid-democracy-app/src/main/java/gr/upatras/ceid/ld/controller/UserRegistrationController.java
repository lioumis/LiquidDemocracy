package gr.upatras.ceid.ld.controller;

import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRegistrationController {

    private final UserService userService;

    public UserRegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam("username") String username, @RequestParam("password") String password,
                                           @RequestParam("email") String email, @RequestParam("securityQuestion") String securityQuestion,
                                           @RequestParam("securityAnswer") String securityAnswer) {
        try {
            userService.registerUser(username, email, password, securityQuestion, securityAnswer);
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
}
