/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.RatioRegulationModeType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@NoArgsConstructor
@SuperBuilder
public class EnumExpertRule extends StringExpertRule {
    private static final String OPERATOR_NOT_SUPPORTED_WITH = " operator not supported with ";
    private static final String RULE_DATA_TYPE = " rule data type";

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.ENUM;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        if (this.getField().equals(FieldType.RATIO_REGULATION_MODE)) {
            return evaluateRatioRegulationMode(identifiable);
        } else if (this.getField().equals(FieldType.PHASE_REGULATION_MODE)) {
            return evaluatePhaseRegulationMode(identifiable);
        } else {
            String identifiableValue = getFieldValue(this.getField(), null, identifiable);
            if (identifiableValue == null) {
                return false;
            }
            return switch (this.getOperator()) {
                case EQUALS -> identifiableValue.equals(this.getValue());
                case NOT_EQUALS -> !identifiableValue.equals(this.getValue());
                case IN -> this.getValues().contains(identifiableValue);
                case NOT_IN -> !this.getValues().contains(identifiableValue);
                default ->
                    throw new PowsyblException(this.getOperator() + OPERATOR_NOT_SUPPORTED_WITH + this.getDataType() + RULE_DATA_TYPE);
            };
        }
    }

    private boolean transformerRatioRegulationModeEquals(TwoWindingsTransformer transformer, String value) {
        try {
            return switch (RatioRegulationModeType.valueOf(value)) {
                case VOLTAGE_REGULATION -> transformer.getRatioTapChanger().hasLoadTapChangingCapabilities() && transformer.getRatioTapChanger().isRegulating();
                case FIXED_RATIO -> !transformer.getRatioTapChanger().isRegulating();
            };
        } catch (IllegalArgumentException e) {
            throw new PowsyblException("Unexpected ratio regulation mode value + " + value + " : expected values are " + Stream.of(RatioRegulationModeType.values())
                .map(Enum::name)
                .toList());
        }
    }

    private boolean transformerRatioRegulationModeIn(TwoWindingsTransformer transformer, Set<String> values) {
        return values.stream().anyMatch(v -> transformerRatioRegulationModeEquals(transformer, v));
    }

    private boolean evaluateRatioRegulationMode(Identifiable<?> identifiable) {
        if (identifiable.getType() != IdentifiableType.TWO_WINDINGS_TRANSFORMER) {
            throw new PowsyblException(FieldType.RATIO_REGULATION_MODE + " field only supported for " + IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        }
        TwoWindingsTransformer transformer = (TwoWindingsTransformer) identifiable;
        if (transformer.getRatioTapChanger() == null) {
            return false;
        }

        return switch (this.getOperator()) {
            case EQUALS -> transformerRatioRegulationModeEquals(transformer, this.getValue());
            case NOT_EQUALS -> !transformerRatioRegulationModeEquals(transformer, this.getValue());
            case IN -> transformerRatioRegulationModeIn(transformer, this.getValues());
            case NOT_IN -> !transformerRatioRegulationModeIn(transformer, this.getValues());
            default ->
                throw new PowsyblException(this.getOperator() + OPERATOR_NOT_SUPPORTED_WITH + this.getDataType() + RULE_DATA_TYPE);
        };
    }

    private boolean transformerPhaseRegulationModeEquals(TwoWindingsTransformer transformer, String value) {
        try {
            return switch (PhaseTapChanger.RegulationMode.valueOf(value)) {
                case ACTIVE_POWER_CONTROL -> transformer.getPhaseTapChanger().getRegulationMode() == PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL && transformer.getPhaseTapChanger().isRegulating();
                case CURRENT_LIMITER -> transformer.getPhaseTapChanger().getRegulationMode() == PhaseTapChanger.RegulationMode.CURRENT_LIMITER && transformer.getPhaseTapChanger().isRegulating();
                case FIXED_TAP -> transformer.getPhaseTapChanger().getRegulationMode() == PhaseTapChanger.RegulationMode.FIXED_TAP ||
                    transformer.getPhaseTapChanger().getRegulationMode() == PhaseTapChanger.RegulationMode.CURRENT_LIMITER && !transformer.getPhaseTapChanger().isRegulating() ||
                    transformer.getPhaseTapChanger().getRegulationMode() == PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL && !transformer.getPhaseTapChanger().isRegulating();
            };
        } catch (IllegalArgumentException e) {
            throw new PowsyblException("Unexpected phase regulation mode value + " + value + " : expected values are " + Stream.of(PhaseTapChanger.RegulationMode.values())
                .map(Enum::name)
                .toList());
        }
    }

    private boolean transformerPhaseRegulationModeIn(TwoWindingsTransformer transformer, Set<String> values) {
        return values.stream().anyMatch(v -> transformerPhaseRegulationModeEquals(transformer, v));
    }

    private boolean evaluatePhaseRegulationMode(Identifiable<?> identifiable) {
        if (identifiable.getType() != IdentifiableType.TWO_WINDINGS_TRANSFORMER) {
            throw new PowsyblException(FieldType.PHASE_REGULATION_MODE + " field only supported for " + IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        }
        TwoWindingsTransformer transformer = (TwoWindingsTransformer) identifiable;
        if (transformer.getPhaseTapChanger() == null) {
            return false;
        }

        return switch (this.getOperator()) {
            case EQUALS -> transformerPhaseRegulationModeEquals(transformer, this.getValue());
            case NOT_EQUALS -> !transformerPhaseRegulationModeEquals(transformer, this.getValue());
            case IN -> transformerPhaseRegulationModeIn(transformer, this.getValues());
            case NOT_IN -> !transformerPhaseRegulationModeIn(transformer, this.getValues());
            default ->
                throw new PowsyblException(this.getOperator() + OPERATOR_NOT_SUPPORTED_WITH + this.getDataType() + RULE_DATA_TYPE);
        };
    }
}
