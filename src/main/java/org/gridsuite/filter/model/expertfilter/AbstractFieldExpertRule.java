package org.gridsuite.filter.model.expertfilter;

import org.gridsuite.filter.model.AbstractExpertRule;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

public abstract class AbstractFieldExpertRule<I> extends AbstractExpertRule {

    protected OperatorType operatorType;

    protected FieldType fieldType;

    protected I value;

    protected AbstractFieldExpertRule(OperatorType operatorType, FieldType fieldType, I value) {
        this.operatorType = operatorType;
        this.fieldType = fieldType;
        this.value = value;
    }
}
