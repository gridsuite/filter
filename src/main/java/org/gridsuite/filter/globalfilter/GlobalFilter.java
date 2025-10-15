/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Country;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author maissa Souissi <maissa.souissi at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldNameConstants
// TODO convert to record when loadflow-server and computation lib stop to extends it
public class GlobalFilter {
    private List<String> nominalV;
    private List<Country> countryCode;
    private List<UUID> genericFilter;
    private Map<String, List<String>> substationProperty;

    /**
     * @return {@code true} if all filter parameters are empty, else {@code false}.
     */
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(this.nominalV)
            && CollectionUtils.isEmpty(this.countryCode)
            && CollectionUtils.isEmpty(this.genericFilter)
            && (MapUtils.isEmpty(this.substationProperty) || this.substationProperty.values().stream().allMatch(CollectionUtils::isEmpty));
    }
}
