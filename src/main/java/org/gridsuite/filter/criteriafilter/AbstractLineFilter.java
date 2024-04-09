/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.criteriafilter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public abstract class AbstractLineFilter extends AbstractEquipmentFilterForm {
    private String substationName1;

    private String substationName2;

    private SortedSet<String> countries1;

    private SortedSet<String> countries2;

    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties1;

    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties2;

    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties;

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                && substationName1 == null
                && substationName2 == null
                && CollectionUtils.isEmpty(countries1)
                && CollectionUtils.isEmpty(countries2)
                && MapUtils.isEmpty(freeProperties1)
                && MapUtils.isEmpty(freeProperties2)
                && MapUtils.isEmpty(freeProperties);
    }
}
