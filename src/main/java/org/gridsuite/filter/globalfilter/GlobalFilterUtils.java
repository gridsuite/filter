package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
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

public final class GlobalFilterUtils {
    private GlobalFilterUtils() {
        throw new IllegalCallerException("Utility class should not be instantiated");
    }

    /** @see ExpertFilterUtils#getFieldValue(FieldType, String, Identifiable) for possible values */
    @Nonnull
    public static List<FieldType> getNominalVoltageFieldType(@Nonnull final EquipmentType equipmentType) {
        return switch (equipmentType) {
            case LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER -> List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2);
            case BATTERY, BUS, BUSBAR_SECTION, GENERATOR, LOAD, SHUNT_COMPENSATOR, VOLTAGE_LEVEL -> List.of(FieldType.NOMINAL_VOLTAGE);
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
                 THREE_WINDINGS_TRANSFORMER, TWO_WINDINGS_TRANSFORMER, VOLTAGE_LEVEL -> List.of(FieldType.COUNTRY);
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

    /**
     * Builds expert filter from a {@link GlobalFilter global filter} for an {@link EquipmentType equipment type}.
     */
    @Nullable
    public static ExpertFilter buildExpertFilter(@Nonnull final GlobalFilter globalFilter, @Nonnull final EquipmentType equipmentType) {
        final List<AbstractExpertRule> andRules = new ArrayList<>();
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
        return andRules.isEmpty() ? null : new ExpertFilter(UuidUtils.generateUUID(), TimeUtils.nowAsDate(), equipmentType,
            CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules).build());
    }

    @Nonnull
    private static List<String> filterNetwork(@Nonnull final AbstractFilter filter, @Nonnull final Network network, @Nonnull final FilterLoader filterLoader) {
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
                                                          @Nonnull final GlobalFilter globalFilter, @Nonnull final List<AbstractFilter> genericFilters,
                                                          @Nonnull final EquipmentType equipmentType, @Nonnull final FilterLoader filterLoader) {
        List<List<String>> allFilterResults = new ArrayList<>(1 + genericFilters.size());

        // Extract IDs from expert filter
        final ExpertFilter expertFilter = buildExpertFilter(globalFilter, equipmentType);
        if (expertFilter != null) {
            allFilterResults.add(filterNetwork(expertFilter, network, filterLoader));
        }

        // Extract IDs from generic filters
        for (final AbstractFilter filter : genericFilters) {
            final List<String> filterResult = applyFilterOnNetwork(filter, equipmentType, network, filterLoader);
            if (!filterResult.isEmpty()) {
                allFilterResults.add(filterResult);
            }
        }

        // Combine results with appropriate logic
        // Expert filters use OR between them, generic filters use AND
        return FiltersUtils.combineFilterResults(allFilterResults, !genericFilters.isEmpty());
    }

    /**
     * Filters equipments by {@link EquipmentType type}
     * @return map of {@link Identifiable#getId() equipment ID}s grouped by {@link EquipmentType equipment type}
     */
    @Nonnull
    public static Map<EquipmentType, List<String>> applyGlobalFilterOnNetwork(@Nonnull final Network network,
            @Nonnull final GlobalFilter globalFilter, @Nonnull final List<AbstractFilter> genericFilters,
            @Nonnull final List<EquipmentType> equipmentTypes, @Nonnull final FilterLoader filterLoader) {
        Map<EquipmentType, List<String>> result = new EnumMap<>(EquipmentType.class);
        for (final EquipmentType equipmentType : equipmentTypes) {
            final List<String> filteredIds = applyGlobalFilterOnNetwork(network, globalFilter, genericFilters, equipmentType, filterLoader);
            if (!filteredIds.isEmpty()) {
                result.put(equipmentType, filteredIds);
            }
        }
        return result;
    }
}
