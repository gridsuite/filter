/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import jakarta.annotation.Nullable;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.List;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "filterType",
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = IdentifierListFilter.class, name = "IDENTIFIER_LIST"),
    @JsonSubTypes.Type(value = ExpertFilter.class, name = "EXPERT"),
})
public interface Filter {

    default List<Identifiable<?>> evaluate(Network network) {
        return evaluate(network, ReportNode.NO_OP);
    }

    List<Identifiable<?>> evaluate(Network network, ReportNode reportNode);

    EquipmentType getEquipmentType();

    FilterType getFilterType();

    @Nullable String getName();

    void setName(String name);
}
