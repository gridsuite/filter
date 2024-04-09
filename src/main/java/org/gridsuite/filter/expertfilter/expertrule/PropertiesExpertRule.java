/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

import java.util.*;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author maissa SOUISSI <maissa.souissi at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PropertiesExpertRule extends AbstractExpertRule {
    private String propertyName;

    @JsonDeserialize(as = ArrayList.class)
    private List<String> propertyValues;

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        if (propertyName == null) {
            return false;
        }
        String propertyeValue = getFieldValue(this.getField(), propertyName, identifiable);
        if (propertyeValue == null) {
            return false;
        }
        return switch (this.getOperator()) {
            case EQUALS -> this.getPropertyValues().contains(propertyeValue);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }

    @Override
    public DataType getDataType() {
        return DataType.PROPERTIES;
    }

    @Override
    public String getStringValue() {
        return this.getPropertyName();
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public List<String> getPropertyValues() {
        return this.propertyValues;
    }
}
