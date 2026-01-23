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

import java.util.Optional;

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
public class BooleanExpertRule extends AbstractFieldExpertRule<Boolean> {

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        String fieldValue = getFieldValue(getField(), null, identifiable);
        if (fieldValue == null) {
            return this.getOperator() == NOT_EXISTS;
        }
        boolean identifiableValue = Boolean.parseBoolean(fieldValue);
        boolean filterValue = Optional.ofNullable(getValue()).orElse(false);
        return switch (getOperator()) {
            case EQUALS -> identifiableValue == filterValue;
            case NOT_EQUALS -> identifiableValue != filterValue;
            case EXISTS -> identifiableValue;
            case NOT_EXISTS -> !identifiableValue;
            default -> throw new PowsyblException(getOperator() + " operator not supported with boolean rule data type");
        };
    }
}
