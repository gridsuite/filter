/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter.rules;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.powsybl.iidm.network.Network;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringExpertRule.class, name = "STRING"),
    @JsonSubTypes.Type(value = StringListExpertRule.class, name = "STRING_LIST"),
    @JsonSubTypes.Type(value = BooleanExpertRule.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = EnumExpertRule.class, name = "ENUM"),
    @JsonSubTypes.Type(value = EnumListExpertRule.class, name = "ENUM_LIST"),
    @JsonSubTypes.Type(value = NumberExpertRule.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = NumberListExpertRule.class, name = "NUMBER_LIST"),
    @JsonSubTypes.Type(value = CombinatorExpertRule.class, name = "COMBINATOR"),
    @JsonSubTypes.Type(value = FilterExpertRule.class, name = "FILTER_UUID"),
    @JsonSubTypes.Type(value = PropertiesExpertRule.class, name = "PROPERTIES"),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class AbstractFieldExpertRule<I> implements ExpertRule {

    protected FieldType field;

    protected OperatorType operator;

    protected I value;

    @Override
    public void init(Network network) {
        // Do nothing by default
    }
}
