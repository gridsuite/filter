/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert;

import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.AbstractFilter;
import org.gridsuite.filter.wip.expert.rule.ExpertRule;

import java.util.Objects;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public class ExpertFilter extends AbstractFilter {

    private final ExpertRule rule;

    public ExpertFilter(EquipmentType equipmentType, ExpertRule rule) {
        super(equipmentType);
        this.rule = Objects.requireNonNull(rule);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.EXPERT;
    }

    @Override
    protected boolean evaluateFilterRule(Identifiable<?> identifiable) {
        return rule.evaluateRule(identifiable);
    }
}
