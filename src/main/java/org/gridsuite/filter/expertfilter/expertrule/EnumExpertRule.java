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
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;

import java.util.Map;
import java.util.UUID;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.evaluatePhaseRegulationMode;
import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.evaluateRatioRegulationMode;
import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@NoArgsConstructor
@SuperBuilder
public class EnumExpertRule extends StringExpertRule {
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.ENUM;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        if (this.getField().equals(FieldType.RATIO_REGULATION_MODE)) {
            return evaluateRatioRegulationMode(identifiable, this.getOperator(), this.getValue(), this.getValues(), this.getDataType());
        } else if (this.getField().equals(FieldType.PHASE_REGULATION_MODE)) {
            return evaluatePhaseRegulationMode(identifiable, this.getOperator(), this.getValue(), this.getValues(), this.getDataType());
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
                    throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
            };
        }
    }
}
