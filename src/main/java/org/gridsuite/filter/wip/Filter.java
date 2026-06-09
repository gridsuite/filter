/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.expert.ExpertFilter;
import org.gridsuite.filter.wip.identifier.IdentifierListFilter;

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

    List<Identifiable<?>> evaluate(Network network);

    List<Identifiable<?>> evaluate(Network network, TopologyKind topologyKind);

    FilterType getFilterType();
}
