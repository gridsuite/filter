package org.gridsuite.filter.globalfilter;

import com.google.common.annotations.VisibleForTesting;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.*;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FiltersUtils;
import org.gridsuite.filter.utils.TimeUtils;
import org.gridsuite.filter.utils.UuidUtils;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class GlobalFilterUtils {
    private GlobalFilterUtils() {
        throw new IllegalCallerException("Utility class should not be instantiated");
    }

    /** @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values */
    @Nonnull
    public static List<FieldType> getNominalVoltageFieldType(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case LINE, TWO_WINDINGS_TRANSFORMER, HVDC_LINE -> List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2);
            case THREE_WINDINGS_TRANSFORMER -> List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2, FieldType.NOMINAL_VOLTAGE_3);
            case BATTERY, BUS, BUSBAR_SECTION, GENERATOR, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, VOLTAGE_LEVEL, DANGLING_LINE, LCC_CONVERTER_STATION, VSC_CONVERTER_STATION -> List.of(FieldType.NOMINAL_VOLTAGE);
            default -> List.of();
        };
    }

    /**
     * Builds nominal voltage rules combining all relevant field types
     * @see GlobalFilter#getNominalV()
     */
    @Nonnull
    public static Optional<AbstractExpertRule> buildNominalVoltageRules(
            @Nonnull final List<String> nominalVoltages, @Nonnull final EquipmentType equipmentType) {
        final List<FieldType> fields = getNominalVoltageFieldType(equipmentType);
        return ExpertFilterUtils.buildOrCombination(nominalVoltages.stream()
            .filter(Predicate.not(String::isBlank))
            .map(Double::valueOf)
            .<AbstractExpertRule>mapMulti((value, accumulator) -> {
                for (final FieldType field : fields) {
                    accumulator.accept(NumberExpertRule.builder()
                        .value(value)
                        .field(field)
                        .operator(OperatorType.EQUALS)
                        .build());
                }
            }).toList());
    }

    /** @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values */
    @Nonnull
    public static List<FieldType> getCountryCodeFieldType(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case BATTERY, BUS, BUSBAR_SECTION, DANGLING_LINE, GENERATOR, LOAD,
                 SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, SUBSTATION,
                 THREE_WINDINGS_TRANSFORMER, TWO_WINDINGS_TRANSFORMER, VOLTAGE_LEVEL, LCC_CONVERTER_STATION, VSC_CONVERTER_STATION -> List.of(FieldType.COUNTRY);
            case LINE, HVDC_LINE -> List.of(FieldType.COUNTRY_1, FieldType.COUNTRY_2);
            default -> List.of();
        };
    }

    /**
     * Builds country code rules combining all relevant field types
     */
    @Nonnull
    public static Optional<AbstractExpertRule> buildCountryCodeRules(
            @Nonnull final List<Country> countryCodes, @Nonnull final EquipmentType equipmentType) {
        final List<FieldType> fields = getCountryCodeFieldType(equipmentType);
        return ExpertFilterUtils.buildOrCombination(countryCodes.stream()
            .map(Country::name)
            .<AbstractExpertRule>mapMulti((countryCode, accumulator) -> {
                for (final FieldType field : fields) {
                    accumulator.accept(EnumExpertRule.builder()
                        .value(countryCode)
                        .field(field)
                        .operator(OperatorType.EQUALS)
                        .build());
                }
            })
        .toList());
    }

    /** @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values */
    @Nonnull
    public static List<FieldType> getSubstationPropertiesFieldTypes(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case LINE, HVDC_LINE -> List.of(FieldType.SUBSTATION_PROPERTIES_1, FieldType.SUBSTATION_PROPERTIES_2);
            case SUBSTATION -> List.of(FieldType.FREE_PROPERTIES);
            default -> List.of(FieldType.SUBSTATION_PROPERTIES);
        };
    }

    /**
     * Builds substation property rules combining all relevant field types
     */
    @Nonnull
    public static Optional<AbstractExpertRule> buildSubstationPropertyRules(
            @Nonnull final Map<String, List<String>> properties, @Nonnull final EquipmentType equipmentType) {
        final List<FieldType> fields = getSubstationPropertiesFieldTypes(equipmentType);
        return ExpertFilterUtils.buildOrCombination(properties.entrySet()
            .stream()
            .<AbstractExpertRule>mapMulti((entry, accumulator) -> {
                for (final FieldType field : fields) {
                    accumulator.accept(PropertiesExpertRule.builder()
                        .combinator(CombinatorType.OR)
                        .operator(OperatorType.IN)
                        .field(field)
                        .propertyName(entry.getKey())
                        .propertyValues(entry.getValue())
                        .build());
                }
            })
        .toList());
    }

    public static AbstractExpertRule createFilterBasedRule(List<AbstractFilter> filters, Set<FieldType> fieldTypes) {
        if (!filters.isEmpty()) {
            return ExpertFilterUtils.buildOrCombination(fieldTypes.stream()
                .map(field -> FilterUuidExpertRule.builder()
                    .field(field)
                    .operator(OperatorType.IS_PART_OF)
                    .values(new HashSet<>(filters.stream().map(filter -> filter.getId().toString()).toList()))
                    .build())
                .collect(Collectors.toUnmodifiableList())).orElseThrow();
        }
        return null;
    }

    /**
     * builds AbstractExpertRule expert rule
     * @param genericFilters the generic filters to build rules for
     * @return {@link AbstractExpertRule expert rules} built from the loaded generic filters.
     */
    public static AbstractExpertRule buildGenericFilterRule(@Nonnull final List<AbstractFilter> genericFilters,
                                                            @Nonnull final EquipmentType actualType) {
        final List<AbstractExpertRule> rules = new ArrayList<>();

        // Create only one OR rule for all filters with same type (matches actualType exclude substation and voltage levels)
        if (actualType != EquipmentType.VOLTAGE_LEVEL && actualType != EquipmentType.SUBSTATION) {
            List<AbstractFilter> typeMatches = genericFilters.stream()
                    .filter(abstractFilter -> abstractFilter.getEquipmentType() == actualType)
                    .toList();

            AbstractExpertRule typeMatchesRule = createFilterBasedRule(typeMatches, Set.of(FieldType.ID));
            if (typeMatchesRule != null) {
                rules.add(typeMatchesRule);
            }
        }

        // Create one rule for substations and voltage levels (combined with Or)
        List<AbstractExpertRule> subsStationsAndVoltageLevelsRules = new ArrayList<>();

        // Create substations rule
        List<AbstractFilter> substations = genericFilters.stream()
            .filter(abstractFilter -> abstractFilter.getEquipmentType() == EquipmentType.SUBSTATION)
            .toList();
        if (!substations.isEmpty()) {
            AbstractExpertRule substationsRule = createFilterBasedRule(substations,
                ExpertFilterUtils.getIdFieldMatchingType(actualType, EquipmentType.SUBSTATION).collect(Collectors.toSet()));
            if (substationsRule != null) {
                subsStationsAndVoltageLevelsRules.add(substationsRule);
            }
        }

        // Create voltage levels rule
        List<AbstractFilter> voltageLevels = genericFilters.stream()
            .filter(abstractFilter -> abstractFilter.getEquipmentType() == EquipmentType.VOLTAGE_LEVEL)
            .toList();
        if (!voltageLevels.isEmpty()) {
            AbstractExpertRule voltageLevelsRule = createFilterBasedRule(voltageLevels,
                ExpertFilterUtils.getIdFieldMatchingType(actualType, EquipmentType.VOLTAGE_LEVEL).collect(Collectors.toSet()));
            if (voltageLevelsRule != null) {
                subsStationsAndVoltageLevelsRules.add(voltageLevelsRule);
            }
        }

        // Combine Substations and voltage levels rules with Or and add it to rules
        if (!subsStationsAndVoltageLevelsRules.isEmpty()) {
            rules.add(ExpertFilterUtils.buildOrCombination(subsStationsAndVoltageLevelsRules).orElseThrow());
        }

        // Create and rule from rules
        return ExpertFilterUtils.buildAndCombination(rules).orElse(null);
    }

    /**
     * Builds expert filter from a {@link GlobalFilter global filter} for an {@link EquipmentType equipment type}.
     */
    @Nullable
    public static ExpertFilter buildExpertFilter(@Nonnull final GlobalFilter globalFilter,
                                                 @Nonnull final EquipmentType equipmentType,
                                                 @Nonnull final List<AbstractFilter> genericFilters) {
        final List<AbstractExpertRule> andRules = new ArrayList<>();

        // Generic filter have a priority on other filter types
        if (!shouldProcessEquipmentType(equipmentType, genericFilters)) {
            return null;
        }

        if (CollectionUtils.isNotEmpty(genericFilters)) {
            AbstractExpertRule genericRule = buildGenericFilterRule(genericFilters, equipmentType);
            if (genericRule != null) {
                andRules.add(genericRule);
            }
        }

        if (globalFilter.getNominalV() != null) {
            buildNominalVoltageRules(globalFilter.getNominalV(), equipmentType).ifPresent(andRules::add);
        }
        if (globalFilter.getCountryCode() != null) {
            buildCountryCodeRules(globalFilter.getCountryCode(), equipmentType).ifPresent(andRules::add);
        }
        if (globalFilter.getSubstationProperty() != null) {
            // custom extension with apps-metadata server
            buildSubstationPropertyRules(globalFilter.getSubstationProperty(), equipmentType).ifPresent(andRules::add);
        }

        return ExpertFilterUtils.buildAndCombination(andRules)
                .map(rule -> new ExpertFilter(UuidUtils.generateUUID(), TimeUtils.nowAsDate(), equipmentType, rule))
                .orElse(null);
    }

    @VisibleForTesting
    @Nonnull
    static List<String> filterNetwork(@Nonnull final AbstractFilter filter, @Nonnull final Network network, @Nonnull final FilterLoader filterLoader) {
        return FiltersUtils.getIdentifiables(filter, network, filterLoader)
            .stream()
            .map(Identifiable::getId)
            .toList();
    }

    /**
     * Extracts {@link Identifiable#getId() equipment ID}s from a generic filter based on {@link EquipmentType equipment type}.
     */
    @Nonnull
    public static List<String> applyFilterOnNetwork(@Nonnull final AbstractFilter filter, @Nonnull final EquipmentType targetEquipmentType,
                                                    @Nonnull final Network network, @Nonnull final FilterLoader filterLoader) {
        if (filter.getEquipmentType() == targetEquipmentType) {
            return filterNetwork(filter, network, filterLoader);
        } else if (filter.getEquipmentType() == EquipmentType.VOLTAGE_LEVEL) {
            return filterNetwork(ExpertFilterUtils.buildExpertFilterWithVoltageLevelIdsCriteria(filter.getId(), targetEquipmentType), network, filterLoader);
        }
        return List.of();
    }

    /**
     * Extracts filtered {@link Identifiable#getId() equipment ID}s by applying {@link ExpertFilter expert}
     * and {@link AbstractFilter generic filter}s.
     */
    @Nonnull
    public static List<String> applyGlobalFilterOnNetwork(@Nonnull final Network network,
                                                          @Nonnull final GlobalFilter globalFilter,
                                                          @Nonnull final EquipmentType equipmentType,
                                                          final List<AbstractFilter> genericFilters,
                                                          @Nonnull final FilterLoader filterLoader) {
        List<String> allFilterResults = null;

        // Extract IDs from expert filter
        final ExpertFilter expertFilter = buildExpertFilter(globalFilter, equipmentType, genericFilters);
        if (expertFilter != null) {
            allFilterResults = filterNetwork(expertFilter, network, filterLoader);
        }

        // return filters List
        return allFilterResults != null ? allFilterResults : List.of();
    }

    /**
     * When we are filtering on several equipment types, and we have generic filters,
     * we consider only equipment types that have one or more generic filter. The other types are excluded.
     * Substation and Voltage level types include all equipment types, only if there is no other equipment
     * type filters
     * @param equipmentType : equipment type that should be processed
     * @param genericFilters : generic filters list
     * **/
    public static boolean shouldProcessEquipmentType(@Nonnull final EquipmentType equipmentType,
                                                     @Nonnull final List<AbstractFilter>genericFilters) {

        // The current equipment type will be process IF
        // list genericFilters is empty
        if (CollectionUtils.isEmpty(genericFilters)) {
            return true;
        } else {
            // OR (list genericFilters is not empty AND
            // (it is present in genericFilters OR there isn't any other equipment type in genericFilters))

            // all effective equipment types in genericFilters
            List<EquipmentType> equipmentTypesInGenericFilters = genericFilters.stream()
                    .map(AbstractFilter::getEquipmentType)
                    .filter(elem ->
                            elem != EquipmentType.SUBSTATION &&
                            elem != EquipmentType.VOLTAGE_LEVEL)
                    .distinct().toList();
            return equipmentTypesInGenericFilters.contains(equipmentType) ||
                   CollectionUtils.isEmpty(equipmentTypesInGenericFilters);
        }
    }

    /**
     * Filters equipments by {@link EquipmentType type}
     * @return map of {@link Identifiable#getId() equipment ID}s grouped by {@link EquipmentType equipment type}
     */
    @Nonnull
    public static Map<EquipmentType, List<String>> applyGlobalFilterOnNetwork(@Nonnull final Network network,
                                                                              @Nonnull final GlobalFilter globalFilter,
                                                                              @Nonnull final List<EquipmentType> equipmentTypes,
                                                                              @Nonnull final FilterLoader filterLoader) {
        Map<EquipmentType, List<String>> result = new EnumMap<>(EquipmentType.class);

        List<AbstractFilter> genericFilters = null;
        if (CollectionUtils.isNotEmpty(globalFilter.getGenericFilter())) {
            genericFilters = filterLoader.getFilters(globalFilter.getGenericFilter());
        }

        for (final EquipmentType equipmentType : equipmentTypes) {
            final List<String> filteredIds = applyGlobalFilterOnNetwork(network, globalFilter, equipmentType, genericFilters, filterLoader);
            if (!filteredIds.isEmpty()) {
                result.put(equipmentType, filteredIds);
            }
        }
        return result;
    }
}
