/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.criteriafilter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.EquipmentType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class HvdcLineFilter extends AbstractLineFilter {
    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.HVDC_LINE;
    }

    private NumericalFilter nominalVoltage;

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                && nominalVoltage == null;
    }
}
