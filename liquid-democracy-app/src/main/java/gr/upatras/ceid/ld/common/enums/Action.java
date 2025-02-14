package gr.upatras.ceid.ld.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Action {
    PASSWORD_RESET(1, "Επαναφορά κωδικού πρόσβασης"),
    USER_REGISTRATION(2, "Εγγραφή χρήστη"),
    VOTE_DELEGATION(3, "Ανάθεση ψήφου"),
    DIRECT_VOTE(4, "Άμεση Ψήφος"),
    DELEGATED_VOTE(5, "Εξουσιοδοτημένη Ψήφος"),
    VOTING_CREATION(6, "Δημιουργία Ψηφοφορίας");

    private final int id;
    private final String name;

    public static Action fromId(int id) {
        for (Action action : values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        throw new IllegalArgumentException("Invalid ID for Action: " + id);
    }
}