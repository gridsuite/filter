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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.model.expertfilter.rules.ExpertRule;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "dataType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringExpertRuleDto.class, name = "STRING"),
    @JsonSubTypes.Type(value = BooleanExpertRuleDto.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = EnumExpertRuleDto.class, name = "ENUM"),
    @JsonSubTypes.Type(value = NumberExpertRuleDto.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = CombinatorExpertRuleDto.class, name = "COMBINATOR"),
    @JsonSubTypes.Type(value = FilterUuidExpertRuleDto.class, name = "FILTER_UUID"),
    @JsonSubTypes.Type(value = PropertiesExpertRuleDto.class, name = "PROPERTIES"),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class AbstractExpertRuleDto {

    private CombinatorType combinator;

    private FieldType field;

    private OperatorType operator;

    private List<AbstractExpertRuleDto> rules;

    public abstract DataType getDataType();

    @JsonIgnore
    public abstract String getStringValue();

    abstract public ExpertRule toModel();
}
