package gr.upatras.ceid.ld.common.converter;

import gr.upatras.ceid.ld.common.enums.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.getId();
    }

    @Override
    public Role convertToEntityAttribute(Integer id) {
        if (id == null) {
            return null;
        }
        return Role.fromId(id);
    }
}
