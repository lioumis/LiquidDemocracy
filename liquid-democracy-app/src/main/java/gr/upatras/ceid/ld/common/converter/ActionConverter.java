package gr.upatras.ceid.ld.common.converter;

import gr.upatras.ceid.ld.common.enums.Action;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionConverter implements AttributeConverter<Action, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Action action) {
        if (action == null) {
            return null;
        }
        return action.getId();
    }

    @Override
    public Action convertToEntityAttribute(Integer id) {
        if (id == null) {
            return null;
        }
        return Action.fromId(id);
    }
}