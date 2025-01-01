package gr.upatras.ceid.ld.authentication;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Object> generateToken(@RequestBody AuthRequest authRequest) {
        try {
            Optional<UserEntity> byEmail = userRepository.findByEmail(authRequest.getUsername());
            String username = byEmail.isPresent() ? byEmail.get().getUsername() : authRequest.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, authRequest.getPassword()));
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.status(HttpStatus.OK).body(new TokenResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Το όνομα χρήστη ή ο κωδικός πρόσβασης είναι λάθος");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}