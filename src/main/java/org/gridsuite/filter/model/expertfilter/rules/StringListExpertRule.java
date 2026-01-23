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

import java.util.List;

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
public class StringListExpertRule extends AbstractFieldExpertRule<List<String>> {

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        String identifiableValue = getFieldValue(this.getField(), null, identifiable);
        if (identifiableValue == null) {
            return this.getOperator() == NOT_EXISTS;
        }
        return switch (this.getOperator()) {
            case IN -> getValue().stream().anyMatch(identifiableValue::equalsIgnoreCase);
            case NOT_IN -> getValue().stream().noneMatch(identifiableValue::equalsIgnoreCase);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with string list rule data type");
        };
    }
}
