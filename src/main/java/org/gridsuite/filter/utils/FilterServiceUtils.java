/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FilterServiceUtils {
    private FilterServiceUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<IdentifiableAttributes> getIdentifiableAttributes(AbstractFilter filter, Network network, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilter identifierListFilter &&
            (filter.getEquipmentType() == EquipmentType.GENERATOR ||
                filter.getEquipmentType() == EquipmentType.LOAD)) {
            return FiltersUtils.getIdentifiables(filter, network, filterLoader)
                .stream()
                .map(identifiable -> new IdentifiableAttributes(identifiable.getId(),
                    identifiable.getType(),
                    identifierListFilter.getDistributionKey(identifiable.getId())))
                .toList();
        } else {
            return FiltersUtils.getIdentifiables(filter, network, filterLoader).stream()
                .map(identifiable -> new IdentifiableAttributes(identifiable.getId(), identifiable.getType(), null))
                .toList();
        }
    }

    public static List<FilterEquipments> getFilterEquipmentsFromUuid(Network network, List<UUID> uuids, FilterLoader filterLoader) {
        return getFilterEquipmentsFromUuid(network, uuids, filterLoader, Set.of());
    }

    public static List<FilterEquipments> getFilterEquipmentsFromUuid(Network network, List<UUID> uuids, FilterLoader filterLoader, Set<FilterType> filterTypesToExclude) {
        List<AbstractFilter> filters = filterLoader.getFilters(uuids);
        return filters.stream()
            .filter(filter -> filter != null && !filterTypesToExclude.contains(filter.getType()))
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }

    public static List<FilterEquipments> getFilterEquipmentsFromUuid(Network network, UUID uuid, FilterLoader filterLoader) {
        List<AbstractFilter> filters = filterLoader.getFilters(List.of(uuid));
        return filters.stream()
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }
}
