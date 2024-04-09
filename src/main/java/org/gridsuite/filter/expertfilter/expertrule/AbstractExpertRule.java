/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.powsybl.iidm.network.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "dataType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringExpertRule.class, name = "STRING"),
    @JsonSubTypes.Type(value = BooleanExpertRule.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = EnumExpertRule.class, name = "ENUM"),
    @JsonSubTypes.Type(value = NumberExpertRule.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = CombinatorExpertRule.class, name = "COMBINATOR"),
    @JsonSubTypes.Type(value = FilterUuidExpertRule.class, name = "FILTER_UUID"),
    @JsonSubTypes.Type(value = PropertiesExpertRule.class, name = "PROPERTIES"),
    @JsonSubTypes.Type(value = PropertiesExpertRule.class, name = "SUBSTATION_PROPERTIES"),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class AbstractExpertRule {

    private CombinatorType combinator;

    private FieldType field;

    private OperatorType operator;

    private List<AbstractExpertRule> rules;

    public abstract boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters);

    public abstract DataType getDataType();

    @JsonIgnore
    public abstract String getStringValue();
}
