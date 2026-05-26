/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import org.gridsuite.filter.utils.RegulationType;
import org.gridsuite.filter.wip.expert.data.FieldType;

import java.util.Objects;

public final class ExpertRuleUtils {

    public static final String TERMINAL_TYPE = "TERMINAL";
    public static final String RATIO_TAP_CHANGER_TYPE = "RATIO_TAP_CHANGER";
    public static final String PHASE_TAP_CHANGER_TYPE = "PHASE_TAP_CHANGER";
    public static final String THREE_WINDINGS_TRANSFORMER_LEG_TYPE = "THREE_WINDINGS_TRANSFORMER_LEG";

    private ExpertRuleUtils() {
    }

    private static final String EQUIPMENT_NOT_SUPPORTED_TEMPLATE = "Data Type %s field value retrieval for equipment type: %s not supported";
    private static final String FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE = "Equipment Type: %s does not support %s field value retrieval for data type: %s";

    static Boolean getBooleanFieldValue(Identifiable<?> identifiable, FieldType fieldType) {
        return switch (identifiable.getType()) {
            case LINE -> getLineBooleanFieldValue((Line) identifiable, fieldType);
            case GENERATOR -> getGeneratorBooleanFieldValue((Generator) identifiable, fieldType);
            case LOAD -> getLoadBooleanFieldValue((Load) identifiable, fieldType);
            case SHUNT_COMPENSATOR -> getShuntCompensatorBooleanFieldValue((ShuntCompensator) identifiable, fieldType);
            case BATTERY -> getBatteryBooleanFieldValue((Battery) identifiable, fieldType);
            case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformerBooleanFieldValue((TwoWindingsTransformer) identifiable, fieldType);
            case STATIC_VAR_COMPENSATOR -> getStaticVarCompensatorBooleanFieldValue((StaticVarCompensator) identifiable, fieldType);
            case BOUNDARY_LINE -> getBoundaryLineBooleanFieldValue((BoundaryLine) identifiable, fieldType);
            case THREE_WINDINGS_TRANSFORMER -> getThreeWindingsTransformerBooleanFieldValue((ThreeWindingsTransformer) identifiable, fieldType);
            case HVDC_LINE -> getHvdcLineBooleanFieldValue((HvdcLine) identifiable, fieldType);
            default -> throw new PowsyblException(String.format(EQUIPMENT_NOT_SUPPORTED_TEMPLATE, identifiable.getType(), fieldType.getDataType()));
        };
    }

    static Double getDoubleFieldValue(Identifiable<?> identifiable, FieldType fieldType) {
        return switch (identifiable.getType()) {
            case VOLTAGE_LEVEL -> getVoltageLevelDoubleFieldValue((VoltageLevel) identifiable, fieldType);
            case LINE -> getLineDoubleFieldValue((Line) identifiable, fieldType);
            case GENERATOR -> getGeneratorDoubleFieldValue((Generator) identifiable, fieldType);
            case LOAD -> getLoadDoubleFieldValue((Load) identifiable, fieldType);
            case SHUNT_COMPENSATOR -> getShuntCompensatorDoubleFieldValue((ShuntCompensator) identifiable, fieldType);
            case BUS -> getBusDoubleFieldValue((Bus) identifiable, fieldType);
            case BUSBAR_SECTION -> getBusbarSectionDoubleFieldValue((BusbarSection) identifiable, fieldType);
            case BATTERY -> getBatteryDoubleFieldValue((Battery) identifiable, fieldType);
            case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformerDoubleFieldValue((TwoWindingsTransformer) identifiable, fieldType);
            case STATIC_VAR_COMPENSATOR -> getStaticVarCompensatorDoubleFieldValue((StaticVarCompensator) identifiable, fieldType);
            case BOUNDARY_LINE -> getBoundaryLineDoubleFieldValue((BoundaryLine) identifiable, fieldType);
            case THREE_WINDINGS_TRANSFORMER -> getThreeWindingsTransformerDoubleFieldValue((ThreeWindingsTransformer) identifiable, fieldType);
            case HVDC_LINE -> getHvdcLineDoubleFieldValue((HvdcLine) identifiable, fieldType);
            case HVDC_CONVERTER_STATION -> getHvdcConverterStationDoubleFieldValue((HvdcConverterStation<?>) identifiable, fieldType);
            default -> throw new PowsyblException(String.format(EQUIPMENT_NOT_SUPPORTED_TEMPLATE, identifiable.getType(), fieldType.getDataType()));
        };
    }

    static String getStringFieldValue(Identifiable<?> identifiable, String propertyName, FieldType fieldType) {
        return switch (fieldType) {
            case ID -> identifiable.getId();
            case NAME -> identifiable.getOptionalName().orElse(null);
            case FREE_PROPERTIES -> identifiable.getProperty(propertyName);
            default -> getIdentifiableStringFieldValue(identifiable, propertyName, fieldType);
        };
    }

