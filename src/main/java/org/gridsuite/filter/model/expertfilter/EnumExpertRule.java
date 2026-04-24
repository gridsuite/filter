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

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class EnumExpertRule extends AbstractFieldExpertRule<String> {

    protected EnumExpertRule(OperatorType operatorType, FieldType fieldType, String value) {
        super(operatorType, fieldType, value);
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String identifiableValue = getFieldValue(fieldType, null, identifiable);
        if (identifiableValue == null) {
            return false;
        }
        return switch (operatorType) {
            case OperatorType.EQUALS -> identifiableValue.equals(value);
            case OperatorType.NOT_EQUALS -> !identifiableValue.equals(value);
            default -> throw new PowsyblException(operatorType + " operator not supported with enum rule data type");
        };
    }
}
