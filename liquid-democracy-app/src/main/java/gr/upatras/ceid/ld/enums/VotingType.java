package gr.upatras.ceid.ld.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VotingType {
    SINGLE(1, "Μοναδική Επιλογή"),
    MULTIPLE(2, "Πολλαπλή Επιλογή");

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

    public static VotingType fromName(String name) {
        for (VotingType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid name for Type: " + name);
    }
}
