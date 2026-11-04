/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import lombok.*;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifierListFilter implements Filter {

    public static final String SEARCH_COUNT = "searchCount";
    public static final String EQUIPMENT_TYPE = "equipmentType";
    private EquipmentType equipmentType;
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<String> equipmentIds;
    private String name;

    public IdentifierListFilter(EquipmentType equipmentType, Set<String> equipmentIds) {
        this(equipmentType, equipmentIds, null);
    }

    @Builder
    public IdentifierListFilter(EquipmentType equipmentType, Set<String> equipmentIds, String name) {
        this.equipmentType = Objects.requireNonNull(equipmentType);
        this.equipmentIds = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(equipmentIds)));
        this.name = name;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public FilterType getFilterType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Identifiable<?>> evaluate(Network network, ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate("filter.evaluation.listFilter.evaluate")
                .withUntypedValue(SEARCH_COUNT, equipmentIds.size())
                .withUntypedValue(EQUIPMENT_TYPE, equipmentType.name())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();

        Map<String, Identifiable<?>> foundById = NetworkUtils.getEquipmentStream(network, getEquipmentType(), TopologyKind.NODE_BREAKER)
                .filter(identifiable -> equipmentIds.contains(identifiable.getId()))
                .collect(toMap(Identifiable::getId, Function.identity()));
        List<Identifiable<?>> orderedResult = equipmentIds.stream()
                .<Identifiable<?>>map(foundById::get)
                .filter(Objects::nonNull)
                .toList();

        Set<String> foundIds = foundById.keySet();
        Set<String> notFoundIds = Sets.difference(equipmentIds, foundIds);

        if (!foundIds.isEmpty() && !notFoundIds.isEmpty()) {
            final ReportNode node = reportNode.newReportNode()
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .withMessageTemplate("filter.evaluation.listFilter.notFound")
                    .withUntypedValue("notFoundCount", notFoundIds.size())
                    .add();

            notFoundIds.stream().sorted().forEach(id -> node.newReportNode()
                .withSeverity(TypedValue.DETAIL_SEVERITY)
                .withMessageTemplate("filter.evaluation.listFilter.notFoundId")
                .withUntypedValue("id", id)
                .add()
            );
        }
        ReportUtils.reportMatchingEquipmentsCount(foundIds.size(), reportNode);
        return orderedResult;
    }
}
