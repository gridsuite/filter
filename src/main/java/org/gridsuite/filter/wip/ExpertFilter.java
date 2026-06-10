/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.rule.ExpertRule;

import java.util.Objects;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Data
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpertFilter implements Filter {

    private EquipmentType equipmentType;
    private ExpertRule rule;

    @Builder
    public ExpertFilter(EquipmentType equipmentType, ExpertRule rule) {
        this.equipmentType = Objects.requireNonNull(equipmentType);
        this.rule = Objects.requireNonNull(rule);
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public FilterType getFilterType() {
        return FilterType.EXPERT;
    }

    @Override
    public boolean evaluateFilterRule(Identifiable<?> identifiable) {
        Objects.requireNonNull(identifiable);

        return rule.evaluateRule(identifiable);
    }

    @Override
    public void clearEvaluationCache() {
        rule.clearCache();
    }
}
