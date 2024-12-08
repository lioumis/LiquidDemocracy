package gr.upatras.ceid.ld.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VotingType {
    BOOLEAN(1, "Ναι/Όχι"),
    SINGLE(2, "Μονή Επιλογή"),
    MULTIPLE(3, "Πολλαπλή Επιλογή");

    private final int id;
    private final String name;

    public static VotingType fromId(int id) {
        for (VotingType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ID for Voting Type: " + id);
    }
}
