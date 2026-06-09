/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.identifier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.AbstractFilter;

import java.util.Objects;
import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class IdentifierListFilter extends AbstractFilter {

    private Set<String> equipmentIds;

    @Builder
    public IdentifierListFilter(EquipmentType equipmentType, Set<String> equipmentIds) {
        super(equipmentType);
        this.equipmentIds = Set.copyOf(Objects.requireNonNull(equipmentIds));
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public FilterType getFilterType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    protected boolean evaluateFilterRule(Identifiable<?> identifiable) {
        Objects.requireNonNull(identifiable);
        return equipmentIds.contains(identifiable.getId());
    }
}
