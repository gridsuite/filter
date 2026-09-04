/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.wip;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
public final class ReportUtils {

    private ReportUtils() {
        // Should not be instantiated
    }

    public static void reportMatchingEquipmentsCount(int matchingEquipmentsCount, ReportNode reportNode) {
        if (matchingEquipmentsCount == 0) {
            reportNode.newReportNode()
                    .withMessageTemplate("filter.evaluation.general.noMatchingEquipment")
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
        } else {
            reportNode.newReportNode()
                    .withMessageTemplate("filter.evaluation.general.countMatchingEquipments")
                    .withUntypedValue("count", matchingEquipmentsCount)
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
        }
    }
}
