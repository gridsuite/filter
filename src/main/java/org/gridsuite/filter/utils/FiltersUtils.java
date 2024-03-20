/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.criteriafilter.*;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;

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

    public static boolean matchesFreeProps(Map<String, List<String>> freeProperties, Substation substation) {
        if (substation == null) {
            return MapUtils.isEmpty(freeProperties);
        }
        if (MapUtils.isEmpty(freeProperties)) {
            return true;
        }
        return freeProperties.entrySet().stream().allMatch(p -> p.getValue().contains(substation.getProperty(p.getKey())));
    }

    public static boolean matchesFreeProps(Map<String, List<String>> freeProperties, Identifiable<?> identifiable) {
        if (identifiable == null) {
            return MapUtils.isEmpty(freeProperties);
        }
        if (MapUtils.isEmpty(freeProperties)) {
            return true;
        }
        return freeProperties.entrySet().stream().allMatch(p -> p.getValue().contains(identifiable.getProperty(p.getKey())));
    }

    private static boolean freePropertiesFilter(Terminal terminal, Map<String, List<String>> propertiesWithValues) {
        Optional<Substation> optSubstation = terminal.getVoltageLevel().getSubstation();
        return optSubstation.filter(substation -> freePropertiesFilter(substation, propertiesWithValues)).isPresent();
    }

    private static boolean countryFilter(Terminal terminal, Set<String> countries) {
        Optional<Country> country = terminal.getVoltageLevel().getSubstation().flatMap(Substation::getCountry);
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private static boolean countryFilter(VoltageLevel voltageLevel, Set<String> countries) {
        Optional<Country> country = voltageLevel.getSubstation().flatMap(Substation::getCountry);
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private static boolean countryFilter(Substation substation, Set<String> countries) {
        Optional<Country> country = substation.getCountry();
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private static boolean freePropertiesFilter(Substation substation, Map<String, List<String>> propertiesWithValues) {
        return FiltersUtils.matchesFreeProps(propertiesWithValues, substation);
    }

    private static boolean freePropertiesFilter(Identifiable<?> identifiable, Map<String, List<String>> propertiesWithValues) {
        return FiltersUtils.matchesFreeProps(propertiesWithValues, identifiable);
    }

    private static boolean equipmentIdFilter(Identifiable<?> identifiable, String equipmentId) {
        return equipmentId == null || identifiable.getId().equals(equipmentId);
    }

    private static boolean equipmentNameFilter(Identifiable<?> identifiable, String equipmentName) {
        return equipmentName == null || identifiable.getNameOrId().equals(equipmentName);
    }

    private static boolean substationNameFilter(Terminal terminal, String substationName) {
        return substationName == null || terminal.getVoltageLevel().getSubstation().map(s -> s.getNameOrId().equals(substationName)).orElse(Boolean.TRUE);
    }

    private static boolean filterByVoltage(double equipmentNominalVoltage, NumericalFilter numericalFilter) {
        if (numericalFilter == null) {
            return true;
        }
        switch (numericalFilter.getType()) {
            case EQUALITY:
                return equipmentNominalVoltage == numericalFilter.getValue1();
            case GREATER_THAN:
                return equipmentNominalVoltage > numericalFilter.getValue1();
            case GREATER_OR_EQUAL:
                return equipmentNominalVoltage >= numericalFilter.getValue1();
            case LESS_THAN:
                return equipmentNominalVoltage < numericalFilter.getValue1();
            case LESS_OR_EQUAL:
                return equipmentNominalVoltage <= numericalFilter.getValue1();
            case RANGE:
                return equipmentNominalVoltage >= numericalFilter.getValue1() && equipmentNominalVoltage <= numericalFilter.getValue2();
            default:
                throw new PowsyblException("Unknown numerical filter type");
        }
    }

    private static boolean filterByCountries(Terminal terminal1, Terminal terminal2, Set<String> filter1, Set<String> filter2) {
        return
            // terminal 1 matches filter 1 and terminal 2 matches filter 2
            countryFilter(terminal1, filter1) &&
                countryFilter(terminal2, filter2)
                || // or the opposite
                countryFilter(terminal1, filter2) &&
                    countryFilter(terminal2, filter1);
    }

    private static boolean filterByProperties(Terminal terminal1, Terminal terminal2,
                                       Map<String, List<String>> freeProperties1, Map<String, List<String>> freeProperties2) {
        return freePropertiesFilter(terminal1, freeProperties1) &&
            freePropertiesFilter(terminal2, freeProperties2)
            || freePropertiesFilter(terminal1, freeProperties2) &&
            freePropertiesFilter(terminal2, freeProperties1);
    }

    private static boolean filterByVoltage(Terminal terminal, NumericalFilter numericalFilter) {
        return filterByVoltage(terminal.getVoltageLevel(), numericalFilter);
    }

    private static boolean filterByVoltage(VoltageLevel voltageLevel, NumericalFilter numericalFilter) {
        return filterByVoltage(voltageLevel.getNominalV(), numericalFilter);
    }

    private static boolean filterByVoltages(Branch<?> branch, NumericalFilter numFilter1, NumericalFilter numFilter2) {
        return
            // terminal 1 matches filter 1 and terminal 2 matches filter 2
            filterByVoltage(branch.getTerminal1(), numFilter1) &&
                filterByVoltage(branch.getTerminal2(), numFilter2)
                || // or the opposite
                filterByVoltage(branch.getTerminal1(), numFilter2) &&
                    filterByVoltage(branch.getTerminal2(), numFilter1);
    }

    private static boolean filterByVoltages(ThreeWindingsTransformer transformer, ThreeWindingsTransformerFilter filter) {
        return
            // leg 1 matches filter 1, leg 2 matches filter 2, and leg 3 filter 3
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage1()) &&
                filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage2()) &&
                filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage3())
                // or any other combination :
                || // keep leg1 on filter 1, switch legs 2/3
                filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage1()) &&
                    filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage2()) &&
                    filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage3())
                || // now leg2 matches filter 1
                filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage1()) &&
                    filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage2()) &&
                    filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage3())
                || // keep leg2 on filter 1, switch legs 1/3
                filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage1()) &&
                    filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage2()) &&
                    filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage3())
                || // now leg3 matches filter 1
                filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage1()) &&
                    filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage2()) &&
                    filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage3())
                || // keep leg3 on filter 1, switch legs 1/2
                filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage1()) &&
                    filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage2()) &&
                    filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage3());
    }

    private static List<String> getIdentifierListFilterEquipmentIds(IdentifierListFilter identifierListFilter) {
        return identifierListFilter.getFilterEquipmentsAttributes()
            .stream()
            .map(IdentifierListFilterEquipmentAttributes::getEquipmentID)
            .toList();
    }

    private static boolean filterByEnergySource(Generator generator, EnergySource energySource) {
        return energySource == null || generator.getEnergySource() == energySource;
    }

    private static boolean filterByProperties(Line line, LineFilter lineFilter) {
        return filterByProperties(line.getTerminal1(), line.getTerminal2(), lineFilter.getFreeProperties1(), lineFilter.getFreeProperties2());
    }

    private static boolean filterByCountries(Line line, LineFilter filter) {
        return filterByCountries(line.getTerminal1(), line.getTerminal2(), filter.getCountries1(), filter.getCountries2());
    }

    private static boolean filterByProperties(HvdcLine line, HvdcLineFilter filter) {
        return filterByProperties(line.getConverterStation1().getTerminal(), line.getConverterStation2().getTerminal(),
            filter.getFreeProperties1(), filter.getFreeProperties2());
    }

    private static boolean filterByCountries(HvdcLine line, HvdcLineFilter filter) {
        return filterByCountries(line.getConverterStation1().getTerminal(), line.getConverterStation2().getTerminal(), filter.getCountries1(), filter.getCountries2());
    }

    private static <I extends Injection<I>> Stream<Injection<I>> getInjectionList(Stream<Injection<I>> stream, AbstractFilter filter, FilterLoader filterLoader) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            AbstractInjectionFilter injectionFilter = (AbstractInjectionFilter) criteriaFilter.getEquipmentFilterForm();
            return stream
                .filter(injection -> equipmentIdFilter(injection, injectionFilter.getEquipmentID()))
                .filter(injection -> equipmentNameFilter(injection, injectionFilter.getEquipmentName()))
                .filter(injection -> freePropertiesFilter(injection, injectionFilter.getFreeProperties()))
                .filter(injection -> filterByVoltage(injection.getTerminal().getVoltageLevel().getNominalV(), injectionFilter.getNominalVoltage()))
                .filter(injection -> countryFilter(injection.getTerminal(), injectionFilter.getCountries()))
                .filter(injection -> substationNameFilter(injection.getTerminal(), injectionFilter.getSubstationName()))
                .filter(injection -> freePropertiesFilter(injection.getTerminal(), injectionFilter.getSubstationFreeProperties()));
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            return stream.filter(injection -> equipmentIds.contains(injection.getId()));
        } else if (filter instanceof ExpertFilter expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            return stream.filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
        } else {
            return Stream.empty();
        }
    }

    private static List<Identifiable<?>> getGeneratorList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            GeneratorFilter generatorFilter = (GeneratorFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<Injection<Generator>> stream = getInjectionList(network.getGeneratorStream().map(injection -> injection), filter, filterLoader)
                .filter(injection -> filterByEnergySource((Generator) injection, generatorFilter.getEnergySource()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter || filter instanceof ExpertFilter) {
            Stream<Injection<Generator>> stream = getInjectionList(network.getGeneratorStream().map(generator -> generator), filter, filterLoader);
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getLoadList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<Load>> stream = getInjectionList(network.getLoadStream().map(load -> load), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getBatteryList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<Battery>> stream = getInjectionList(network.getBatteryStream().map(battery -> battery), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getStaticVarCompensatorList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<StaticVarCompensator>> stream = getInjectionList(network.getStaticVarCompensatorStream().map(svc -> svc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getShuntCompensatorList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<ShuntCompensator>> stream = getInjectionList(network.getShuntCompensatorStream().map(sc -> sc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getDanglingLineList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<DanglingLine>> stream = getInjectionList(network.getDanglingLineStream().map(dl -> dl), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getLccConverterStationList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<LccConverterStation>> stream = getInjectionList(network.getLccConverterStationStream().map(lcc -> lcc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getVscConverterStationList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<VscConverterStation>> stream = getInjectionList(network.getVscConverterStationStream().map(vsc -> vsc), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getBusList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        if (filter instanceof ExpertFilter expertFilter) {
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

    private static List<Identifiable<?>> getBusbarSectionList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        Stream<Injection<BusbarSection>> stream = getInjectionList(network.getBusbarSectionStream().map(bbs -> bbs), filter, filterLoader);
        return new ArrayList<>(stream.toList());
    }

    private static List<Identifiable<?>> getLineList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            LineFilter lineFilter = (LineFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<Line> stream = network.getLineStream()
                .filter(line -> equipmentIdFilter(line, lineFilter.getEquipmentID()))
                .filter(line -> equipmentNameFilter(line, lineFilter.getEquipmentName()))
                .filter(line -> filterByVoltages(line, lineFilter.getNominalVoltage1(), lineFilter.getNominalVoltage2()))
                .filter(line -> filterByCountries(line, lineFilter))
                .filter(line -> filterByProperties(line, lineFilter))
                .filter(line -> substationNameFilter(line.getTerminal1(), lineFilter.getSubstationName1()) &&
                    substationNameFilter(line.getTerminal2(), lineFilter.getSubstationName2()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<Line> stream = network.getLineStream()
                .filter(line -> equipmentIds.contains(line.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilter expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<Line> stream = network.getLineStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> get2WTransformerList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            TwoWindingsTransformerFilter twoWindingsTransformerFilter = (TwoWindingsTransformerFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<TwoWindingsTransformer> stream = network.getTwoWindingsTransformerStream()
                .filter(twoWindingsTransformer -> equipmentIdFilter(twoWindingsTransformer, twoWindingsTransformerFilter.getEquipmentID()))
                .filter(twoWindingsTransformer -> equipmentNameFilter(twoWindingsTransformer, twoWindingsTransformerFilter.getEquipmentName()))
                .filter(twoWindingsTransformer -> filterByVoltages(twoWindingsTransformer, twoWindingsTransformerFilter.getNominalVoltage1(), twoWindingsTransformerFilter.getNominalVoltage2()))
                .filter(twoWindingsTransformer -> countryFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getCountries()) ||
                    countryFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getCountries()))
                .filter(twoWindingsTransformer -> freePropertiesFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getSubstationFreeProperties()) ||
                    freePropertiesFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getSubstationFreeProperties()))
                .filter(twoWindingsTransformer -> substationNameFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getSubstationName()) ||
                    substationNameFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getSubstationName()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<TwoWindingsTransformer> stream = network.getTwoWindingsTransformerStream()
                .filter(twoWindingsTransformer -> equipmentIds.contains(twoWindingsTransformer.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilter expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<TwoWindingsTransformer> stream = network.getTwoWindingsTransformerStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> get3WTransformerList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<ThreeWindingsTransformer> stream = network.getThreeWindingsTransformerStream()
                .filter(threeWindingsTransformer -> equipmentIdFilter(threeWindingsTransformer, threeWindingsTransformerFilter.getEquipmentID()))
                .filter(threeWindingsTransformer -> equipmentNameFilter(threeWindingsTransformer, threeWindingsTransformerFilter.getEquipmentName()))
                .filter(threeWindingsTransformer -> filterByVoltages(threeWindingsTransformer, threeWindingsTransformerFilter))
                .filter(threeWindingsTransformer -> countryFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getCountries()) ||
                    countryFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getCountries()) ||
                    countryFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getCountries()))
                .filter(threeWindingsTransformer -> freePropertiesFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getSubstationFreeProperties()) ||
                    freePropertiesFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getSubstationFreeProperties()) ||
                    freePropertiesFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getSubstationFreeProperties()))
                .filter(threeWindingsTransformer -> substationNameFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getSubstationName()) ||
                    substationNameFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getSubstationName()) ||
                    substationNameFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getSubstationName()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<ThreeWindingsTransformer> stream = network.getThreeWindingsTransformerStream()
                .filter(threeWindingsTransformer -> equipmentIds.contains(threeWindingsTransformer.getId()));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getHvdcList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<HvdcLine> stream = network.getHvdcLineStream()
                .filter(hvdcLine -> equipmentIdFilter(hvdcLine, hvdcLineFilter.getEquipmentID()))
                .filter(hvdcLine -> equipmentNameFilter(hvdcLine, hvdcLineFilter.getEquipmentName()))
                .filter(hvdcLine -> filterByVoltage(hvdcLine.getNominalV(), hvdcLineFilter.getNominalVoltage()))
                .filter(hvdcLine -> filterByCountries(hvdcLine, hvdcLineFilter))
                .filter(hvdcLine -> filterByProperties(hvdcLine, hvdcLineFilter))
                .filter(hvdcLine -> substationNameFilter(hvdcLine.getConverterStation1().getTerminal(), hvdcLineFilter.getSubstationName1()) &&
                    substationNameFilter(hvdcLine.getConverterStation2().getTerminal(), hvdcLineFilter.getSubstationName2()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentsIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<HvdcLine> stream = network.getHvdcLineStream()
                .filter(hvdcLine -> equipmentsIds.contains(hvdcLine.getId()));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getVoltageLevelList(Network network, AbstractFilter filter, FilterLoader filterloader) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            VoltageLevelFilter voltageLevelFilter = (VoltageLevelFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<VoltageLevel> stream = network.getVoltageLevelStream()
                .filter(voltageLevel -> equipmentIdFilter(voltageLevel, voltageLevelFilter.getEquipmentID()))
                .filter(voltageLevel -> equipmentNameFilter(voltageLevel, voltageLevelFilter.getEquipmentName()))
                .filter(voltageLevel -> filterByVoltage(voltageLevel, voltageLevelFilter.getNominalVoltage()))
                .filter(voltageLevel -> countryFilter(voltageLevel, voltageLevelFilter.getCountries()))
                .filter(voltageLevel -> freePropertiesFilter(voltageLevel.getNullableSubstation(), voltageLevelFilter.getSubstationFreeProperties()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<VoltageLevel> stream = network.getVoltageLevelStream()
                .filter(voltageLevel -> equipmentIds.contains(voltageLevel.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilter expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<VoltageLevel> stream = network.getVoltageLevelStream()
                .filter(ident -> rule.evaluateRule(ident, filterloader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    private static List<Identifiable<?>> getSubstationList(Network network, AbstractFilter filter, FilterLoader filterLoader) {
        if (filter instanceof CriteriaFilter criteriaFilter) {
            SubstationFilter substationFilter = (SubstationFilter) criteriaFilter.getEquipmentFilterForm();
            Stream<Substation> stream = network.getSubstationStream()
                .filter(substation -> equipmentIdFilter(substation, substationFilter.getEquipmentID()))
                .filter(substation -> equipmentNameFilter(substation, substationFilter.getEquipmentName()))
                .filter(substation -> countryFilter(substation, substationFilter.getCountries()))
                .filter(substation -> freePropertiesFilter(substation, substationFilter.getFreeProperties()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof IdentifierListFilter identifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds(identifierListFilter);
            Stream<Substation> stream = network.getSubstationStream()
                .filter(substation -> equipmentIds.contains(substation.getId()));
            return new ArrayList<>(stream.toList());
        } else if (filter instanceof ExpertFilter expertFilter) {
            var rule = expertFilter.getRules();
            Map<UUID, FilterEquipments> cachedUuidFilters = new HashMap<>();
            Stream<Substation> stream = network.getSubstationStream()
                .filter(ident -> rule.evaluateRule(ident, filterLoader, cachedUuidFilters));
            return new ArrayList<>(stream.toList());
        } else {
            return List.of();
        }
    }

    public static List<Identifiable<?>> getIdentifiables(AbstractFilter filter, Network network, FilterLoader filterLoader) {
        List<Identifiable<?>> identifiables;
        switch (filter.getEquipmentType()) {
            case GENERATOR:
                identifiables = getGeneratorList(network, filter, filterLoader);
                break;
            case LOAD:
                identifiables = getLoadList(network, filter, filterLoader);
                break;
            case BATTERY:
                identifiables = getBatteryList(network, filter, filterLoader);
                break;
            case STATIC_VAR_COMPENSATOR:
                identifiables = getStaticVarCompensatorList(network, filter, filterLoader);
                break;
            case SHUNT_COMPENSATOR:
                identifiables = getShuntCompensatorList(network, filter, filterLoader);
                break;
            case LCC_CONVERTER_STATION:
                identifiables = getLccConverterStationList(network, filter, filterLoader);
                break;
            case VSC_CONVERTER_STATION:
                identifiables = getVscConverterStationList(network, filter, filterLoader);
                break;
            case HVDC_LINE:
                identifiables = getHvdcList(network, filter);
                break;
            case DANGLING_LINE:
                identifiables = getDanglingLineList(network, filter, filterLoader);
                break;
            case LINE:
                identifiables = getLineList(network, filter, filterLoader);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                identifiables = get2WTransformerList(network, filter, filterLoader);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                identifiables = get3WTransformerList(network, filter);
                break;
            case BUS:
                identifiables = getBusList(network, filter, filterLoader);
                break;
            case BUSBAR_SECTION:
                identifiables = getBusbarSectionList(network, filter, filterLoader);
                break;
            case VOLTAGE_LEVEL:
                identifiables = getVoltageLevelList(network, filter, filterLoader);
                break;
            case SUBSTATION:
                identifiables = getSubstationList(network, filter, filterLoader);
                break;
            default:
                throw new PowsyblException("Unknown equipment type");
        }
        return identifiables;
    }
}
