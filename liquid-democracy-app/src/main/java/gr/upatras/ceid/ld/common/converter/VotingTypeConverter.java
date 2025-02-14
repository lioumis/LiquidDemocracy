package gr.upatras.ceid.ld.common.converter;

import gr.upatras.ceid.ld.common.enums.VotingType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VotingTypeConverter implements AttributeConverter<VotingType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(VotingType type) {
        if (type == null) {
            return null;
        }
        return type.getId();
    }

    @Override
    public VotingType convertToEntityAttribute(Integer id) {
        if (id == null) {
            return null;
        }
        return VotingType.fromId(id);
    }
}