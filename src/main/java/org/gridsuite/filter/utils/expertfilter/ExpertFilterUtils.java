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
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.exception.InvalidEquipmentType;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.utils.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                case STATIC_VAR_COMPENSATOR -> getStaticVarCompensatorFieldValue(field, propertyName, (StaticVarCompensator) identifiable);
                case DANGLING_LINE -> getDanglingLinesFieldValue(field, propertyName, (DanglingLine) identifiable);
                case THREE_WINDINGS_TRANSFORMER -> getThreeWindingsTransformerFieldValue(field, propertyName, (ThreeWindingsTransformer) identifiable);
                case HVDC_LINE -> getHvdcLineFieldValue(field, propertyName, (HvdcLine) identifiable);
                default -> throw new PowsyblException(TYPE_NOT_IMPLEMENTED + " [" + identifiable.getType() + "]");
            };
        };
    }

    private static String getHvdcLineFieldValue(FieldType field, String propertyName, HvdcLine hvdcLine) {
        return switch (field) {
            case CONNECTED_1 -> getTerminalFieldValue(field, hvdcLine.getConverterStation1().getTerminal());
            case CONNECTED_2 -> getTerminalFieldValue(field, hvdcLine.getConverterStation2().getTerminal());
            case CONVERTERS_MODE -> hvdcLine.getConvertersMode() != null ? hvdcLine.getConvertersMode().name() : null;
            case ACTIVE_POWER_SET_POINT -> String.valueOf(hvdcLine.getActivePowerSetpoint());
            case MAX_P -> String.valueOf(hvdcLine.getMaxP());
            case DC_NOMINAL_VOLTAGE -> String.valueOf(hvdcLine.getNominalV());
            case CONVERTER_STATION_ID_1 -> hvdcLine.getConverterStation1().getId();
            case CONVERTER_STATION_NOMINAL_VOLTAGE_1 ->
                String.valueOf(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getNominalV());
            case CONVERTER_STATION_ID_2 -> hvdcLine.getConverterStation2().getId();
            case CONVERTER_STATION_NOMINAL_VOLTAGE_2 ->
                String.valueOf(hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getNominalV());
            case COUNTRY_1, VOLTAGE_LEVEL_ID_1, SUBSTATION_ID_1 ->
                getVoltageLevelFieldValue(field, null, hvdcLine.getConverterStation1().getTerminal().getVoltageLevel());
            case COUNTRY_2, VOLTAGE_LEVEL_ID_2, SUBSTATION_ID_2 ->
                getVoltageLevelFieldValue(field, null, hvdcLine.getConverterStation2().getTerminal().getVoltageLevel());
            case SERIE_RESISTANCE -> String.valueOf(hvdcLine.getR());
            case SUBSTATION_PROPERTIES_1 -> hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case SUBSTATION_PROPERTIES_2 -> hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_1 -> hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_2 -> hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + hvdcLine.getType() + "]");
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
            case VOLTAGE_LEVEL_PROPERTIES -> voltageLevel.getProperty(propertyName);
            case LOW_SHORT_CIRCUIT_CURRENT_LIMIT -> String.valueOf(voltageLevel.getExtension(IdentifiableShortCircuit.class) == null ?
                Double.NaN : voltageLevel.getExtension(IdentifiableShortCircuit.class).getIpMin());
            case HIGH_SHORT_CIRCUIT_CURRENT_LIMIT -> String.valueOf(voltageLevel.getExtension(IdentifiableShortCircuit.class) == null ?
                Double.NaN : voltageLevel.getExtension(IdentifiableShortCircuit.class).getIpMax());
            case SUBSTATION_ID,
                 SUBSTATION_ID_1,
                 SUBSTATION_ID_2 ->
                voltageLevel.getSubstation().map(Substation::getId).orElse(null);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + voltageLevel.getType() + "]");
        };
    }

    private static String getLineFieldValue(FieldType field, String propertyName, Line line) {
        return switch (field) {
            case CONNECTED_1 -> getTerminalFieldValue(field, line.getTerminal(TwoSides.ONE));
            case CONNECTED_2 -> getTerminalFieldValue(field, line.getTerminal(TwoSides.TWO));
            case COUNTRY_1,
                VOLTAGE_LEVEL_ID_1,
                NOMINAL_VOLTAGE_1,
                SUBSTATION_ID_1 -> getVoltageLevelFieldValue(field, null, line.getTerminal(TwoSides.ONE).getVoltageLevel());
            case COUNTRY_2,
                VOLTAGE_LEVEL_ID_2,
                NOMINAL_VOLTAGE_2,
                SUBSTATION_ID_2 -> getVoltageLevelFieldValue(field, null, line.getTerminal(TwoSides.TWO).getVoltageLevel());
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
                VOLTAGE_LEVEL_ID,
                SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, load.getTerminal().getVoltageLevel());
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
                NOMINAL_VOLTAGE,
                SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, shuntCompensator.getTerminal().getVoltageLevel());
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
                getGeneratorStartupFieldValue(generator, field);
            case RATED_S -> String.valueOf(generator.getRatedS());
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID,
                SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, generator.getTerminal().getVoltageLevel());
            case CONNECTED,
                 P,
                 Q,
                 P_ABSOLUTE,
                 Q_ABSOLUTE
                    -> getTerminalFieldValue(field, generator.getTerminal());
            case SUBSTATION_PROPERTIES -> generator.getTerminal().getVoltageLevel().getNullableSubstation().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES -> generator.getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + generator.getType() + "]");
        };
    }

    @Nonnull
    private static String getGeneratorStartupFieldValue(Generator generator, FieldType fieldType) {
        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        if (generatorStartup == null) {
            return String.valueOf(Double.NaN);
        } else {
            return String.valueOf(
                switch (fieldType) {
                    case PLANNED_ACTIVE_POWER_SET_POINT -> generatorStartup.getPlannedActivePowerSetpoint();
                    case MARGINAL_COST -> generatorStartup.getMarginalCost();
                    case PLANNED_OUTAGE_RATE -> generatorStartup.getPlannedOutageRate();
                    case FORCED_OUTAGE_RATE -> generatorStartup.getForcedOutageRate();
                    default -> String.valueOf(Double.NaN);
                });
        }
    }

    private static String getBusFieldValue(FieldType field, Bus bus) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID,
                SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, bus.getVoltageLevel());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + bus.getType() + "]");
        };
    }

    private static String getBusBarSectionFieldValue(FieldType field, BusbarSection busbarSection) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID,
                SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, busbarSection.getTerminal().getVoltageLevel());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + busbarSection.getType() + "]");
        };
    }

    private static String getTerminalFieldValue(FieldType field, @Nullable Terminal terminal) {
        if (terminal == null) {
            return null;
        }
        return switch (field) {
            case CONNECTED,
                CONNECTED_1,
                CONNECTED_2 -> String.valueOf(terminal.isConnected());
            case REGULATING_TERMINAL_VL_ID ->
                    terminal.getVoltageLevel() != null ?
                    terminal.getVoltageLevel().getId() : null;
            case REGULATING_TERMINAL_CONNECTABLE_ID ->
                    terminal.getConnectable() != null ?
                    terminal.getConnectable().getId() : null;
            case P -> String.valueOf(terminal.getP());
            case Q -> String.valueOf(terminal.getQ());
            case P_ABSOLUTE -> String.valueOf(Math.abs(terminal.getP()));
            case Q_ABSOLUTE -> String.valueOf(Math.abs(terminal.getQ()));
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
                    VOLTAGE_LEVEL_ID,
                    SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, battery.getTerminal().getVoltageLevel());
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

    private static String getRatioRegulationMode(RatioTapChanger ratioTapChanger) {
        if (ratioTapChanger.hasLoadTapChangingCapabilities() && ratioTapChanger.isRegulating()) {
            return RatioRegulationModeType.VOLTAGE_REGULATION.name();
        } else if (!ratioTapChanger.isRegulating()) {
            return RatioRegulationModeType.FIXED_RATIO.name();
        } else {
            return null;
        }
    }

    private static String getRatioTapChangerFieldValue(FieldType field, @Nullable RatioTapChanger ratioTapChanger) {
        if (ratioTapChanger == null) {
            return null;
        }
        return switch (field) {
            case RATIO_TARGET_V,
                 RATIO_TARGET_V1,
                 RATIO_TARGET_V2,
                 RATIO_TARGET_V3 -> String.valueOf(ratioTapChanger.getTargetV());
            case LOAD_TAP_CHANGING_CAPABILITIES,
                 LOAD_TAP_CHANGING_CAPABILITIES_1,
                 LOAD_TAP_CHANGING_CAPABILITIES_2,
                 LOAD_TAP_CHANGING_CAPABILITIES_3 -> String.valueOf(ratioTapChanger.hasLoadTapChangingCapabilities());
            case RATIO_REGULATION_MODE,
                 RATIO_REGULATION_MODE_1,
                 RATIO_REGULATION_MODE_2,
                 RATIO_REGULATION_MODE_3 -> String.valueOf(getRatioRegulationMode(ratioTapChanger));
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",ratioTapChanger]");
        };
    }

    private static String getPhaseRegulationMode(PhaseTapChanger phaseTapChanger) {
        if (phaseTapChanger.getRegulationMode() != null && phaseTapChanger.isRegulating()) {
            return phaseTapChanger.getRegulationMode().name();
        } else {
            return null;
        }
    }

    private static String getPhaseTapChangerFieldValue(FieldType field, @Nullable PhaseTapChanger phaseTapChanger) {
        if (phaseTapChanger == null) {
            return null;
        }
        return switch (field) {
            case PHASE_REGULATION_VALUE,
                 PHASE_REGULATION_VALUE_1,
                 PHASE_REGULATION_VALUE_2,
                 PHASE_REGULATION_VALUE_3 -> String.valueOf(phaseTapChanger.getRegulationValue());
            case PHASE_REGULATION_MODE,
                 PHASE_REGULATION_MODE_1,
                 PHASE_REGULATION_MODE_2,
                 PHASE_REGULATION_MODE_3 -> String.valueOf(getPhaseRegulationMode(phaseTapChanger));
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
            case RATIO_TARGET_V,
                LOAD_TAP_CHANGING_CAPABILITIES,
                RATIO_REGULATION_MODE -> getRatioTapChangerFieldValue(field, twoWindingsTransformer.getRatioTapChanger());
            case HAS_PHASE_TAP_CHANGER -> String.valueOf(twoWindingsTransformer.hasPhaseTapChanger());
            case PHASE_REGULATION_MODE,
                PHASE_REGULATION_VALUE -> getPhaseTapChangerFieldValue(field, twoWindingsTransformer.getPhaseTapChanger());
            case SUBSTATION_PROPERTIES -> twoWindingsTransformer.getNullableSubstation() != null ?
                twoWindingsTransformer.getNullableSubstation().getProperty(propertyName) : null;
            case VOLTAGE_LEVEL_PROPERTIES_1 -> twoWindingsTransformer.getTerminal1().getVoltageLevel().getProperty(propertyName);
            case VOLTAGE_LEVEL_PROPERTIES_2 -> twoWindingsTransformer.getTerminal2().getVoltageLevel().getProperty(propertyName);
            case SUBSTATION_ID -> twoWindingsTransformer.getNullableSubstation() != null ?
                twoWindingsTransformer.getNullableSubstation().getId() : null;
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + twoWindingsTransformer.getType() + "]");
        };
    }

    private static String getThreeWindingsTransformerFieldValue(FieldType field, String propertyName, ThreeWindingsTransformer threeWindingsTransformer) {
        return switch (field) {
            case COUNTRY -> threeWindingsTransformer.getSubstation().flatMap(Substation::getCountry).map(String::valueOf).orElse(null);
            case RATED_VOLTAGE_0 -> String.valueOf(threeWindingsTransformer.getRatedU0());
            case SUBSTATION_PROPERTIES -> threeWindingsTransformer.getNullableSubstation() != null ?
                threeWindingsTransformer.getNullableSubstation().getProperty(propertyName) : null;
            case SUBSTATION_ID -> threeWindingsTransformer.getNullableSubstation() != null ?
                threeWindingsTransformer.getNullableSubstation().getId() : null;
            case CONNECTED_1,
                 NOMINAL_VOLTAGE_1,
                 RATED_VOLTAGE_1,
                 VOLTAGE_LEVEL_ID_1,
                 RATED_S1,
                 SERIE_RESISTANCE_1,
                 SERIE_REACTANCE_1,
                 MAGNETIZING_CONDUCTANCE_1,
                 MAGNETIZING_SUSCEPTANCE_1,
                 HAS_RATIO_TAP_CHANGER_1,
                 RATIO_TARGET_V1,
                 LOAD_TAP_CHANGING_CAPABILITIES_1,
                 RATIO_REGULATION_MODE_1,
                 HAS_PHASE_TAP_CHANGER_1,
                 PHASE_REGULATION_MODE_1,
                 PHASE_REGULATION_VALUE_1,
                 VOLTAGE_LEVEL_PROPERTIES_1 -> getThreeWindingsTransformerLegFieldValue(field, propertyName, threeWindingsTransformer.getLeg1());
            case CONNECTED_2,
                 NOMINAL_VOLTAGE_2,
                 RATED_VOLTAGE_2,
                 VOLTAGE_LEVEL_ID_2,
                 RATED_S2,
                 SERIE_RESISTANCE_2,
                 SERIE_REACTANCE_2,
                 MAGNETIZING_CONDUCTANCE_2,
                 MAGNETIZING_SUSCEPTANCE_2,
                 HAS_RATIO_TAP_CHANGER_2,
                 RATIO_TARGET_V2,
                 LOAD_TAP_CHANGING_CAPABILITIES_2,
                 RATIO_REGULATION_MODE_2,
                 HAS_PHASE_TAP_CHANGER_2,
                 PHASE_REGULATION_MODE_2,
                 PHASE_REGULATION_VALUE_2,
                 VOLTAGE_LEVEL_PROPERTIES_2 -> getThreeWindingsTransformerLegFieldValue(field, propertyName, threeWindingsTransformer.getLeg2());
            case CONNECTED_3,
                 NOMINAL_VOLTAGE_3,
                 RATED_VOLTAGE_3,
                 VOLTAGE_LEVEL_ID_3,
                 RATED_S3,
                 SERIE_RESISTANCE_3,
                 SERIE_REACTANCE_3,
                 MAGNETIZING_CONDUCTANCE_3,
                 MAGNETIZING_SUSCEPTANCE_3,
                 HAS_RATIO_TAP_CHANGER_3,
                 RATIO_TARGET_V3,
                 LOAD_TAP_CHANGING_CAPABILITIES_3,
                 RATIO_REGULATION_MODE_3,
                 HAS_PHASE_TAP_CHANGER_3,
                 PHASE_REGULATION_MODE_3,
                 PHASE_REGULATION_VALUE_3,
                 VOLTAGE_LEVEL_PROPERTIES_3 -> getThreeWindingsTransformerLegFieldValue(field, propertyName, threeWindingsTransformer.getLeg3());
            default ->
                throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + threeWindingsTransformer.getType() + "]");
        };
    }

    private static String getThreeWindingsTransformerLegFieldValue(FieldType field, String propertyName, ThreeWindingsTransformer.Leg leg) {
        return switch (field) {
            case CONNECTED_1,
                 CONNECTED_2,
                 CONNECTED_3 -> String.valueOf(leg.getTerminal().isConnected());
            case NOMINAL_VOLTAGE_1,
                 NOMINAL_VOLTAGE_2,
                 NOMINAL_VOLTAGE_3 -> String.valueOf(leg.getTerminal().getVoltageLevel().getNominalV());
            case RATED_VOLTAGE_1,
                 RATED_VOLTAGE_2,
                 RATED_VOLTAGE_3 -> String.valueOf(leg.getRatedU());
            case VOLTAGE_LEVEL_ID_1,
                 VOLTAGE_LEVEL_ID_2,
                 VOLTAGE_LEVEL_ID_3 -> String.valueOf(leg.getTerminal().getVoltageLevel().getId());
            case RATED_S1,
                 RATED_S2,
                 RATED_S3 -> String.valueOf(leg.getRatedS());
            case SERIE_RESISTANCE_1,
                 SERIE_RESISTANCE_2,
                 SERIE_RESISTANCE_3 -> String.valueOf(leg.getR());
            case SERIE_REACTANCE_1,
                 SERIE_REACTANCE_2,
                 SERIE_REACTANCE_3 -> String.valueOf(leg.getX());
            case MAGNETIZING_CONDUCTANCE_1,
                 MAGNETIZING_CONDUCTANCE_2,
                 MAGNETIZING_CONDUCTANCE_3 -> String.valueOf(leg.getG());
            case MAGNETIZING_SUSCEPTANCE_1,
                 MAGNETIZING_SUSCEPTANCE_2,
                 MAGNETIZING_SUSCEPTANCE_3 -> String.valueOf(leg.getB());
            case HAS_RATIO_TAP_CHANGER_1,
                 HAS_RATIO_TAP_CHANGER_2,
                 HAS_RATIO_TAP_CHANGER_3 -> String.valueOf(leg.hasRatioTapChanger());
            case RATIO_TARGET_V1,
                 LOAD_TAP_CHANGING_CAPABILITIES_1,
                 RATIO_REGULATION_MODE_1,
                 RATIO_TARGET_V2,
                 LOAD_TAP_CHANGING_CAPABILITIES_2,
                 RATIO_REGULATION_MODE_2,
                 RATIO_TARGET_V3,
                 LOAD_TAP_CHANGING_CAPABILITIES_3,
                 RATIO_REGULATION_MODE_3 -> getRatioTapChangerFieldValue(field, leg.getRatioTapChanger());
            case HAS_PHASE_TAP_CHANGER_1,
                 HAS_PHASE_TAP_CHANGER_2,
                 HAS_PHASE_TAP_CHANGER_3 -> String.valueOf(leg.hasPhaseTapChanger());
            case PHASE_REGULATION_MODE_1,
                 PHASE_REGULATION_VALUE_1,
                 PHASE_REGULATION_MODE_2,
                 PHASE_REGULATION_VALUE_2,
                 PHASE_REGULATION_MODE_3,
                 PHASE_REGULATION_VALUE_3 -> getPhaseTapChangerFieldValue(field, leg.getPhaseTapChanger());
            case VOLTAGE_LEVEL_PROPERTIES_1,
                 VOLTAGE_LEVEL_PROPERTIES_2,
                 VOLTAGE_LEVEL_PROPERTIES_3 -> leg.getTerminal().getVoltageLevel().getProperty(propertyName);
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ", THREE_WINDINGS_TRANSFORMER]");
        };
    }

    private static String getStaticVarCompensatorFieldValue(FieldType field, String propertyName, StaticVarCompensator svar) {
        return switch (field) {
            case COUNTRY,
                    NOMINAL_VOLTAGE,
                    VOLTAGE_LEVEL_ID,
                    VOLTAGE_LEVEL_PROPERTIES,
                    SUBSTATION_PROPERTIES,
                    SUBSTATION_ID -> getVoltageLevelFieldValue(field, propertyName, svar.getTerminal().getVoltageLevel());
            case CONNECTED -> getTerminalFieldValue(field, svar.getTerminal());
            case REGULATING_TERMINAL_VL_ID,
                    REGULATING_TERMINAL_CONNECTABLE_ID -> getTerminalFieldValue(field, svar.getRegulatingTerminal());
            case LOW_VOLTAGE_SET_POINT,
                    HIGH_VOLTAGE_SET_POINT,
                    LOW_VOLTAGE_THRESHOLD,
                    HIGH_VOLTAGE_THRESHOLD,
                    SUSCEPTANCE_FIX,
                    FIX_Q_AT_NOMINAL_V -> getStandbyAutomatonFieldValue(field, svar);
            case REGULATION_TYPE -> svar.getRegulatingTerminal() != null &&
                    svar.getRegulatingTerminal().getConnectable() != null &&
                    !Objects.equals(svar.getRegulatingTerminal().getConnectable().getId(), svar.getId()) ?
                    RegulationType.DISTANT.name() :
                    RegulationType.LOCAL.name();
            case REMOTE_REGULATED_TERMINAL -> svar.getRegulatingTerminal() != null &&
                    svar.getRegulatingTerminal().getVoltageLevel() != null &&
                    svar.getRegulatingTerminal().getConnectable() != null &&
                    !Objects.equals(svar.getRegulatingTerminal().getConnectable().getId(), svar.getId()) ?
                    String.valueOf(true) : null;
            case AUTOMATE -> svar.getExtension(StandbyAutomaton.class) != null ? String.valueOf(true) : null;
            case MAX_Q_AT_NOMINAL_V -> String.valueOf(
                    Math.pow(svar.getTerminal().getVoltageLevel().getNominalV(), 2) * svar.getBmax()
            );
            case MIN_Q_AT_NOMINAL_V -> String.valueOf(
                    Math.pow(svar.getTerminal().getVoltageLevel().getNominalV(), 2) * svar.getBmin()
            );
            case MIN_SUSCEPTANCE -> String.valueOf(svar.getBmin());
            case MAX_SUSCEPTANCE -> String.valueOf(svar.getBmax());
            case SVAR_REGULATION_MODE -> svar.getRegulationMode() != null ? svar.getRegulationMode().name() : null;
            case VOLTAGE_SET_POINT -> String.valueOf(svar.getVoltageSetpoint());
            case REACTIVE_POWER_SET_POINT -> String.valueOf(svar.getReactivePowerSetpoint());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + svar.getType() + "]");
        };
    }

    private static String getDanglingLinesFieldValue(FieldType field, String propertyName, DanglingLine danglingLine) {
        return switch (field) {
            case CONNECTED -> getTerminalFieldValue(field, danglingLine.getTerminal());
            case COUNTRY,
                    VOLTAGE_LEVEL_ID,
                    NOMINAL_VOLTAGE,
                    SUBSTATION_ID -> getVoltageLevelFieldValue(field, null, danglingLine.getTerminal().getVoltageLevel());
            case VOLTAGE_LEVEL_PROPERTIES,
                    SUBSTATION_PROPERTIES -> getVoltageLevelFieldValue(field, propertyName, danglingLine.getTerminal().getVoltageLevel());
            case P0 -> String.valueOf(danglingLine.getP0());
            case Q0 -> String.valueOf(danglingLine.getQ0());
            case SERIE_RESISTANCE -> String.valueOf(danglingLine.getR());
            case SERIE_REACTANCE -> String.valueOf(danglingLine.getX());
            case SHUNT_SUSCEPTANCE -> String.valueOf(danglingLine.getB());
            case SHUNT_CONDUCTANCE -> String.valueOf(danglingLine.getG());
            case PAIRED -> String.valueOf(danglingLine.isPaired());
            case PAIRING_KEY -> danglingLine.getPairingKey();
            case TIE_LINE_ID -> danglingLine.getTieLine().map(TieLine::getId).orElse(null);
            default ->
                throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + danglingLine.getType() + "]");
        };
    }

    private static String getStandbyAutomatonFieldValue(FieldType field, StaticVarCompensator svar) {
        StandbyAutomaton standbyAutomaton = svar.getExtension(StandbyAutomaton.class);
        if (standbyAutomaton == null) {
            return String.valueOf(Double.NaN);
        } else {
            return switch (field) {
                case LOW_VOLTAGE_SET_POINT -> String.valueOf(standbyAutomaton.getLowVoltageSetpoint());
                case HIGH_VOLTAGE_SET_POINT -> String.valueOf(standbyAutomaton.getHighVoltageSetpoint());
                case LOW_VOLTAGE_THRESHOLD -> String.valueOf(standbyAutomaton.getLowVoltageThreshold());
                case HIGH_VOLTAGE_THRESHOLD -> String.valueOf(standbyAutomaton.getHighVoltageThreshold());
                case SUSCEPTANCE_FIX -> String.valueOf(standbyAutomaton.getB0());
                case FIX_Q_AT_NOMINAL_V -> String.valueOf(
                        Math.pow(svar.getTerminal().getVoltageLevel().getNominalV(), 2) * standbyAutomaton.getB0()
                );
                default -> String.valueOf(Double.NaN);
            };
        }
    }

    public static List<FilterEquipments> getFilterEquipments(Network network, Set<String> uuids, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        List<FilterEquipments> res = new ArrayList<>();
        uuids.stream().map(UUID::fromString).forEach(uuid -> {
            if (cachedUuidFilters.containsKey(uuid)) {
                if (cachedUuidFilters.get(uuid) != null) {
                    res.add(cachedUuidFilters.get(uuid));
                }
            } else {
                filterLoader.getFilters(List.of(uuid)).stream()
                    .findFirst()
                    .ifPresent(filter -> FilterCycleDetector.checkNoCycle(filter, filterLoader));

                List<FilterEquipments> filterEquipments = FilterServiceUtils.getFilterEquipmentsFromUuid(network, uuid, filterLoader);
                cachedUuidFilters.put(uuid, CollectionUtils.isNotEmpty(filterEquipments) ? filterEquipments.getFirst() : null);
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

    /**
     * Build an {@code OR} rule from the rules passed.
     * @param rules the rule(s) to be applied
     * @return {@link Optional#empty() Empty} if no rule is passed,
     *     the {@link AbstractExpertRule rule} if the list has only 1 rule inside,
     *     otherwise an {@link CombinatorType#OR OR} {@link CombinatorExpertRule combinator} with the rules.
     */
    @Nonnull
    public static Optional<AbstractExpertRule> buildOrCombination(@Nullable final List<AbstractExpertRule> rules) {
        return buildCombination(rules, false);
    }

    /**
     * Build an {@code AND} rule from the rules passed.
     * @param rules the rule(s) to be applied
     * @return {@link Optional#empty() Empty} if no rule is passed,
     *     the {@link AbstractExpertRule rule} if the list has only 1 rule inside,
     *     otherwise an {@link CombinatorType#AND AND} {@link CombinatorExpertRule combinator} with the rules.
     */
    @Nonnull
    public static Optional<AbstractExpertRule> buildAndCombination(@Nullable final List<AbstractExpertRule> rules) {
        return buildCombination(rules, true);
    }

    @Nonnull
    private static Optional<AbstractExpertRule> buildCombination(@Nullable final List<AbstractExpertRule> rules, final boolean and) {
        if (rules == null || rules.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rules.size() > 1
                ? CombinatorExpertRule.builder().combinator(and ? CombinatorType.AND : CombinatorType.OR).rules(rules).build()
                : rules.getFirst());
    }

    /**
     * Builds expert filter with {@link VoltageLevel voltage level} IDs criteria.
     */
    @Nonnull
    public static ExpertFilter buildExpertFilterWithVoltageLevelIdsCriteria(@Nonnull final UUID filterUuid, @Nonnull final EquipmentType equipmentType) {
        return new ExpertFilter(UuidUtils.generateUUID(), TimeUtils.nowAsDate(), equipmentType,
            CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(
                FilterUuidExpertRule.builder().operator(OperatorType.IS_PART_OF).field(FieldType.VOLTAGE_LEVEL_ID_1).values(Set.of(filterUuid.toString())).build(),
                FilterUuidExpertRule.builder().operator(OperatorType.IS_PART_OF).field(FieldType.VOLTAGE_LEVEL_ID_2).values(Set.of(filterUuid.toString())).build()
            )).build());
    }

    @Nonnull
    public static Stream<FieldType> getIdFieldMatchingType(@Nonnull final EquipmentType actualType, @Nonnull final EquipmentType filterEquipmentType) {
        if (actualType == filterEquipmentType) {
            return Stream.of(FieldType.ID);
        } else if (filterEquipmentType == EquipmentType.SUBSTATION) {
            return switch (actualType) {
                case SUBSTATION -> throw new AssertionError("This case can't happen");
                case BATTERY, BUS, BUSBAR_SECTION, GENERATOR, LOAD, SHUNT_COMPENSATOR, VOLTAGE_LEVEL,
                     LCC_CONVERTER_STATION, STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION -> Stream.of(FieldType.VOLTAGE_LEVEL_ID);
                case LINE, HVDC_LINE, DANGLING_LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER
                        -> Stream.of(FieldType.VOLTAGE_LEVEL_ID_1, FieldType.VOLTAGE_LEVEL_ID_2);
            };
        } else if (filterEquipmentType == EquipmentType.VOLTAGE_LEVEL) {
            return switch (actualType) {
                case VOLTAGE_LEVEL -> throw new AssertionError("This case can't happen");
                case BATTERY, BUS, BUSBAR_SECTION, GENERATOR, LOAD, SHUNT_COMPENSATOR, SUBSTATION,
                     LCC_CONVERTER_STATION, STATIC_VAR_COMPENSATOR, VSC_CONVERTER_STATION -> Stream.of(FieldType.SUBSTATION_ID);
                case LINE, HVDC_LINE, DANGLING_LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER
                        -> Stream.of(FieldType.SUBSTATION_ID_1, FieldType.SUBSTATION_ID_2);
            };
        } else {
            // the webapp doesn't authorize this case, so normally this case can't happen
            throw new InvalidEquipmentType("No matching field for type " + actualType + " and filter type " + filterEquipmentType);
        }
    }
}
