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

import java.util.*;

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
public class NumberListExpertRule extends AbstractFieldExpertRule<List<Double>> {

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
            case BETWEEN -> {
                Double lowerLimit = Collections.min(getValue());
                Double upperLimit = Collections.max(getValue());
                yield identifiableValue.compareTo(lowerLimit) >= 0 && identifiableValue.compareTo(upperLimit) <= 0;
            }
            case IN -> getValue().contains(identifiableValue);
            case NOT_IN -> !getValue().contains(identifiableValue);
            default ->
                throw new PowsyblException(this.getOperator() + " operator not supported with number list rule data type");
        };
    }
}
