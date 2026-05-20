package org.gridsuite.filter.wip.expert.rules;

import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.wip.expert.data.CombinatorType;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.gridsuite.filter.wip.expert.data.FieldType;
import org.gridsuite.filter.wip.expert.data.OperatorType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public abstract class AbstractExpertRule {

    private final CombinatorType combinatorType;

    private final FieldType fieldType;

    private final OperatorType operatorType;

    private final List<AbstractExpertRule> subRules;

    protected AbstractExpertRule(
            CombinatorType combinatorType,
            FieldType fieldType,
            OperatorType operatorType,
            List<AbstractExpertRule> subRules
    ) {
        this.combinatorType = combinatorType;
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.subRules = subRules;
    }

    public abstract boolean evaluateRule(Identifiable<?> identifiable);

    public abstract DataType getDataType();
}
