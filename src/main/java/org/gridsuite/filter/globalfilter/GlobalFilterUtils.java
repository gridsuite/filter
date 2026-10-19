package org.gridsuite.filter.globalfilter;

import com.google.common.annotations.VisibleForTesting;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.ExpertFilter;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.rule.*;

import java.util.*;
import java.util.stream.Collectors;

public final class GlobalFilterUtils {
    private GlobalFilterUtils() {
        throw new IllegalCallerException("Utility class should not be instantiated");
    }

    /**
     * @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values
     */
    @VisibleForTesting
    @Nonnull
    static List<FieldType> getNominalVoltageFieldType(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case LINE, TWO_WINDINGS_TRANSFORMER -> List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2);
            case HVDC_LINE -> List.of(FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2);
            case THREE_WINDINGS_TRANSFORMER -> List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2, FieldType.NOMINAL_VOLTAGE_3);
            case BATTERY, BUS, BUSBAR_SECTION, GENERATOR, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR,
                 VOLTAGE_LEVEL, BOUNDARY_LINE, LCC_CONVERTER_STATION,
                 VSC_CONVERTER_STATION -> List.of(FieldType.NOMINAL_VOLTAGE);
            default -> List.of();
        };
    }

    /**
     * Builds nominal voltage rules combining all relevant field types
     *
     * @see GlobalFilter#getVoltageRanges()
     */
    @VisibleForTesting
    @Nonnull
    static Optional<ExpertRule> buildNominalVoltageRules(
            @Nonnull final List<List<Integer>> voltageRanges, @Nonnull final EquipmentType equipmentType) {
        final List<FieldType> fields = getNominalVoltageFieldType(equipmentType);
        return ExpertFilterUtils.buildOrCombination(voltageRanges.stream()
                .filter(CollectionUtils::isNotEmpty)
                .<ExpertRule>mapMulti((range, accumulator) -> {
                    for (final FieldType field : fields) {
                        accumulator.accept(NumberExpertRule.builder()
                                .values(range.stream().map(Integer::doubleValue).toList())
                                .field(field)
                                .operator(OperatorType.BETWEEN)
                                .build());
                    }
                }).toList());
    }

    /**
     * @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values
     */
    @VisibleForTesting
    @Nonnull
    static List<FieldType> getCountryCodeFieldType(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case BATTERY, BUS, BUSBAR_SECTION, BOUNDARY_LINE, GENERATOR, LOAD,
                 SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, SUBSTATION,
                 THREE_WINDINGS_TRANSFORMER, TWO_WINDINGS_TRANSFORMER, VOLTAGE_LEVEL, LCC_CONVERTER_STATION,
                 VSC_CONVERTER_STATION -> List.of(FieldType.COUNTRY);
            case LINE, HVDC_LINE -> List.of(FieldType.COUNTRY_1, FieldType.COUNTRY_2);
        };
    }

    /**
     * Builds country code rules combining all relevant field types
     */
    @VisibleForTesting
    @Nonnull
    static Optional<ExpertRule> buildCountryCodeRules(
            @Nonnull final List<Country> countryCodes, @Nonnull final EquipmentType equipmentType) {
        final List<FieldType> fields = getCountryCodeFieldType(equipmentType);
        return ExpertFilterUtils.buildOrCombination(countryCodes.stream()
                .map(Country::name)
                .<ExpertRule>mapMulti((countryCode, accumulator) -> {
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

    /**
     * @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values
     */
    @VisibleForTesting
    @Nonnull
    static List<FieldType> getSubstationPropertiesFieldTypes(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case LINE, HVDC_LINE -> List.of(FieldType.SUBSTATION_PROPERTIES_1, FieldType.SUBSTATION_PROPERTIES_2);
            case SUBSTATION -> List.of(FieldType.FREE_PROPERTIES);
            default -> List.of(FieldType.SUBSTATION_PROPERTIES);
        };
    }

    /**
     * Builds substation property rules combining all relevant field types
     */
    @VisibleForTesting
    @Nonnull
    static Optional<ExpertRule> buildSubstationPropertyRules(
            @Nonnull final Map<String, List<String>> properties, @Nonnull final EquipmentType equipmentType) {
        final List<FieldType> fields = getSubstationPropertiesFieldTypes(equipmentType);
        return ExpertFilterUtils.buildOrCombination(properties.entrySet()
                .stream()
                .<ExpertRule>mapMulti((entry, accumulator) -> {
                    for (final FieldType field : fields) {
                        accumulator.accept(PropertiesExpertRule.builder()
                                .operator(OperatorType.IN)
                                .field(field)
                                .propertyName(entry.getKey())
                                .propertyValues(entry.getValue())
                                .build());
                    }
                })
                .toList());
    }

    private static ExpertRule createFilterBasedRule(List<Filter> filters, Set<FieldType> fieldTypes) {
        if (!filters.isEmpty()) {
            return ExpertFilterUtils.buildOrCombination(fieldTypes.stream()
                    .map(field -> FilterExpertRule.builder()
                            .field(field)
                            .operator(OperatorType.IS_PART_OF)
                            .filters(filters)
                            .build())
                    .collect(Collectors.toUnmodifiableList())).orElseThrow();
        }
        return null;
    }

    /**
     * builds ExpertRule expert rule
     *
     * @param genericFilters the generic filters to build rules for
     * @return {@link ExpertRule expert rules} built from the loaded generic filters.
     */
    @VisibleForTesting
    static ExpertRule buildGenericFilterRule(@Nonnull final List<Filter> genericFilters,
                                             @Nonnull final EquipmentType actualType) {
        final List<ExpertRule> rules = new ArrayList<>();

        // Create only one OR rule for all filters with same type
        List<Filter> typeMatches = genericFilters.stream()
                .filter(abstractFilter -> abstractFilter.getEquipmentType() == actualType)
                .toList();

        ExpertRule typeMatchesRule = createFilterBasedRule(typeMatches, Set.of(FieldType.ID));
        if (typeMatchesRule != null) {
            rules.add(typeMatchesRule);
        }

        return ExpertFilterUtils.buildAndCombination(rules).orElse(null);
    }

    @VisibleForTesting
    static ExpertRule buildSubstationOrVoltageLevelFilterRule(@Nonnull final List<Filter> genericFilters,
                                                              @Nonnull final EquipmentType actualType) {
        final List<ExpertRule> rules = new ArrayList<>();

        // Create one rule for substations and voltage levels (combined with Or)
        List<ExpertRule> subsStationsAndVoltageLevelsRules = new ArrayList<>();

        // Create substations rule
        List<Filter> substations = genericFilters.stream()
                .filter(abstractFilter -> abstractFilter.getEquipmentType() == EquipmentType.SUBSTATION)
                .toList();
        if (!substations.isEmpty()) {
            ExpertRule substationsRule = createFilterBasedRule(substations,
                    ExpertFilterUtils.getIdFieldMatchingType(actualType, EquipmentType.SUBSTATION).collect(Collectors.toSet()));
            if (substationsRule != null) {
                subsStationsAndVoltageLevelsRules.add(substationsRule);
            }
        }

        // Create voltage levels rule
        List<Filter> voltageLevels = genericFilters.stream()
                .filter(abstractFilter -> abstractFilter.getEquipmentType() == EquipmentType.VOLTAGE_LEVEL)
                .toList();
        if (!voltageLevels.isEmpty()) {
            ExpertRule voltageLevelsRule = createFilterBasedRule(voltageLevels,
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
                                                 @Nonnull final FilterLoader filterLoader) {
        final List<ExpertRule> andRules = new ArrayList<>();
        List<Filter> genericFilters = filterLoader.getFilters(globalFilter.getGenericFilter()).stream().map(af -> af.toFilter(filterLoader)).toList();
        List<Filter> substationOrVoltageLevelFilters = filterLoader.getFilters(globalFilter.getSubstationOrVoltageLevelFilter()).stream().map(af -> af.toFilter(filterLoader)).toList();

        // Generic filter have a priority on other filter types
        if (!shouldProcessEquipmentType(equipmentType, genericFilters)) {
            return null;
        }

        // substation and voltage levels are handled specifically in buildSubstationOrVoltageLevelFilterRule, they are ignored here
        if (CollectionUtils.isNotEmpty(globalFilter.getGenericFilter()) && equipmentType != EquipmentType.VOLTAGE_LEVEL && equipmentType != EquipmentType.SUBSTATION) {
            ExpertRule genericRule = buildGenericFilterRule(genericFilters, equipmentType);
            if (genericRule != null) {
                andRules.add(genericRule);
            }
        }

        if (CollectionUtils.isNotEmpty(globalFilter.getSubstationOrVoltageLevelFilter())) {
            ExpertRule genericRule = buildSubstationOrVoltageLevelFilterRule(substationOrVoltageLevelFilters, equipmentType);
            if (genericRule != null) {
                andRules.add(genericRule);
            }
        }

        if (CollectionUtils.isNotEmpty(globalFilter.getVoltageRanges())) {
            buildNominalVoltageRules(globalFilter.getVoltageRanges(), equipmentType).ifPresent(andRules::add);
        }
        if (globalFilter.getCountryCode() != null) {
            buildCountryCodeRules(globalFilter.getCountryCode(), equipmentType).ifPresent(andRules::add);
        }
        if (globalFilter.getSubstationProperty() != null) {
            // custom extension with apps-metadata server
            buildSubstationPropertyRules(globalFilter.getSubstationProperty(), equipmentType).ifPresent(andRules::add);
        }

        return ExpertFilterUtils.buildAndCombination(andRules)
                .map(rule -> ExpertFilter.builder().equipmentType(equipmentType).rule(rule).build())
                .orElse(null);
    }

    /**
     * When we are filtering on several equipment types, and we have generic filters,
     * we consider only equipment types that have one or more generic filter. The other types are excluded.
     * Substation and Voltage level types include all equipment types, only if there is no other equipment
     * type filters
     *
     * @param equipmentType  : equipment type that should be processed
     * @param genericFilters : generic filters list
     * @return false if the global filter should not be processed because of the generic filters equipment types
     */
    @VisibleForTesting
    static boolean shouldProcessEquipmentType(@Nonnull final EquipmentType equipmentType,
                                              @Nonnull final List<Filter> genericFilters) {

        // The current equipment type will be process IF
        // list genericFilters is empty
        if (CollectionUtils.isEmpty(genericFilters)) {
            return true;
        } else {
            // OR (list genericFilters is not empty AND
            // (it is present in genericFilters OR there isn't any other equipment type in genericFilters))

            // all effective equipment types in genericFilters
            List<EquipmentType> equipmentTypesInGenericFilters = genericFilters.stream()
                    .map(Filter::getEquipmentType)
                    .filter(elem ->
                            elem != EquipmentType.SUBSTATION &&
                                    elem != EquipmentType.VOLTAGE_LEVEL)
                    .distinct().toList();
            return equipmentTypesInGenericFilters.contains(equipmentType) ||
                    CollectionUtils.isEmpty(equipmentTypesInGenericFilters);
        }
    }
}
