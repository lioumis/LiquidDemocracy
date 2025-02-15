package gr.upatras.ceid.ld.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Action {
    PASSWORD_RESET(1, "Επαναφορά κωδικού πρόσβασης"),
    PASSWORD_CHANGE(2, "Αλλαγή κωδικού πρόσβασης"),
    USER_REGISTRATION(3, "Εγγραφή χρήστη"),
    VOTE_DELEGATION(4, "Ανάθεση ψήφου"),
    DIRECT_VOTE(5, "Άμεση Ψήφος"),
    DELEGATED_VOTE(6, "Εξουσιοδοτημένη Ψήφος"),
    VOTING_CREATION(7, "Δημιουργία Ψηφοφορίας"),
    VOTING_EDIT(8, "Επεξεργασία Ψηφοφορίας");

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