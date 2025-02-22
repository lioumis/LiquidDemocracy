package gr.upatras.ceid.ld.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Action {
    PASSWORD_RESET(1, "Επαναφορά Κωδικού Πρόσβασης"),
    PASSWORD_CHANGE(2, "Αλλαγή Κωδικού Πρόσβασης"),
    USER_REGISTRATION(3, "Εγγραφή Χρήστη"),
    VOTE_DELEGATION(4, "Ανάθεση Ψήφου"),
    DIRECT_VOTE(5, "Άμεση Ψήφος"),
    DELEGATED_VOTE(6, "Εξουσιοδοτημένη Ψήφος"),
    VOTING_CREATION(7, "Δημιουργία Ψηφοφορίας"),
    VOTING_EDIT(8, "Επεξεργασία Ψηφοφορίας"),
    NEW_ROLE(9, "Νέος Ρόλος"),
    REQUEST_CREATION(10, "Δημιουργία Αιτήματος"),
    REQUEST_APPROVAL(11, "Αποδοχή Αιτήματος"),
    REQUEST_REJECTION(12, "Απόρριψη Αιτήματος"),
    REACTION(13, "Αντίδραση σε Μήνυμα"),
    COMMENT(14, "Σχόλιο"),
    FEEDBACK(15, "Ανατροφοδότηση"),
    ROLE_REVOCATION(16, "Ανάκληση Ρόλου"),
    DELEGATE_ADDITION(17, "Προσθήκη Αντιπροσώπου σε Ψηφοφορία");

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
