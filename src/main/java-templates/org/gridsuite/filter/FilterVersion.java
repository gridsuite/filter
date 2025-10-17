/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter;

import com.google.auto.service.AutoService;
import com.powsybl.tools.AbstractVersion;
import com.powsybl.tools.Version;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(Version.class)
public class FilterVersion extends AbstractVersion {

    public FilterVersion() {
        super("filter", "${project.version}", "${buildNumber}", "${scmBranch}", Long.parseLong("${timestamp}"));
    }
}
