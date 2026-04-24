/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.model.AbstractExpertRule;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.DataType;

import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class CombinatorExpertRule extends AbstractExpertRule {
    private final CombinatorType combinatorType;

    private final List<AbstractExpertRule> rules;

    public CombinatorExpertRule(CombinatorType combinatorType, List<AbstractExpertRule> rules) {
        this.combinatorType = combinatorType;
        this.rules = rules;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        // As long as there are rules, we go down the tree
        if (CombinatorType.AND == combinatorType) {
            for (AbstractExpertRule rule : rules) {
                // Recursively evaluate the rule
                if (!rule.evaluateRule(identifiable)) {
                    // If any rule is false, the whole combination is false
                    return false;
                }
            }
            return true;
        } else if (CombinatorType.OR == combinatorType) {
            for (AbstractExpertRule rule : rules) {
                // Recursively evaluate the rule
                if (rule.evaluateRule(identifiable)) {
                    // If any rule is true, the whole combination is true
                    return true;
                }
            }
            return false;
        } else {
            throw new PowsyblException(combinatorType + " combinator type is not implemented with expert filter");
        }
    }

    @Override
    public void init(Network network) {
        rules.forEach(rule -> rule.init(network));
    }
}
