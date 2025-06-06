package gr.upatras.ceid.ld.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    VOTER(1, "Ψηφοφόρος"),
    REPRESENTATIVE(2, "Αντιπρόσωπος"),
    ELECTORAL_COMMITTEE(3, "Εφορευτική Επιτροπή"),
    SYSTEM_ADMIN(4, "Διαχειριστής Συστήματος");

    private final int id;
    private final String name;

    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid ID for Role: " + id);
    }

    public static Role fromName(String name) {
        for (Role role : values()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid name for Role: " + name);
    }

}
