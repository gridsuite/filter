/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.criteriafilter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

@AllArgsConstructor
@Data
public class InjectionFilterAttributes {
    private String equipmentID;

    private String equipmentName;

    private String substationName;

    private SortedSet<String> countries;

    private Map<String, List<String>> substationFreeProperties;

    private Map<String, List<String>> freeProperties;

    private NumericalFilter nominalVoltage;
}
