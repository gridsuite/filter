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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.utils.expertfilter.DataType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.NOT_EXISTS;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BooleanExpertRule extends AbstractExpertRule {
    private Boolean value;

    @Override
    public String getStringValue() {
        return value != null ? value.toString() : null;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.BOOLEAN;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        String fieldValue = getFieldValue(this.getField(), null, identifiable);
        if (fieldValue == null) {
            return this.getOperator() == NOT_EXISTS;
        }
        boolean identifiableValue = Boolean.parseBoolean(fieldValue);
        boolean filterValue = Optional.ofNullable(this.getValue()).orElse(false);
        return switch (this.getOperator()) {
            case EQUALS -> identifiableValue == filterValue;
            case NOT_EQUALS -> identifiableValue != filterValue;
            case EXISTS -> identifiableValue;
            case NOT_EXISTS -> !identifiableValue;
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }
}
