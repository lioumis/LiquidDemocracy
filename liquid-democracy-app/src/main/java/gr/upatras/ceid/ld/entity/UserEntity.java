package gr.upatras.ceid.ld.entity;

import gr.upatras.ceid.ld.converter.RoleConverter;
import gr.upatras.ceid.ld.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String securityQuestion;

    @Column(nullable = false)
    private String securityAnswerHash;

    @OneToMany(mappedBy = "voter")
    private List<VoteEntity> votes;

    @OneToMany(mappedBy = "originalVoter")
    private List<VoteEntity> originalVotes;

    @OneToMany(mappedBy = "delegate")
    private List<DelegationEntity> delegations;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role_id")
    @Convert(converter = RoleConverter.class)
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "electoralCommittee")
    private Set<VotingEntity> overseenVotings = new HashSet<>();

    public UserEntity(Long id) {
        this.id = id;
    }

    public UserEntity(String username, String email, String name, String surname, String passwordHash, String securityQuestion, String securityAnswerHash) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.passwordHash = passwordHash;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.roles.add(Role.VOTER);
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