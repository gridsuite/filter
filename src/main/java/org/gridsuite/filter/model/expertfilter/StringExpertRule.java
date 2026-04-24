/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.*;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.NOT_EXISTS;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.isMultipleCriteriaOperator;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class StringExpertRule extends AbstractFieldExpertRule<String> {

    protected StringExpertRule(OperatorType operatorType, FieldType fieldType, String value) {
        super(operatorType, fieldType, value);
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String identifiableValue = getFieldValue(fieldType, null, identifiable);
        if (identifiableValue == null) {
            return operatorType == NOT_EXISTS;
        }
        return switch (operatorType) {
            case IS -> identifiableValue.equalsIgnoreCase(value);
            case CONTAINS -> StringUtils.containsIgnoreCase(identifiableValue, value);
            case BEGINS_WITH -> StringUtils.startsWithIgnoreCase(identifiableValue, value);
            case ENDS_WITH -> StringUtils.endsWithIgnoreCase(identifiableValue, value);
            case EXISTS -> !StringUtils.isEmpty(identifiableValue);
            case NOT_EXISTS -> StringUtils.isEmpty(identifiableValue);
            default -> throw new PowsyblException(operatorType + " operator not supported with string rule data type");
        };
    }
}
