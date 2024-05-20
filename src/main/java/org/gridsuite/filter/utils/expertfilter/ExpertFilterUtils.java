/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.FilterType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class ExpertFilterUtils {

    public static final String FIELD_AND_TYPE_NOT_IMPLEMENTED = "This field and equipment type combination is not implemented with expert filter";

    public static final String TYPE_NOT_IMPLEMENTED = "This equipment type is not implemented with expert filter";

    private ExpertFilterUtils() { }

    public static <I extends Identifiable<I>> String getFieldValue(FieldType field, String propertyName, Identifiable<I> identifiable) {
        return switch (field) {
            case ID -> identifiable.getId();
            case NAME -> identifiable.getOptionalName().orElse(null);
            case FREE_PROPERTIES -> identifiable.getProperty(propertyName);
            default -> switch (identifiable.getType()) {
                case VOLTAGE_LEVEL -> getVoltageLevelFieldValue(field, propertyName, (VoltageLevel) identifiable);
                case LINE -> getLineFieldValue(field, propertyName, (Line) identifiable);
                case GENERATOR -> getGeneratorFieldValue(field, propertyName, (Generator) identifiable);
                case LOAD -> getLoadFieldValue(field, propertyName, (Load) identifiable);
                case SHUNT_COMPENSATOR -> getShuntCompensatorFieldValue(field, propertyName, (ShuntCompensator) identifiable);
                case BUS -> getBusFieldValue(field, (Bus) identifiable);
                case BUSBAR_SECTION -> getBusBarSectionFieldValue(field, (BusbarSection) identifiable);
                case BATTERY -> getBatteryFieldValue(field, propertyName, (Battery) identifiable);
                case SUBSTATION -> getSubstationFieldValue(field, (Substation) identifiable);
                case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformerFieldValue(field, propertyName, (TwoWindingsTransformer) identifiable);
                default -> throw new PowsyblException(TYPE_NOT_IMPLEMENTED + " [" + identifiable.getType() + "]");
            };
        };
    }

    private static String getVoltageLevelFieldValue(FieldType field, String propertyName, VoltageLevel voltageLevel) {
        return switch (field) {
            case COUNTRY,
                COUNTRY_1,
                COUNTRY_2 ->
                    voltageLevel.getSubstation().flatMap(Substation::getCountry).map(String::valueOf).orElse(null);
            case NOMINAL_VOLTAGE,
                NOMINAL_VOLTAGE_1,
                NOMINAL_VOLTAGE_2 -> String.valueOf(voltageLevel.getNominalV());
            case VOLTAGE_LEVEL_ID,
                VOLTAGE_LEVEL_ID_1,
                VOLTAGE_LEVEL_ID_2 -> voltageLevel.getId();
            case LOW_VOLTAGE_LIMIT -> String.valueOf(voltageLevel.getLowVoltageLimit());
            case HIGH_VOLTAGE_LIMIT -> String.valueOf(voltageLevel.getHighVoltageLimit());
            case SUBSTATION_PROPERTIES -> voltageLevel.getNullableSubstation().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + voltageLevel.getType() + "]");
        };
    }

    private static String getLineFieldValue(FieldType field, String propertyName, Line line) {
        return switch (field) {
            case CONNECTED_1 -> getTerminalFieldValue(field, line.getTerminal(TwoSides.ONE));
            case CONNECTED_2 -> getTerminalFieldValue(field, line.getTerminal(TwoSides.TWO));
            case COUNTRY_1,
                VOLTAGE_LEVEL_ID_1,
                NOMINAL_VOLTAGE_1 -> getVoltageLevelFieldValue(field, null, line.getTerminal(TwoSides.ONE).getVoltageLevel());
            case COUNTRY_2,
                VOLTAGE_LEVEL_ID_2,
                NOMINAL_VOLTAGE_2 -> getVoltageLevelFieldValue(field, null, line.getTerminal(TwoSides.TWO).getVoltageLevel());
            case SERIE_RESISTANCE -> String.valueOf(line.getR());
            case SERIE_REACTANCE -> String.valueOf(line.getX());
            case SHUNT_CONDUCTANCE_1 -> String.valueOf(line.getG1());
            case SHUNT_CONDUCTANCE_2 -> String.valueOf(line.getG2());
            case SHUNT_SUSCEPTANCE_1 -> String.valueOf(line.getB1());
            case SHUNT_SUSCEPTANCE_2 -> String.valueOf(line.getB2());
            case SUBSTATION_PROPERTIES_1 -> line.getTerminal1().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case SUBSTATION_PROPERTIES_2 -> line.getTerminal2().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_1 -> line.getTerminal1().getVoltageLevel().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_2 -> line.getTerminal2().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + line.getType() + "]");
        };
    }

    private static String getLoadFieldValue(FieldType field, String propertyName, Load load) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, null, load.getTerminal().getVoltageLevel());
            case P0 -> String.valueOf(load.getP0());
            case Q0 -> String.valueOf(load.getQ0());
            case CONNECTED -> getTerminalFieldValue(field, load.getTerminal());
            case LOAD_TYPE -> load.getLoadType().name();
            case SUBSTATION_PROPERTIES -> load.getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES -> load.getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + load.getType() + "]");
        };
    }

    private static String getShuntCompensatorFieldValue(FieldType field, String propertyName, ShuntCompensator shuntCompensator) {
        return switch (field) {
            case VOLTAGE_LEVEL_ID,
                COUNTRY,
                NOMINAL_VOLTAGE -> getVoltageLevelFieldValue(field, null, shuntCompensator.getTerminal().getVoltageLevel());
            case MAXIMUM_SECTION_COUNT -> String.valueOf(shuntCompensator.getMaximumSectionCount());
            case SECTION_COUNT -> String.valueOf(shuntCompensator.getSectionCount());
            case SHUNT_COMPENSATOR_TYPE,
                MAX_Q_AT_NOMINAL_V,
                SWITCHED_ON_Q_AT_NOMINAL_V,
                MAX_SUSCEPTANCE,
                SWITCHED_ON_SUSCEPTANCE -> getSectionBasedFieldValue(field, shuntCompensator);
            case CONNECTED -> getTerminalFieldValue(field, shuntCompensator.getTerminal());
            case SUBSTATION_PROPERTIES -> shuntCompensator.getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES -> shuntCompensator.getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + shuntCompensator.getType() + "]");
        };
    }

    private static String getGeneratorFieldValue(FieldType field, String propertyName, Generator generator) {
        return switch (field) {
            case ENERGY_SOURCE -> String.valueOf(generator.getEnergySource());
            case MIN_P -> String.valueOf(generator.getMinP());
            case MAX_P -> String.valueOf(generator.getMaxP());
            case TARGET_V -> String.valueOf(generator.getTargetV());
            case TARGET_P -> String.valueOf(generator.getTargetP());
            case TARGET_Q -> String.valueOf(generator.getTargetQ());
            case VOLTAGE_REGULATOR_ON -> String.valueOf(generator.isVoltageRegulatorOn());
            case PLANNED_ACTIVE_POWER_SET_POINT,
                MARGINAL_COST,
                PLANNED_OUTAGE_RATE,
                FORCED_OUTAGE_RATE ->
                getGeneratorStartupField(generator, field);
            case RATED_S -> String.valueOf(generator.getRatedS());
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, null, generator.getTerminal().getVoltageLevel());
            case CONNECTED -> getTerminalFieldValue(field, generator.getTerminal());
            case SUBSTATION_PROPERTIES -> generator.getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES -> generator.getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + generator.getType() + "]");
        };
    }

    @Nonnull
    private static String getGeneratorStartupField(Generator generator, FieldType fieldType) {
        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        if (generatorStartup != null) {
            return String.valueOf(
                switch (fieldType) {
                    case PLANNED_ACTIVE_POWER_SET_POINT -> generatorStartup.getPlannedActivePowerSetpoint();
                    case MARGINAL_COST -> generatorStartup.getMarginalCost();
                    case PLANNED_OUTAGE_RATE -> generatorStartup.getPlannedOutageRate();
                    case FORCED_OUTAGE_RATE -> generatorStartup.getForcedOutageRate();
                    default -> String.valueOf(Double.NaN);
                });
        } else {
            return String.valueOf(Double.NaN);
        }
    }

    private static String getBusFieldValue(FieldType field, Bus bus) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, null, bus.getVoltageLevel());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + bus.getType() + "]");
        };
    }

    private static String getBusBarSectionFieldValue(FieldType field, BusbarSection busbarSection) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, null, busbarSection.getTerminal().getVoltageLevel());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + busbarSection.getType() + "]");
        };
    }

    private static String getTerminalFieldValue(FieldType field, Terminal terminal) {
        return switch (field) {
            case CONNECTED,
                CONNECTED_1,
                CONNECTED_2 -> String.valueOf(terminal.isConnected());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",terminal]");
        };
    }

    private static String getSectionBasedFieldValue(FieldType field, ShuntCompensator shuntCompensator) {
        double susceptancePerSection = shuntCompensator.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        double qAtNominalV = Math.pow(shuntCompensator.getTerminal().getVoltageLevel().getNominalV(), 2) * Math.abs(susceptancePerSection);

        return switch (field) {
            case SHUNT_COMPENSATOR_TYPE -> susceptancePerSection > 0 ? "CAPACITOR" : "REACTOR";
            case MAX_Q_AT_NOMINAL_V -> String.valueOf(qAtNominalV * shuntCompensator.getMaximumSectionCount());
            case SWITCHED_ON_Q_AT_NOMINAL_V -> String.valueOf(qAtNominalV * shuntCompensator.getSectionCount());
            case MAX_SUSCEPTANCE -> String.valueOf(susceptancePerSection * shuntCompensator.getMaximumSectionCount());
            case SWITCHED_ON_SUSCEPTANCE -> String.valueOf(susceptancePerSection * shuntCompensator.getSectionCount());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + shuntCompensator.getType() + "]");
        };
    }

    private static String getBatteryFieldValue(FieldType field, String propertyName, Battery battery) {
        return switch (field) {
            case COUNTRY,
                    NOMINAL_VOLTAGE,
                    VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, null, battery.getTerminal().getVoltageLevel());
            case CONNECTED -> getTerminalFieldValue(field, battery.getTerminal());
            case MIN_P -> String.valueOf(battery.getMinP());
            case MAX_P -> String.valueOf(battery.getMaxP());
            case TARGET_P -> String.valueOf(battery.getTargetP());
            case TARGET_Q -> String.valueOf(battery.getTargetQ());
            case SUBSTATION_PROPERTIES -> battery.getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES -> battery.getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + battery.getType() + "]");

        };
    }

    private static String getSubstationFieldValue(FieldType field, Substation substation) {
        return switch (field) {
            case COUNTRY -> String.valueOf(substation.getCountry().orElse(null));
            default ->
                throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + substation.getType() + "]");
        };
    }

    private static String getRatioTapChangerFieldValue(FieldType field, RatioTapChanger ratioTapChanger) {
        if (ratioTapChanger == null) {
            return null;
        }
        return switch (field) {
            case RATIO_REGULATING -> String.valueOf(ratioTapChanger.isRegulating());
            case RATIO_TARGET_V -> String.valueOf(ratioTapChanger.getTargetV());
            case LOAD_TAP_CHANGING_CAPABILITIES -> String.valueOf(ratioTapChanger.hasLoadTapChangingCapabilities());
            case RATIO_REGULATION_MODE -> ratioTapChanger.getRegulationMode() != null ? ratioTapChanger.getRegulationMode().name() : null;
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",ratioTapChanger]");
        };
    }

    private static String getPhaseTapChangerFieldValue(FieldType field, PhaseTapChanger phaseTapChanger) {
        if (phaseTapChanger == null) {
            return null;
        }
        return switch (field) {
            case PHASE_REGULATING -> String.valueOf(phaseTapChanger.isRegulating());
            case PHASE_REGULATION_VALUE -> String.valueOf(phaseTapChanger.getRegulationValue());
            case PHASE_REGULATION_MODE -> phaseTapChanger.getRegulationMode() != null ? phaseTapChanger.getRegulationMode().name() : null;
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",phaseTapChanger]");
        };
    }

    private static String getTwoWindingsTransformerFieldValue(FieldType field, String propertyName, TwoWindingsTransformer twoWindingsTransformer) {
        return switch (field) {
            case COUNTRY -> twoWindingsTransformer.getSubstation().flatMap(Substation::getCountry).map(String::valueOf).orElse(null);
            case CONNECTED_1 -> String.valueOf(twoWindingsTransformer.getTerminal1().isConnected());
            case CONNECTED_2 -> String.valueOf(twoWindingsTransformer.getTerminal2().isConnected());
            case NOMINAL_VOLTAGE_1 -> String.valueOf(twoWindingsTransformer.getTerminal1().getVoltageLevel().getNominalV());
            case NOMINAL_VOLTAGE_2 -> String.valueOf(twoWindingsTransformer.getTerminal2().getVoltageLevel().getNominalV());
            case RATED_VOLTAGE_1 -> String.valueOf(twoWindingsTransformer.getRatedU1());
            case RATED_VOLTAGE_2 -> String.valueOf(twoWindingsTransformer.getRatedU2());
            case VOLTAGE_LEVEL_ID_1 -> twoWindingsTransformer.getTerminal1().getVoltageLevel().getId();
            case VOLTAGE_LEVEL_ID_2 -> twoWindingsTransformer.getTerminal2().getVoltageLevel().getId();
            case RATED_S -> String.valueOf(twoWindingsTransformer.getRatedS());
            case SERIE_RESISTANCE -> String.valueOf(twoWindingsTransformer.getR());
            case SERIE_REACTANCE -> String.valueOf(twoWindingsTransformer.getX());
            case MAGNETIZING_CONDUCTANCE -> String.valueOf(twoWindingsTransformer.getG());
            case MAGNETIZING_SUSCEPTANCE -> String.valueOf(twoWindingsTransformer.getB());
            case HAS_RATIO_TAP_CHANGER -> String.valueOf(twoWindingsTransformer.hasRatioTapChanger());
            case RATIO_REGULATING,
                RATIO_TARGET_V,
                LOAD_TAP_CHANGING_CAPABILITIES,
                RATIO_REGULATION_MODE -> getRatioTapChangerFieldValue(field, twoWindingsTransformer.getRatioTapChanger());
            case HAS_PHASE_TAP_CHANGER -> String.valueOf(twoWindingsTransformer.hasPhaseTapChanger());
            case PHASE_REGULATING,
                PHASE_REGULATION_MODE,
                PHASE_REGULATION_VALUE -> getPhaseTapChangerFieldValue(field, twoWindingsTransformer.getPhaseTapChanger());
            case SUBSTATION_PROPERTIES_1 -> twoWindingsTransformer.getTerminal1().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case SUBSTATION_PROPERTIES_2 -> twoWindingsTransformer.getTerminal2().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_1 -> twoWindingsTransformer.getTerminal1().getVoltageLevel().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_2 -> twoWindingsTransformer.getTerminal2().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + twoWindingsTransformer.getType() + "]");
        };
    }

    public static List<FilterEquipments> getFilterEquipments(Network network, Set<String> uuids, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        List<FilterEquipments> res = new ArrayList<>();
        uuids.stream().map(UUID::fromString).forEach(uuid -> {
            if (cachedUuidFilters.containsKey(uuid)) {
                if (cachedUuidFilters.get(uuid) != null) {
                    res.add(cachedUuidFilters.get(uuid));
                }
            } else {
                // We do not allow to use expert filters for IS_PART_OF or IS_NOT_PART_OF operators
                List<FilterEquipments> filterEquipments = FilterServiceUtils.getFilterEquipmentsFromUuid(network, uuid, filterLoader, Set.of(FilterType.EXPERT));
                cachedUuidFilters.put(uuid, !CollectionUtils.isEmpty(filterEquipments) ? filterEquipments.get(0) : null);
                res.addAll(filterEquipments);
            }
        });
        return res;
    }

    public static boolean isPartOf(Network network, String value, Set<String> uuids, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        List<FilterEquipments> equipments = getFilterEquipments(network, uuids, filterLoader, cachedUuidFilters);
        return equipments.stream().flatMap(e -> e.getIdentifiableAttributes().stream()
            .map(IdentifiableAttributes::getId)).collect(Collectors.toSet()).contains(value);
    }
}
