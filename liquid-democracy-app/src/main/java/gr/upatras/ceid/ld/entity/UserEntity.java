package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Entity(name = "user")
@NoArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;


    @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; //TODO: Enum

    @Column(nullable = false)
    private String securityQuestion;

    @Column(nullable = false)
    private String securityAnswerHash;

    @OneToMany(mappedBy = "voter")
    private List<VoteEntity> votes;

    @OneToMany(mappedBy = "delegate")
    private List<DelegationEntity> delegations;

    public UserEntity(Long id) {
        this.id = id;
    }

    public UserEntity(String username, String email, String passwordHash, String securityQuestion, String securityAnswerHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.role = "USER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.getPasswordHash();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}