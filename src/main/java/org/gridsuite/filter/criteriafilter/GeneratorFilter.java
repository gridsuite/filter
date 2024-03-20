/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.criteriafilter;

import com.powsybl.iidm.network.EnergySource;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.gridsuite.filter.utils.EquipmentType;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class GeneratorFilter extends AbstractInjectionFilter {

    private EnergySource energySource;

    public GeneratorFilter(String equipmentID, String equipmentName, String substationName,
        SortedSet<String> countries, Map<String, List<String>> substationFreeProperties, Map<String, List<String>> freeProperties,
        NumericalFilter nominalVoltage, EnergySource energySource) {
        super(new InjectionFilterAttributes(equipmentID, equipmentName, substationName, countries, substationFreeProperties, freeProperties, nominalVoltage));
        this.energySource = energySource;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && energySource == null;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.GENERATOR;
    }
}
