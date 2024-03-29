/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.identifierlistfilter;

import com.powsybl.iidm.network.IdentifiableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifiableAttributes {
    private String id;

    private IdentifiableType type;

    private Double distributionKey;
}
