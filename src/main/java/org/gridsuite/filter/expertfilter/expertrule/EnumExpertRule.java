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

import java.util.Map;
import java.util.UUID;

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
        return switch (this.getOperator()) {
            case EQUALS -> {
                String identifiableValue = getFieldValue(this.getField(), this.getValue(), identifiable);
                yield identifiableValue != null && identifiableValue.equals(this.getValue());
            }
            case NOT_EQUALS -> {
                String identifiableValue = getFieldValue(this.getField(), this.getValue(), identifiable);
                yield identifiableValue != null && !identifiableValue.equals(this.getValue());
            }
            case IN -> this.getValues().stream().anyMatch(value -> value.equals(getFieldValue(this.getField(), value, identifiable)));
            case NOT_IN -> this.getValues().stream().noneMatch(value -> value.equals(getFieldValue(this.getField(), value, identifiable)));
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }
}