    private static String getIdentifiableStringFieldValue(Identifiable<?> identifiable, String propertyName, FieldType fieldType) {
        return switch (identifiable.getType()) {
            case VOLTAGE_LEVEL -> getVoltageLevelStringFieldValue((VoltageLevel) identifiable, propertyName, fieldType);
            case LINE -> getLineStringFieldValue((Line) identifiable, propertyName, fieldType);
            case GENERATOR -> getGeneratorStringFieldValue((Generator) identifiable, propertyName, fieldType);
            case LOAD -> getLoadStringFieldValue((Load) identifiable, propertyName, fieldType);
            case SHUNT_COMPENSATOR -> getShuntCompensatorStringFieldValue((ShuntCompensator) identifiable, propertyName, fieldType);
            case BUS -> getBusStringFieldValue((Bus) identifiable, propertyName, fieldType);
            case BUSBAR_SECTION -> getBusbarSectionStringFieldValue((BusbarSection) identifiable, propertyName, fieldType);
            case BATTERY -> getBatteryStringFieldValue((Battery) identifiable, propertyName, fieldType);
            case SUBSTATION -> getSubstationStringFieldValue((Substation) identifiable, propertyName, fieldType);
            case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformerStringFieldValue((TwoWindingsTransformer) identifiable, propertyName, fieldType);
            case STATIC_VAR_COMPENSATOR -> getStaticVarCompensatorStringFieldValue((StaticVarCompensator) identifiable, propertyName, fieldType);
            case BOUNDARY_LINE -> getBoundaryLineStringFieldValue((BoundaryLine) identifiable, propertyName, fieldType);
            case THREE_WINDINGS_TRANSFORMER -> getThreeWindingsTransformerStringFieldValue((ThreeWindingsTransformer) identifiable, propertyName, fieldType);
            case HVDC_LINE -> getHvdcLineStringFieldValue((HvdcLine) identifiable, propertyName, fieldType);
            case HVDC_CONVERTER_STATION -> getHvdcConverterStationStringFieldValue((HvdcConverterStation<?>) identifiable, propertyName, fieldType);
            default -> throw new PowsyblException(String.format(EQUIPMENT_NOT_SUPPORTED_TEMPLATE, identifiable.getType(), fieldType.getDataType()));
        };
    }

