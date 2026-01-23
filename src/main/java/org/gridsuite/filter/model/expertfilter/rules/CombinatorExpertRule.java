/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;

import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString(callSuper = true)
@SuperBuilder
public class CombinatorExpertRule implements ExpertRule {
    private CombinatorType combinator;

    private List<ExpertRule> rules;

    @Override
    public void init(Network network) {
        rules.forEach(rule -> rule.init(network));
    }

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        // As long as there are rules, we go down the tree
        if (CombinatorType.AND == getCombinator()) {
            for (ExpertRule rule : this.getRules()) {
                // Recursively evaluate the rule
                if (!rule.evaluate(identifiable)) {
                    // If any rule is false, the whole combination is false
                    return false;
                }
            }
            return true;
        } else if (CombinatorType.OR == getCombinator()) {
            for (ExpertRule rule : getRules()) {
                // Recursively evaluate the rule
                if (rule.evaluate(identifiable)) {
                    // If any rule is true, the whole combination is true
                    return true;
                }
            }
            return false;
        } else {
            throw new PowsyblException(this.getCombinator() + " combinator type is not implemented with expert filter");
        }
    }
}
