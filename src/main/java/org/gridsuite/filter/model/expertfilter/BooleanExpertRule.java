/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.Optional;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.NOT_EXISTS;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class BooleanExpertRule extends AbstractFieldExpertRule<Boolean> {

    protected BooleanExpertRule(OperatorType operatorType, FieldType fieldType, Boolean value) {
        super(operatorType, fieldType, value);
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = getFieldValue(fieldType, null, identifiable);
        if (fieldValue == null) {
            return operatorType == NOT_EXISTS;
        }
        boolean identifiableValue = Boolean.parseBoolean(fieldValue);
        boolean filterValue = Optional.ofNullable(value)
                .orElse(false);
        return switch (operatorType) {
            case EQUALS -> identifiableValue == filterValue;
            case NOT_EQUALS -> identifiableValue != filterValue;
            case EXISTS -> identifiableValue;
            case NOT_EXISTS -> !identifiableValue;
            default -> throw new PowsyblException(operatorType + " operator not supported with boolean rule data type");
        };
    }
}
