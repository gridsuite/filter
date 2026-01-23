/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.*;
import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilterDto;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterDto;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FiltersUtils {
    private FiltersUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static List<String> getIdentifierListFilterEquipmentIds(IdentifierListFilterDto identifierListFilter) {
        return identifierListFilter.getFilterEquipmentsAttributes()
            .stream()
            .map(IdentifierListFilterEquipmentAttributes::getEquipmentID)
            .toList();
    }

    private static <I extends Injection<I>> Stream<Injection<I>> getInjectionList(Stream<Injection<I>> stream, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            return stream.filter(injection -> equipmentIds.contains(injection.getId()));
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            return stream.filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
        } else {
            return Stream.empty();
        }
    }

    private static List<Identifiable<?>> getGeneratorList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto || filter instanceof ExpertFilterDto) {
            Stream<Injection<Generator>> stream = getInjectionList(network.getGeneratorStream().map(generator -> generator), filter, filterLoader);
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getLoadList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<Load>> stream = getInjectionList(network.getLoadStream().map(load -> load), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getBatteryList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<Battery>> stream = getInjectionList(network.getBatteryStream().map(battery -> battery), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getStaticVarCompensatorList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<StaticVarCompensator>> stream = getInjectionList(network.getStaticVarCompensatorStream().map(svc -> svc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getShuntCompensatorList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<ShuntCompensator>> stream = getInjectionList(network.getShuntCompensatorStream().map(sc -> sc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getDanglingLineList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<DanglingLine>> stream = getInjectionList(network.getDanglingLineStream().map(dl -> dl), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getLccConverterStationList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<LccConverterStation>> stream = getInjectionList(network.getLccConverterStationStream().map(lcc -> lcc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getVscConverterStationList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<VscConverterStation>> stream = getInjectionList(network.getVscConverterStationStream().map(vsc -> vsc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getBusList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof ExpertFilterDto expertFilter) {
            // topologyKind is an optional info attached into expert filter when filtering bus for optimizing the perf
            // note that with voltage levels of kind TopologyKind.NODE_BREAKER, buses are computed on-the-fly => expensive
            var topologyKind = expertFilter.getTopologyKind();
            Predicate<VoltageLevel> voltageLevelFilter = vl -> topologyKind == null || vl.getTopologyKind() == topologyKind;

            Stream<Identifiable<?>> stream = network.getVoltageLevelStream()
                .filter(voltageLevelFilter)
                .map(VoltageLevel::getBusBreakerView)
                .flatMap(VoltageLevel.BusBreakerView::getBusStream);

            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            return stream.filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters)).toList();
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getBusbarSectionList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        Stream<Injection<BusbarSection>> stream = getInjectionList(network.getBusbarSectionStream().map(bbs -> bbs), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getLineList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<Line> stream = network.getLineStream()
                .filter(line -> equipmentIds.contains(line.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<Line> stream = network.getLineStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> get2WTransformerList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<TwoWindingsTransformer> stream = network.getTwoWindingsTransformerStream()
                .filter(twoWindingsTransformer -> equipmentIds.contains(twoWindingsTransformer.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<TwoWindingsTransformer> stream = network.getTwoWindingsTransformerStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> get3WTransformerList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<ThreeWindingsTransformer> stream = network.getThreeWindingsTransformerStream()
                .filter(threeWindingsTransformer -> equipmentIds.contains(threeWindingsTransformer.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<ThreeWindingsTransformer> stream = network.getThreeWindingsTransformerStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getHvdcList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentsIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<HvdcLine> stream = network.getHvdcLineStream()
                .filter(hvdcLine -> equipmentsIds.contains(hvdcLine.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<HvdcLine> stream = network.getHvdcLineStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getVoltageLevelList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<VoltageLevel> stream = network.getVoltageLevelStream()
                .filter(voltageLevel -> equipmentIds.contains(voltageLevel.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<VoltageLevel> stream = network.getVoltageLevelStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getSubstationList(Network network, AbstractFilterDto filter, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilterDto identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<Substation> stream = network.getSubstationStream()
                .filter(substation -> equipmentIds.contains(substation.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilterDto expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<Substation> stream = network.getSubstationStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    public static List<Identifiable<?>> getIdentifiables(AbstractFilterDto filter, Network network, FilterLoader filterLoader) {
        return switch (filter.getEquipmentType()) {
            case GENERATOR -> getGeneratorList(network, filter, filterLoader);
            case LOAD -> getLoadList(network, filter, filterLoader);
            case BATTERY -> getBatteryList(network, filter, filterLoader);
            case STATIC_VAR_COMPENSATOR -> getStaticVarCompensatorList(network, filter, filterLoader);
            case SHUNT_COMPENSATOR -> getShuntCompensatorList(network, filter, filterLoader);
            case LCC_CONVERTER_STATION -> getLccConverterStationList(network, filter, filterLoader);
            case VSC_CONVERTER_STATION -> getVscConverterStationList(network, filter, filterLoader);
            case HVDC_LINE -> getHvdcList(network, filter, filterLoader);
            case DANGLING_LINE -> getDanglingLineList(network, filter, filterLoader);
            case LINE -> getLineList(network, filter, filterLoader);
            case TWO_WINDINGS_TRANSFORMER -> get2WTransformerList(network, filter, filterLoader);
            case THREE_WINDINGS_TRANSFORMER -> get3WTransformerList(network, filter, filterLoader);
            case BUS -> getBusList(network, filter, filterLoader);
            case BUSBAR_SECTION -> getBusbarSectionList(network, filter, filterLoader);
            case VOLTAGE_LEVEL -> getVoltageLevelList(network, filter, filterLoader);
            case SUBSTATION -> getSubstationList(network, filter, filterLoader);
        };
    }

    /**
     * Combines multiple filter results using {@code AND} or {@code OR} logic.
     */
    @Nonnull
    public static <E> List<E> combineFilterResults(@Nullable final List<List<E>> filterResults, final boolean useAndLogic) {
        if (filterResults == null || filterResults.isEmpty()) {
            return List.of();
        }
        if (filterResults.size() == 1) {
            return filterResults.getFirst();
        }
        if (useAndLogic) {
            // Intersection of all results
            Set<E> result = new HashSet<>(filterResults.getFirst());
            for (int i = 1; i < filterResults.size(); i++) {
                result.retainAll(filterResults.get(i));
            }
            return new ArrayList<>(result);
        } else {
            // Union of all results
            Set<E> result = new HashSet<>();
            filterResults.forEach(result::addAll);
            return new ArrayList<>(result);
        }
    }
}
