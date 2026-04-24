package org.gridsuite.filter.model.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

public class EnumListExpertRule extends AbstractFieldExpertRule<List<String>> {

    protected EnumListExpertRule(
            OperatorType operatorType,
            FieldType fieldType,
            List<String> value
    ) {
        super(operatorType, fieldType, value);
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String identifiableValue = getFieldValue(fieldType, null, identifiable);
        if (identifiableValue == null) {
            return false;
        }
        return switch (operatorType) {
            case OperatorType.IN -> value.contains(identifiableValue);
            case OperatorType.NOT_IN -> !value.contains(identifiableValue);
            default -> throw new PowsyblException(operatorType + " operator not supported with enum list rule data type");
        };
    }
}
