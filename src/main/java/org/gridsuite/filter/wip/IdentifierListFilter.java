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
import com.google.common.collect.Sets;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import lombok.*;
import org.gridsuite.filter.report.FilterReportResourceBundle;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifierListFilter implements Filter {

    private EquipmentType equipmentType;
    private Set<String> equipmentIds;

    @Builder
    public IdentifierListFilter(EquipmentType equipmentType, Set<String> equipmentIds) {
        this.equipmentType = Objects.requireNonNull(equipmentType);
        this.equipmentIds = Set.copyOf(Objects.requireNonNull(equipmentIds));
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public FilterType getFilterType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    public boolean evaluateFilterRule(Identifiable<?> identifiable) {
        Objects.requireNonNull(identifiable);
        return equipmentIds.contains(identifiable.getId());
    }

    @Override
    public List<Identifiable<?>> evaluate(Network network, TopologyKind topologyKind, ReportNode reportNode) {
        var result = Filter.super.evaluate(network, topologyKind, reportNode);
        Set<String> found = result.stream().map(Identifiable::getId).collect(toSet());
        Set<String> notFound = Sets.difference(equipmentIds, found);

        if (!notFound.isEmpty()) {
            reportNodeRoot.newReportNode()
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .withResourceBundles(FilterReportResourceBundle.BASE_NAME)
                    .withMessageTemplate("filter.evaluation.listFilter.notFound")
                    .withUntypedValue("equipementType", equipmentType.name())
                    .withUntypedValue("searchCount", equipmentIds.size())
                    .withUntypedValue("notFoundCount", notFound.size())
                    .withUntypedValue("notFoundIds", String.join(", ",
                            notFound.stream().sorted().toList()))
                    .add();
        } else {
            reportNodeRoot.newReportNode()
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .withResourceBundles(FilterReportResourceBundle.BASE_NAME)
                    .withMessageTemplate("filter.evaluation.listFilter.allFound")
                    .withUntypedValue("equipementType", equipmentType.name())
                    .withUntypedValue("searchCount", equipmentIds.size())
                    .add();
        }
        return result;
    }
}
