/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author maissa SOUISSI <maissa.souissi at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class PropertiesExpertRule extends AbstractFieldExpertRule<List<String>> {
    private String propertyName;

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        if (propertyName == null) {
            return false;
        }
        String propertyValue = getFieldValue(this.getField(), propertyName, identifiable);
        if (propertyValue == null) {
            return false;
        }
        return switch (this.getOperator()) {
            case IN -> getValue().stream().anyMatch(propertyValue::equalsIgnoreCase);
            case NOT_IN -> getValue().stream().noneMatch(propertyValue::equalsIgnoreCase);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with properties rule data type");
        };
    }
}
