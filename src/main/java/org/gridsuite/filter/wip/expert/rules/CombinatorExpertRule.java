/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rules;

import com.google.common.annotations.Beta;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;

import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public final class CombinatorExpertRule extends AbstractExpertRule {

    private final CombinatorType combinatorType;
    private final Set<ExpertRule> subRules;

    public static CombinatorExpertRule of(CombinatorType combinatorType, Set<ExpertRule> subRules) {
        if (combinatorType == null || subRules == null || subRules.isEmpty()) {
            throw new PowsyblException("Invalid combinator expert rule, all parameters must be non null and subrules must not be empty");
        }
        return new CombinatorExpertRule(combinatorType, subRules);
    }

    private CombinatorExpertRule(CombinatorType combinatorType, Set<ExpertRule> subRules) {
        this.combinatorType = combinatorType;
        this.subRules = Set.copyOf(subRules);
    }

    @Override
    public DataType getDataType() {
        return DataType.COMBINATOR;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        return switch (combinatorType) {
            case AND -> subRules.stream().allMatch(rule -> rule.evaluateRule(identifiable));
            case OR -> subRules.stream().anyMatch(rule -> rule.evaluateRule(identifiable));
        };
    }

    @Override
    protected OperatorType getOperatorType() {
        throw new UnsupportedOperationException("Operator type not applicable for combinator rules");
    }
}
