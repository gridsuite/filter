/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.NOT_EXISTS;
import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class NumberExpertRule extends AbstractFieldExpertRule<Double> {

    public static Double getNumberValue(String value) {
        return value == null ? Double.NaN : Double.parseDouble(value);
    }

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        Double identifiableValue = getNumberValue(getFieldValue(this.getField(), null, identifiable));
        if (Double.isNaN(identifiableValue)) {
            return this.getOperator() == NOT_EXISTS;
        }
        return switch (this.getOperator()) {
            case EQUALS -> identifiableValue.equals(getValue());
            case GREATER_OR_EQUALS -> identifiableValue.compareTo(getValue()) >= 0;
            case GREATER -> identifiableValue.compareTo(getValue()) > 0;
            case LOWER_OR_EQUALS -> identifiableValue.compareTo(getValue()) <= 0;
            case LOWER -> identifiableValue.compareTo(getValue()) < 0;
            case EXISTS -> true; // We return true here because we already test above if identifiableValue is NaN.
            case NOT_EXISTS -> false; // if true, checked above
            default ->
                throw new PowsyblException(this.getOperator() + " operator not supported with number rule data type");
        };
    }
}
