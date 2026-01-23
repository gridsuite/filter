/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class EnumListExpertRule extends AbstractFieldExpertRule<List<String>> {

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        String identifiableValue = getFieldValue(getField(), null, identifiable);
        if (identifiableValue == null) {
            return false;
        }
        return switch (this.getOperator()) {
            case IN -> getValue().contains(identifiableValue);
            case NOT_IN -> !getValue().contains(identifiableValue);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with enum list rule data type");
        };
    }
}
