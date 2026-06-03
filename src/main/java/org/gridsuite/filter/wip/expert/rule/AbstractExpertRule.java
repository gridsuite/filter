/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.annotations.Beta;
import com.powsybl.commons.PowsyblException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;

@Beta
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BooleanExpertRule.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = CombinatorExpertRule.class, name = "COMBINATOR"),
    @JsonSubTypes.Type(value = EnumExpertRule.class, name = "ENUM"),
    @JsonSubTypes.Type(value = FilterExpertRule.class, name = "FILTER"),
    @JsonSubTypes.Type(value = NumberExpertRule.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = PropertiesExpertRule.class, name = "PROPERTIES"),
    @JsonSubTypes.Type(value = StringExpertRule.class, name = "STRING")
})
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractExpertRule implements ExpertRule {

    @JsonProperty("type")
    public abstract DataType getDataType();

    protected abstract OperatorType getOperatorType();

    protected PowsyblException unsupportedOperatorException() {
        return new PowsyblException(String.format("%s operator not supported with %s rule data type", getOperatorType(), getDataType()));
    }
}
