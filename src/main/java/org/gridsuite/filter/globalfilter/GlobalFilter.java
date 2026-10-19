/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.wip.ExpertFilter;

import java.util.*;

import static org.gridsuite.filter.globalfilter.GlobalFilterUtils.buildExpertFilter;

/**
 * @author maissa Souissi <maissa.souissi at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
// TODO convert to record when loadflow-server and computation lib stop to extends it
public class GlobalFilter {
    private List<List<Integer>> voltageRanges;
    private List<Country> countryCode;
    private List<UUID> substationOrVoltageLevelFilter; // list of generic filters containing only voltage level and substation filters
    private List<UUID> genericFilter;
    private Map<String, List<String>> substationProperty;

    /**
     * @return {@code true} if all filter parameters are empty, else {@code false}.
     */
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(this.voltageRanges)
            && CollectionUtils.isEmpty(this.countryCode)
            && CollectionUtils.isEmpty(this.genericFilter)
            && CollectionUtils.isEmpty(this.substationOrVoltageLevelFilter)
            && (MapUtils.isEmpty(this.substationProperty) || this.substationProperty.values().stream().allMatch(CollectionUtils::isEmpty));
    }

    @Nonnull
    public List<Identifiable<?>> evaluate(@Nonnull final Network network,
                                          @Nonnull final List<EquipmentType> equipmentTypes,
                                          @Nonnull final FilterLoader filterLoader) {
        List<Identifiable<?>> identifiables = new ArrayList<>();
        for (final EquipmentType equipmentType : equipmentTypes) {
            // Extract IDs from expert filter
            final ExpertFilter expertFilter = buildExpertFilter(this, equipmentType, filterLoader);
            if (expertFilter != null) {
                identifiables.addAll(expertFilter.evaluate(network));
            }
        }
        return identifiables;
    }
}