    private static Boolean getLineBooleanFieldValue(Line line, FieldType fieldType) {
        if (line == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED_1 -> getTerminalBooleanFieldValue(line.getTerminal(TwoSides.ONE), fieldType);
            case CONNECTED_2 -> getTerminalBooleanFieldValue(line.getTerminal(TwoSides.TWO), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, line.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getLineDoubleFieldValue(Line line, FieldType fieldType) {
        if (line == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE_1 -> getVoltageLevelDoubleFieldValue(line.getTerminal(TwoSides.ONE).getVoltageLevel(), fieldType);
            case NOMINAL_VOLTAGE_2 -> getVoltageLevelDoubleFieldValue(line.getTerminal(TwoSides.TWO).getVoltageLevel(), fieldType);
            case SERIE_RESISTANCE -> line.getR();
            case SERIE_REACTANCE -> line.getX();
            case SHUNT_CONDUCTANCE_1 -> line.getG1();
            case SHUNT_CONDUCTANCE_2 -> line.getG2();
            case SHUNT_SUSCEPTANCE_1 -> line.getB1();
            case SHUNT_SUSCEPTANCE_2 -> line.getB2();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, line.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getLineStringFieldValue(Line line, String propertyName, FieldType fieldType) {
        if (line == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY_1, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_PROPERTIES_1, SUBSTATION_ID_1, SUBSTATION_PROPERTIES_1 -> getVoltageLevelStringFieldValue(line.getTerminal(TwoSides.ONE).getVoltageLevel(), propertyName, fieldType);
            case COUNTRY_2, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_PROPERTIES_2, SUBSTATION_ID_2, SUBSTATION_PROPERTIES_2 -> getVoltageLevelStringFieldValue(line.getTerminal(TwoSides.TWO).getVoltageLevel(), propertyName, fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, line.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getVoltageLevelStringFieldValue(VoltageLevel voltageLevel, String propertyName, FieldType fieldType) {
        if (voltageLevel == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, COUNTRY_1, COUNTRY_2, SUBSTATION_ID, SUBSTATION_ID_1, SUBSTATION_ID_2,
                 SUBSTATION_PROPERTIES, SUBSTATION_PROPERTIES_1, SUBSTATION_PROPERTIES_2 -> getSubstationStringFieldValue(voltageLevel.getNullableSubstation(), propertyName, fieldType);
            case VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2 -> voltageLevel.getId();
            case VOLTAGE_LEVEL_PROPERTIES, VOLTAGE_LEVEL_PROPERTIES_1, VOLTAGE_LEVEL_PROPERTIES_2 -> voltageLevel.getProperty(propertyName);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, voltageLevel.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getVoltageLevelDoubleFieldValue(VoltageLevel voltageLevel, FieldType fieldType) {
        if (voltageLevel == null) {
            return null;
        }
        IdentifiableShortCircuit<VoltageLevel> identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);

        return switch (fieldType) {
            case NOMINAL_VOLTAGE, NOMINAL_VOLTAGE_1, NOMINAL_VOLTAGE_2, NOMINAL_VOLTAGE_3,
                 CONVERTER_STATION_NOMINAL_VOLTAGE_1, CONVERTER_STATION_NOMINAL_VOLTAGE_2 -> voltageLevel.getNominalV();
            case LOW_VOLTAGE_LIMIT -> voltageLevel.getLowVoltageLimit();
            case HIGH_VOLTAGE_LIMIT -> voltageLevel.getHighVoltageLimit();
            case LOW_SHORT_CIRCUIT_CURRENT_LIMIT -> identifiableShortCircuit != null ? identifiableShortCircuit.getIpMin() : Double.NaN;
            case HIGH_SHORT_CIRCUIT_CURRENT_LIMIT -> identifiableShortCircuit != null ? identifiableShortCircuit.getIpMax() : Double.NaN;
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, voltageLevel.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getGeneratorBooleanFieldValue(Generator generator, FieldType fieldType) {
        if (generator == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED -> getTerminalBooleanFieldValue(generator.getTerminal(), fieldType);
            case VOLTAGE_REGULATOR_ON -> generator.isVoltageRegulatorOn();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, generator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getGeneratorDoubleFieldValue(Generator generator, FieldType fieldType) {
        if (generator == null) {
            return null;
        }
        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);

        return switch (fieldType) {
            case NOMINAL_VOLTAGE, P, Q, P_ABSOLUTE, Q_ABSOLUTE -> getTerminalDoubleFieldValue(generator.getTerminal(), fieldType);
            case PLANNED_ACTIVE_POWER_SET_POINT -> generatorStartup != null ? generatorStartup.getPlannedActivePowerSetpoint() : Double.NaN;
            case MARGINAL_COST -> generatorStartup != null ? generatorStartup.getMarginalCost() : Double.NaN;
            case PLANNED_OUTAGE_RATE -> generatorStartup != null ? generatorStartup.getPlannedOutageRate() : Double.NaN;
            case FORCED_OUTAGE_RATE -> generatorStartup != null ? generatorStartup.getForcedOutageRate() : Double.NaN;
            case MIN_P -> generator.getMinP();
            case MAX_P -> generator.getMaxP();
            case TARGET_V -> generator.getTargetV();
            case TARGET_P -> generator.getTargetP();
            case TARGET_Q -> generator.getTargetQ();
            case RATED_S -> generator.getRatedS();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, generator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getGeneratorStringFieldValue(Generator generator, String propertyName, FieldType fieldType) {
        if (generator == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_PROPERTIES, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getTerminalStringFieldValue(generator.getTerminal(), propertyName, fieldType);
            case ENERGY_SOURCE -> generator.getEnergySource().name();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, generator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getLoadBooleanFieldValue(Load load, FieldType fieldType) {
        if (load == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED -> getTerminalBooleanFieldValue(load.getTerminal(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, load.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getLoadDoubleFieldValue(Load load, FieldType fieldType) {
        if (load == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getTerminalDoubleFieldValue(load.getTerminal(), fieldType);
            case P0 -> load.getP0();
            case Q0 -> load.getQ0();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, load.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getLoadStringFieldValue(Load load, String propertyName, FieldType fieldType) {
        if (load == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_PROPERTIES, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getTerminalStringFieldValue(load.getTerminal(), propertyName, fieldType);
            case LOAD_TYPE -> load.getLoadType().name();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, load.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getShuntCompensatorBooleanFieldValue(ShuntCompensator shuntCompensator, FieldType fieldType) {
        if (shuntCompensator == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED -> getTerminalBooleanFieldValue(shuntCompensator.getTerminal(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, shuntCompensator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getShuntCompensatorDoubleFieldValue(ShuntCompensator shuntCompensator, FieldType fieldType) {
        if (shuntCompensator == null) {
            return null;
        }
        ShuntCompensatorLinearModel shuntCompensatorLinearModel = shuntCompensator.getModel(ShuntCompensatorLinearModel.class);
        Double retrievedNominalVoltage = getTerminalDoubleFieldValue(shuntCompensator.getTerminal(), FieldType.NOMINAL_VOLTAGE);

        double susceptancePerSection = shuntCompensatorLinearModel != null ? shuntCompensatorLinearModel.getBPerSection() : Double.NaN;
        double nominalVoltage = retrievedNominalVoltage != null ? retrievedNominalVoltage : Double.NaN;
        double qAtNominalVoltage = Math.pow(nominalVoltage, 2) * Math.abs(susceptancePerSection);

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getTerminalDoubleFieldValue(shuntCompensator.getTerminal(), fieldType);
            case MAX_Q_AT_NOMINAL_V -> qAtNominalVoltage * shuntCompensator.getMaximumSectionCount();
            case SWITCHED_ON_Q_AT_NOMINAL_V -> qAtNominalVoltage * shuntCompensator.getSectionCount();
            case MAX_SUSCEPTANCE -> susceptancePerSection * shuntCompensator.getMaximumSectionCount();
            case SWITCHED_ON_SUSCEPTANCE -> susceptancePerSection * shuntCompensator.getSectionCount();
            case SECTION_COUNT -> (double) shuntCompensator.getSectionCount();
            case MAXIMUM_SECTION_COUNT -> (double) shuntCompensator.getMaximumSectionCount();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, shuntCompensator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getShuntCompensatorStringFieldValue(ShuntCompensator shuntCompensator, String propertyName, FieldType fieldType) {
        if (shuntCompensator == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_PROPERTIES, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getTerminalStringFieldValue(shuntCompensator.getTerminal(), propertyName, fieldType);
            case SHUNT_COMPENSATOR_TYPE -> getShuntCompensatorType(shuntCompensator);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, shuntCompensator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getBusDoubleFieldValue(Bus bus, FieldType fieldType) {
        if (bus == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getVoltageLevelDoubleFieldValue(bus.getVoltageLevel(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, bus.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getBusStringFieldValue(Bus bus, String propertyName, FieldType fieldType) {
        if (bus == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, SUBSTATION_ID -> getVoltageLevelStringFieldValue(bus.getVoltageLevel(), propertyName, fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, bus.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getBusbarSectionDoubleFieldValue(BusbarSection busbarSection, FieldType fieldType) {
        if (busbarSection == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getTerminalDoubleFieldValue(busbarSection.getTerminal(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, busbarSection.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getBusbarSectionStringFieldValue(BusbarSection busbarSection, String propertyName, FieldType fieldType) {
        if (busbarSection == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, SUBSTATION_ID -> getTerminalStringFieldValue(busbarSection.getTerminal(), propertyName, fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, busbarSection.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getBatteryBooleanFieldValue(Battery battery, FieldType fieldType) {
        if (battery == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED -> getTerminalBooleanFieldValue(battery.getTerminal(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, battery.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getBatteryDoubleFieldValue(Battery battery, FieldType fieldType) {
        if (battery == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getTerminalDoubleFieldValue(battery.getTerminal(), fieldType);
            case MIN_P -> battery.getMinP();
            case MAX_P -> battery.getMaxP();
            case TARGET_P -> battery.getTargetP();
            case TARGET_Q -> battery.getTargetQ();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, battery.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getBatteryStringFieldValue(Battery battery, String propertyName, FieldType fieldType) {
        if (battery == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_PROPERTIES, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getTerminalStringFieldValue(battery.getTerminal(), propertyName, fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, battery.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getSubstationStringFieldValue(Substation substation, String propertyName, FieldType fieldType) {
        if (substation == null) {
            return null;
        }

        return switch (fieldType) {
            case SUBSTATION_ID, SUBSTATION_ID_1, SUBSTATION_ID_2 -> substation.getId();
            case COUNTRY, COUNTRY_1, COUNTRY_2 -> substation.getCountry().map(Country::name).orElse(null);
            case SUBSTATION_PROPERTIES, SUBSTATION_PROPERTIES_1, SUBSTATION_PROPERTIES_2 -> substation.getProperty(propertyName);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, substation.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getTwoWindingsTransformerBooleanFieldValue(TwoWindingsTransformer twoWindingsTransformer, FieldType fieldType) {
        if (twoWindingsTransformer == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED_1 -> getTerminalBooleanFieldValue(twoWindingsTransformer.getTerminal(TwoSides.ONE), fieldType);
            case CONNECTED_2 -> getTerminalBooleanFieldValue(twoWindingsTransformer.getTerminal(TwoSides.TWO), fieldType);
            case LOAD_TAP_CHANGING_CAPABILITIES -> getRatioTapChangerBooleanFieldValue(twoWindingsTransformer.getRatioTapChanger(), fieldType);
            case HAS_RATIO_TAP_CHANGER -> twoWindingsTransformer.hasRatioTapChanger();
            case HAS_PHASE_TAP_CHANGER -> twoWindingsTransformer.hasPhaseTapChanger();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, twoWindingsTransformer.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getTwoWindingsTransformerDoubleFieldValue(TwoWindingsTransformer twoWindingsTransformer, FieldType fieldType) {
        if (twoWindingsTransformer == null) {
            return null;
        }
        return switch (fieldType) {
            case NOMINAL_VOLTAGE_1 -> getTerminalDoubleFieldValue(twoWindingsTransformer.getTerminal(TwoSides.ONE), fieldType);
            case NOMINAL_VOLTAGE_2 -> getTerminalDoubleFieldValue(twoWindingsTransformer.getTerminal(TwoSides.TWO), fieldType);
            case RATIO_TARGET_V -> getRatioTapChangerDoubleFieldValue(twoWindingsTransformer.getRatioTapChanger(), fieldType);
            case PHASE_REGULATION_VALUE -> getPhaseTapChangerDoubleFieldValue(twoWindingsTransformer.getPhaseTapChanger(), fieldType);
            case RATED_VOLTAGE_1 -> twoWindingsTransformer.getRatedU1();
            case RATED_VOLTAGE_2 -> twoWindingsTransformer.getRatedU2();
            case RATED_S -> twoWindingsTransformer.getRatedS();
            case SERIE_RESISTANCE -> twoWindingsTransformer.getR();
            case SERIE_REACTANCE -> twoWindingsTransformer.getX();
            case MAGNETIZING_CONDUCTANCE -> twoWindingsTransformer.getG();
            case MAGNETIZING_SUSCEPTANCE -> twoWindingsTransformer.getB();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, twoWindingsTransformer.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getTwoWindingsTransformerStringFieldValue(TwoWindingsTransformer twoWindingsTransformer, String propertyName, FieldType fieldType) {
        if (twoWindingsTransformer == null) {
            return null;
        }
        return switch (fieldType) {
            case COUNTRY, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getSubstationStringFieldValue(twoWindingsTransformer.getNullableSubstation(), propertyName, fieldType);
            case VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_PROPERTIES_1 -> getTerminalStringFieldValue(twoWindingsTransformer.getTerminal(TwoSides.ONE), propertyName, fieldType);
            case VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_PROPERTIES_2 -> getTerminalStringFieldValue(twoWindingsTransformer.getTerminal(TwoSides.TWO), propertyName, fieldType);
            case RATIO_REGULATION_MODE -> getRatioTapChangerStringFieldValue(twoWindingsTransformer.getRatioTapChanger(), fieldType);
            case PHASE_REGULATION_MODE -> getPhaseTapChangerStringFieldValue(twoWindingsTransformer.getPhaseTapChanger(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, twoWindingsTransformer.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getStaticVarCompensatorBooleanFieldValue(StaticVarCompensator staticVarCompensator, FieldType fieldType) {
        if (staticVarCompensator == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED -> getTerminalBooleanFieldValue(staticVarCompensator.getTerminal(), fieldType);
            case REMOTE_REGULATED_TERMINAL -> hasStaticVarCompensatorRemoteRegulatedTerminal(staticVarCompensator);
            case AUTOMATE -> staticVarCompensator.getExtension(StandbyAutomaton.class) != null;
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, staticVarCompensator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getStaticVarCompensatorDoubleFieldValue(StaticVarCompensator staticVarCompensator, FieldType fieldType) {
        if (staticVarCompensator == null) {
            return null;
        }

        StandbyAutomaton standbyAutomaton = staticVarCompensator.getExtension(StandbyAutomaton.class);
        Double retrievedNominalV = getTerminalDoubleFieldValue(staticVarCompensator.getTerminal(), FieldType.NOMINAL_VOLTAGE);
        double nominalV = retrievedNominalV != null ? retrievedNominalV : Double.NaN;

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getTerminalDoubleFieldValue(staticVarCompensator.getTerminal(), fieldType);
            case LOW_VOLTAGE_SET_POINT -> standbyAutomaton != null ? standbyAutomaton.getLowVoltageSetpoint() : Double.NaN;
            case HIGH_VOLTAGE_SET_POINT -> standbyAutomaton != null ? standbyAutomaton.getHighVoltageSetpoint() : Double.NaN;
            case LOW_VOLTAGE_THRESHOLD -> standbyAutomaton != null ? standbyAutomaton.getLowVoltageThreshold() : Double.NaN;
            case HIGH_VOLTAGE_THRESHOLD -> standbyAutomaton != null ? standbyAutomaton.getHighVoltageThreshold() : Double.NaN;
            case SUSCEPTANCE_FIX -> standbyAutomaton != null ? standbyAutomaton.getB0() : Double.NaN;
            case FIX_Q_AT_NOMINAL_V -> standbyAutomaton != null ? Math.pow(nominalV, 2) * standbyAutomaton.getB0() : Double.NaN;
            case MIN_Q_AT_NOMINAL_V -> Math.pow(nominalV, 2) * staticVarCompensator.getBmin();
            case MAX_Q_AT_NOMINAL_V -> Math.pow(nominalV, 2) * staticVarCompensator.getBmax();
            case MIN_SUSCEPTANCE -> staticVarCompensator.getBmin();
            case MAX_SUSCEPTANCE -> staticVarCompensator.getBmax();
            case VOLTAGE_SET_POINT -> staticVarCompensator.getVoltageSetpoint();
            case REACTIVE_POWER_SET_POINT -> staticVarCompensator.getReactivePowerSetpoint();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, staticVarCompensator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getStaticVarCompensatorStringFieldValue(StaticVarCompensator staticVarCompensator, String propertyName, FieldType fieldType) {
        if (staticVarCompensator == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_PROPERTIES, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getTerminalStringFieldValue(staticVarCompensator.getTerminal(), propertyName, fieldType);
            case REGULATING_TERMINAL_VL_ID, REGULATING_TERMINAL_CONNECTABLE_ID -> getTerminalStringFieldValue(staticVarCompensator.getRegulatingTerminal(), propertyName, fieldType);
            case SVAR_REGULATION_MODE -> staticVarCompensator.getRegulationMode() != null ? staticVarCompensator.getRegulationMode().name() : null;
            case REGULATION_TYPE -> getStaticVarCompensatorRegulationType(staticVarCompensator);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, staticVarCompensator.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getBoundaryLineBooleanFieldValue(BoundaryLine boundaryLine, FieldType fieldType) {
        if (boundaryLine == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED -> getTerminalBooleanFieldValue(boundaryLine.getTerminal(), fieldType);
            case PAIRED -> boundaryLine.isPaired();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, boundaryLine.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getBoundaryLineDoubleFieldValue(BoundaryLine boundaryLine, FieldType fieldType) {
        if (boundaryLine == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE -> getTerminalDoubleFieldValue(boundaryLine.getTerminal(), fieldType);
            case P0 -> boundaryLine.getP0();
            case Q0 -> boundaryLine.getQ0();
            case SERIE_RESISTANCE -> boundaryLine.getR();
            case SERIE_REACTANCE -> boundaryLine.getX();
            case SHUNT_SUSCEPTANCE -> boundaryLine.getB();
            case SHUNT_CONDUCTANCE -> boundaryLine.getG();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, boundaryLine.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getBoundaryLineStringFieldValue(BoundaryLine boundaryLine, String propertyName, FieldType fieldType) {
        if (boundaryLine == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_PROPERTIES, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getTerminalStringFieldValue(boundaryLine.getTerminal(), propertyName, fieldType);
            case PAIRING_KEY -> boundaryLine.getPairingKey();
            case TIE_LINE_ID -> boundaryLine.getTieLine().map(TieLine::getId).orElse(null);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, boundaryLine.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getThreeWindingsTransformerBooleanFieldValue(ThreeWindingsTransformer threeWindingsTransformer, FieldType fieldType) {
        if (threeWindingsTransformer == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED_1, LOAD_TAP_CHANGING_CAPABILITIES_1, HAS_PHASE_TAP_CHANGER_1, HAS_RATIO_TAP_CHANGER_1 -> getThreeWindingsTransformerLegBooleanFieldValue(threeWindingsTransformer.getLeg(ThreeSides.ONE), fieldType);
            case CONNECTED_2, LOAD_TAP_CHANGING_CAPABILITIES_2, HAS_PHASE_TAP_CHANGER_2, HAS_RATIO_TAP_CHANGER_2 -> getThreeWindingsTransformerLegBooleanFieldValue(threeWindingsTransformer.getLeg(ThreeSides.TWO), fieldType);
            case CONNECTED_3, LOAD_TAP_CHANGING_CAPABILITIES_3, HAS_PHASE_TAP_CHANGER_3, HAS_RATIO_TAP_CHANGER_3 -> getThreeWindingsTransformerLegBooleanFieldValue(threeWindingsTransformer.getLeg(ThreeSides.THREE), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, threeWindingsTransformer.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getThreeWindingsTransformerDoubleFieldValue(ThreeWindingsTransformer threeWindingsTransformer, FieldType fieldType) {
        if (threeWindingsTransformer == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE_1, RATED_VOLTAGE_1, RATED_S1, SERIE_RESISTANCE_1, SERIE_REACTANCE_1, MAGNETIZING_CONDUCTANCE_1, MAGNETIZING_SUSCEPTANCE_1,
                 RATIO_TARGET_V1, PHASE_REGULATION_VALUE_1 -> getThreeWindingsTransformerLegDoubleFieldValue(threeWindingsTransformer.getLeg(ThreeSides.ONE), fieldType);
            case NOMINAL_VOLTAGE_2, RATED_VOLTAGE_2, RATED_S2, SERIE_RESISTANCE_2, SERIE_REACTANCE_2, MAGNETIZING_CONDUCTANCE_2, MAGNETIZING_SUSCEPTANCE_2,
                 RATIO_TARGET_V2, PHASE_REGULATION_VALUE_2 -> getThreeWindingsTransformerLegDoubleFieldValue(threeWindingsTransformer.getLeg(ThreeSides.TWO), fieldType);
            case NOMINAL_VOLTAGE_3, RATED_VOLTAGE_3, RATED_S3, SERIE_RESISTANCE_3, SERIE_REACTANCE_3, MAGNETIZING_CONDUCTANCE_3, MAGNETIZING_SUSCEPTANCE_3,
                 RATIO_TARGET_V3, PHASE_REGULATION_VALUE_3 -> getThreeWindingsTransformerLegDoubleFieldValue(threeWindingsTransformer.getLeg(ThreeSides.THREE), fieldType);
            case RATED_VOLTAGE_0 -> threeWindingsTransformer.getRatedU0();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, threeWindingsTransformer.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getThreeWindingsTransformerStringFieldValue(ThreeWindingsTransformer threeWindingsTransformer, String propertyName, FieldType fieldType) {
        if (threeWindingsTransformer == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, SUBSTATION_ID, SUBSTATION_PROPERTIES -> getSubstationStringFieldValue(threeWindingsTransformer.getNullableSubstation(), propertyName, fieldType);
            case VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_PROPERTIES_1, RATIO_REGULATION_MODE_1, PHASE_REGULATION_MODE_1 -> getThreeWindingsTransformerLegStringFieldValue(threeWindingsTransformer.getLeg(ThreeSides.ONE), propertyName, fieldType);
            case VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_PROPERTIES_2, RATIO_REGULATION_MODE_2, PHASE_REGULATION_MODE_2 -> getThreeWindingsTransformerLegStringFieldValue(threeWindingsTransformer.getLeg(ThreeSides.TWO), propertyName, fieldType);
            case VOLTAGE_LEVEL_ID_3, VOLTAGE_LEVEL_PROPERTIES_3, RATIO_REGULATION_MODE_3, PHASE_REGULATION_MODE_3 -> getThreeWindingsTransformerLegStringFieldValue(threeWindingsTransformer.getLeg(ThreeSides.THREE), propertyName, fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, threeWindingsTransformer.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getThreeWindingsTransformerLegBooleanFieldValue(ThreeWindingsTransformer.Leg leg, FieldType fieldType) {
        if (leg == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED_1, CONNECTED_2, CONNECTED_3 -> getTerminalBooleanFieldValue(leg.getTerminal(), fieldType);
            case LOAD_TAP_CHANGING_CAPABILITIES_1, LOAD_TAP_CHANGING_CAPABILITIES_2, LOAD_TAP_CHANGING_CAPABILITIES_3 -> getRatioTapChangerBooleanFieldValue(leg.getRatioTapChanger(), fieldType);
            case HAS_RATIO_TAP_CHANGER_1, HAS_RATIO_TAP_CHANGER_2, HAS_RATIO_TAP_CHANGER_3 -> leg.hasRatioTapChanger();
            case HAS_PHASE_TAP_CHANGER_1, HAS_PHASE_TAP_CHANGER_2, HAS_PHASE_TAP_CHANGER_3 -> leg.hasPhaseTapChanger();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, THREE_WINDINGS_TRANSFORMER_LEG_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static Double getThreeWindingsTransformerLegDoubleFieldValue(ThreeWindingsTransformer.Leg leg, FieldType fieldType) {
        if (leg == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE_1, NOMINAL_VOLTAGE_2, NOMINAL_VOLTAGE_3 -> getTerminalDoubleFieldValue(leg.getTerminal(), fieldType);
            case RATIO_TARGET_V1, RATIO_TARGET_V2, RATIO_TARGET_V3 -> getRatioTapChangerDoubleFieldValue(leg.getRatioTapChanger(), fieldType);
            case PHASE_REGULATION_VALUE_1, PHASE_REGULATION_VALUE_2, PHASE_REGULATION_VALUE_3 -> getPhaseTapChangerDoubleFieldValue(leg.getPhaseTapChanger(), fieldType);
            case RATED_VOLTAGE_1, RATED_VOLTAGE_2, RATED_VOLTAGE_3 -> leg.getRatedU();
            case RATED_S1, RATED_S2, RATED_S3 -> leg.getRatedS();
            case SERIE_RESISTANCE_1, SERIE_RESISTANCE_2, SERIE_RESISTANCE_3 -> leg.getR();
            case SERIE_REACTANCE_1, SERIE_REACTANCE_2, SERIE_REACTANCE_3 -> leg.getX();
            case MAGNETIZING_CONDUCTANCE_1, MAGNETIZING_CONDUCTANCE_2, MAGNETIZING_CONDUCTANCE_3 -> leg.getG();
            case MAGNETIZING_SUSCEPTANCE_1, MAGNETIZING_SUSCEPTANCE_2, MAGNETIZING_SUSCEPTANCE_3 -> leg.getB();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, THREE_WINDINGS_TRANSFORMER_LEG_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static String getThreeWindingsTransformerLegStringFieldValue(ThreeWindingsTransformer.Leg leg, String propertyName, FieldType fieldType) {
        if (leg == null) {
            return null;
        }

        return switch (fieldType) {
            case VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_ID_3, VOLTAGE_LEVEL_PROPERTIES_1, VOLTAGE_LEVEL_PROPERTIES_2, VOLTAGE_LEVEL_PROPERTIES_3 -> getTerminalStringFieldValue(leg.getTerminal(), propertyName, fieldType);
            case PHASE_REGULATION_MODE_1, PHASE_REGULATION_MODE_2, PHASE_REGULATION_MODE_3 -> getPhaseTapChangerStringFieldValue(leg.getPhaseTapChanger(), fieldType);
            case RATIO_REGULATION_MODE_1, RATIO_REGULATION_MODE_2, RATIO_REGULATION_MODE_3 -> getRatioTapChangerStringFieldValue(leg.getRatioTapChanger(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, THREE_WINDINGS_TRANSFORMER_LEG_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getHvdcLineBooleanFieldValue(HvdcLine hvdcLine, FieldType fieldType) {
        if (hvdcLine == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED_1 -> getHvdcConverterStationBooleanFieldValue(hvdcLine.getConverterStation(TwoSides.ONE), fieldType);
            case CONNECTED_2 -> getHvdcConverterStationBooleanFieldValue(hvdcLine.getConverterStation(TwoSides.TWO), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, hvdcLine.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getHvdcLineDoubleFieldValue(HvdcLine hvdcLine, FieldType fieldType) {
        if (hvdcLine == null) {
            return null;
        }

        return switch (fieldType) {
            case CONVERTER_STATION_NOMINAL_VOLTAGE_1 -> getHvdcConverterStationDoubleFieldValue(hvdcLine.getConverterStation(TwoSides.ONE), fieldType);
            case CONVERTER_STATION_NOMINAL_VOLTAGE_2 -> getHvdcConverterStationDoubleFieldValue(hvdcLine.getConverterStation(TwoSides.TWO), fieldType);
            case ACTIVE_POWER_SET_POINT -> hvdcLine.getActivePowerSetpoint();
            case MAX_P -> hvdcLine.getMaxP();
            case DC_NOMINAL_VOLTAGE -> hvdcLine.getNominalV();
            case SERIE_RESISTANCE -> hvdcLine.getR();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, hvdcLine.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getHvdcLineStringFieldValue(HvdcLine hvdcLine, String propertyName, FieldType fieldType) {
        if (hvdcLine == null) {
            return null;
        }

        return switch (fieldType) {
            case CONVERTER_STATION_ID_1, COUNTRY_1, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_PROPERTIES_1, SUBSTATION_ID_1, SUBSTATION_PROPERTIES_1 -> getHvdcConverterStationStringFieldValue(hvdcLine.getConverterStation(TwoSides.ONE), propertyName, fieldType);
            case CONVERTER_STATION_ID_2, COUNTRY_2, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_PROPERTIES_2, SUBSTATION_ID_2, SUBSTATION_PROPERTIES_2 -> getHvdcConverterStationStringFieldValue(hvdcLine.getConverterStation(TwoSides.TWO), propertyName, fieldType);
            case CONVERTERS_MODE -> hvdcLine.getConvertersMode() != null ? hvdcLine.getConvertersMode().name() : null;
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, hvdcLine.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getHvdcConverterStationBooleanFieldValue(HvdcConverterStation<?> converterStation, FieldType fieldType) {
        if (converterStation == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED_1, CONNECTED_2 -> getTerminalBooleanFieldValue(converterStation.getTerminal(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, converterStation.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Double getHvdcConverterStationDoubleFieldValue(HvdcConverterStation<?> converterStation, FieldType fieldType) {
        if (converterStation == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE, CONVERTER_STATION_NOMINAL_VOLTAGE_1, CONVERTER_STATION_NOMINAL_VOLTAGE_2 -> getTerminalDoubleFieldValue(converterStation.getTerminal(), fieldType);
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, converterStation.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static String getHvdcConverterStationStringFieldValue(HvdcConverterStation<?> converterStation, String propertyName, FieldType fieldType) {
        if (converterStation == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, COUNTRY_1, COUNTRY_2, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_PROPERTIES_1, VOLTAGE_LEVEL_PROPERTIES_2,
                 SUBSTATION_ID, SUBSTATION_ID_1, SUBSTATION_ID_2, SUBSTATION_PROPERTIES_1, SUBSTATION_PROPERTIES_2 -> getTerminalStringFieldValue(converterStation.getTerminal(), propertyName, fieldType);
            case CONVERTER_STATION_ID_1, CONVERTER_STATION_ID_2 -> converterStation.getId();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, converterStation.getType(), fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getTerminalBooleanFieldValue(Terminal terminal, FieldType fieldType) {
        if (terminal == null) {
            return null;
        }

        return switch (fieldType) {
            case CONNECTED, CONNECTED_1, CONNECTED_2, CONNECTED_3 -> terminal.isConnected();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, TERMINAL_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static Double getTerminalDoubleFieldValue(Terminal terminal, FieldType fieldType) {
        if (terminal == null) {
            return null;
        }

        return switch (fieldType) {
            case NOMINAL_VOLTAGE, NOMINAL_VOLTAGE_1, NOMINAL_VOLTAGE_2, NOMINAL_VOLTAGE_3,
                 CONVERTER_STATION_NOMINAL_VOLTAGE_1, CONVERTER_STATION_NOMINAL_VOLTAGE_2 -> getVoltageLevelDoubleFieldValue(terminal.getVoltageLevel(), fieldType);
            case P -> terminal.getP();
            case Q -> terminal.getQ();
            case P_ABSOLUTE -> Math.abs(terminal.getP());
            case Q_ABSOLUTE -> Math.abs(terminal.getQ());
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, TERMINAL_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static String getTerminalStringFieldValue(Terminal terminal, String propertyName, FieldType fieldType) {
        if (terminal == null) {
            return null;
        }

        return switch (fieldType) {
            case COUNTRY, VOLTAGE_LEVEL_ID, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_ID_3, VOLTAGE_LEVEL_PROPERTIES,
                 VOLTAGE_LEVEL_PROPERTIES_1, VOLTAGE_LEVEL_PROPERTIES_2, VOLTAGE_LEVEL_PROPERTIES_3, SUBSTATION_ID, SUBSTATION_ID_1,
                 SUBSTATION_ID_2, SUBSTATION_PROPERTIES, SUBSTATION_PROPERTIES_1, SUBSTATION_PROPERTIES_2 -> getVoltageLevelStringFieldValue(terminal.getVoltageLevel(), propertyName, fieldType);
            case REGULATING_TERMINAL_VL_ID -> terminal.getVoltageLevel() != null ? terminal.getVoltageLevel().getId() : null;
            case REGULATING_TERMINAL_CONNECTABLE_ID -> terminal.getConnectable() != null ? terminal.getConnectable().getId() : null;
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, TERMINAL_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static Boolean getRatioTapChangerBooleanFieldValue(RatioTapChanger ratioTapChanger, FieldType fieldType) {
        if (ratioTapChanger == null) {
            return null;
        }

        return switch (fieldType) {
            case LOAD_TAP_CHANGING_CAPABILITIES, LOAD_TAP_CHANGING_CAPABILITIES_1, LOAD_TAP_CHANGING_CAPABILITIES_2, LOAD_TAP_CHANGING_CAPABILITIES_3 -> ratioTapChanger.hasLoadTapChangingCapabilities();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, RATIO_TAP_CHANGER_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static Double getRatioTapChangerDoubleFieldValue(RatioTapChanger ratioTapChanger, FieldType fieldType) {
        if (ratioTapChanger == null) {
            return null;
        }

        return switch (fieldType) {
            case RATIO_TARGET_V, RATIO_TARGET_V1, RATIO_TARGET_V2, RATIO_TARGET_V3 -> ratioTapChanger.getTargetV();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, RATIO_TAP_CHANGER_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static String getRatioTapChangerStringFieldValue(RatioTapChanger ratioTapChanger, FieldType fieldType) {
        if (ratioTapChanger == null) {
            return null;
        }

        return switch (fieldType) {
            case RATIO_REGULATION_MODE, RATIO_REGULATION_MODE_1, RATIO_REGULATION_MODE_2, RATIO_REGULATION_MODE_3 -> ratioTapChanger.getRegulationMode() != null ? ratioTapChanger.getRegulationMode().name() : null;
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, RATIO_TAP_CHANGER_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static Double getPhaseTapChangerDoubleFieldValue(PhaseTapChanger phaseTapChanger, FieldType fieldType) {
        if (phaseTapChanger == null) {
            return null;
        }

        return switch (fieldType) {
            case PHASE_REGULATION_VALUE, PHASE_REGULATION_VALUE_1, PHASE_REGULATION_VALUE_2, PHASE_REGULATION_VALUE_3 -> phaseTapChanger.getRegulationValue();
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, PHASE_TAP_CHANGER_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static String getPhaseTapChangerStringFieldValue(PhaseTapChanger phaseTapChanger, FieldType fieldType) {
        if (phaseTapChanger == null) {
            return null;
        }

        return switch (fieldType) {
            case PHASE_REGULATION_MODE, PHASE_REGULATION_MODE_1, PHASE_REGULATION_MODE_2, PHASE_REGULATION_MODE_3 -> phaseTapChanger.getRegulationMode() != null ? phaseTapChanger.getRegulationMode().name() : null;
            default -> throw new PowsyblException(String.format(FIELD_RETRIEVAL_NOT_SUPPORTED_TEMPLATE, PHASE_TAP_CHANGER_TYPE, fieldType, fieldType.getDataType()));
        };
    }

    private static String getStaticVarCompensatorRegulationType(StaticVarCompensator staticVarCompensator) {
        if (staticVarCompensator == null) {
            return null;
        }

        if (staticVarCompensator.getRegulatingTerminal() != null &&
                staticVarCompensator.getRegulatingTerminal().getConnectable() != null &&
                Objects.equals(staticVarCompensator.getRegulatingTerminal().getConnectable().getId(), staticVarCompensator.getId())) {
            return RegulationType.LOCAL.name();
        }
        return RegulationType.DISTANT.name();
    }

    private static String getShuntCompensatorType(ShuntCompensator shuntCompensator) {
        if (shuntCompensator == null) {
            return null;
        }

        ShuntCompensatorLinearModel shuntCompensatorLinearModel = shuntCompensator.getModel(ShuntCompensatorLinearModel.class);
        if (shuntCompensatorLinearModel == null) {
            return null;
        }
        return shuntCompensatorLinearModel.getBPerSection() > 0 ? "CAPACITOR" : "REACTOR";
    }

    private static Boolean hasStaticVarCompensatorRemoteRegulatedTerminal(StaticVarCompensator staticVarCompensator) {
        if (staticVarCompensator == null) {
            return false;
        }

        return staticVarCompensator.getRegulatingTerminal() != null &&
                staticVarCompensator.getRegulatingTerminal().getVoltageLevel() != null &&
                staticVarCompensator.getRegulatingTerminal().getConnectable() != null &&
                !Objects.equals(staticVarCompensator.getRegulatingTerminal().getConnectable().getId(), staticVarCompensator.getId());
    }
}
