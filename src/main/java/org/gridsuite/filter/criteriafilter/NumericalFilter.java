/**
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.criteriafilter;

import lombok.*;
import org.gridsuite.filter.utils.RangeType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumericalFilter {
    private RangeType type;
    private Double value1;
    private Double value2;
}
