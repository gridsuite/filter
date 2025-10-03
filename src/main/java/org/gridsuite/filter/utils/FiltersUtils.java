/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FiltersUtils {
    private FiltersUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static List<Identifiable<?>> filterById(@Nonnull final Network network, @Nonnull final IdentifierListFilter filter,
                                                    @Nonnull final Function<Network, Stream<? extends Identifiable<?>>> extractor) {
        final List<String> equipmentIds = filter.getFilterEquipmentsAttributes()
                                                .stream()
                                                .map(IdentifierListFilterEquipmentAttributes::getEquipmentID)
                                                .toList();
        return extractor.apply(network)
                        .filter(ident -> equipmentIds.contains(ident.getId()))
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<Identifiable<?>> filterByRules(@Nonnull final Network network,
                                                       @Nonnull final ExpertFilter filter, final FilterLoader filterLoader,
                                                       @Nonnull final Function<Network, Stream<? extends Identifiable<?>>> extractor) {
        final AbstractExpertRule rule = filter.getRules();
        final Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
        return extractor.apply(network)
                        .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters))
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<Identifiable<?>> getIdentifiableList(@Nonnull final Network network,
                                                             @Nonnull final AbstractFilter filter, final FilterLoader filterLoader,
                                                             @Nonnull final Function<Network, Stream<? extends Identifiable<?>>> extractor) {
        return switch (filter) {
            case final IdentifierListFilter identifierListFilter -> filterById(network, identifierListFilter, extractor);
            case final ExpertFilter expertFilter -> filterByRules(network, expertFilter, filterLoader, extractor);
            default -> List.of();
        };
    }

    public static List<Identifiable<?>> getIdentifiables(@Nonnull final AbstractFilter filter, @Nonnull final Network network,
                                                         final FilterLoader filterLoader) {
        final Function<Network, Stream<? extends Identifiable<?>>> getter = switch (filter.getEquipmentType()) {
            case null -> throw new NullPointerException("Equipment type cannot be null");
            case BATTERY -> Network::getBatteryStream;
            case BUS -> network1 -> {
                if (filter instanceof final ExpertFilter expertFilter) {
                    // topologyKind is an optional info attached into an expert filter when filtering bus for optimizing the perf
                    // note that with voltage levels of kind TopologyKind.NODE_BREAKER, buses are computed on-the-fly => expensive
                    final TopologyKind topologyKind = expertFilter.getTopologyKind();
                    return network.getVoltageLevelStream()
                            .filter(vl -> topologyKind == null || vl.getTopologyKind() == topologyKind)
                            .map(VoltageLevel::getBusBreakerView)
                            .flatMap(VoltageLevel.BusBreakerView::getBusStream);
                }
                // Note: the webapps don't permit filtering a BUS by ID, so this case will not happen in reality
                return Stream.empty();
            };
            case BUSBAR_SECTION -> Network::getBusbarSectionStream;
            case DANGLING_LINE -> Network::getDanglingLineStream;
            case GENERATOR -> Network::getGeneratorStream;
            case HVDC_LINE -> Network::getHvdcLineStream;
            case LCC_CONVERTER_STATION -> Network::getLccConverterStationStream;
            case LINE -> Network::getLineStream;
            case LOAD -> Network::getLoadStream;
            case SHUNT_COMPENSATOR -> Network::getShuntCompensatorStream;
            case STATIC_VAR_COMPENSATOR -> Network::getStaticVarCompensatorStream;
            case SUBSTATION -> Network::getSubstationStream;
            case THREE_WINDINGS_TRANSFORMER -> Network::getThreeWindingsTransformerStream;
            case TWO_WINDINGS_TRANSFORMER -> Network::getTwoWindingsTransformerStream;
            case VOLTAGE_LEVEL -> Network::getVoltageLevelStream;
            case VSC_CONVERTER_STATION -> Network::getVscConverterStationStream;
        };
        return getIdentifiableList(network, filter, filterLoader, getter);
    }
}
