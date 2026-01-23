/*
 *  Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.utils.expertfilter;

import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.exception.FilterCycleException;
import org.gridsuite.filter.expertfilter.ExpertFilterDto;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRuleDto;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRuleDto;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRuleDto;

import java.util.*;

/**
 * @author Mohamed BENREJEB <mohamed.ben-rejeb at rte-france.com>
 */
public final class FilterCycleDetector {

    private FilterCycleDetector() {
        // Utility class
    }

    /**
     * Check that there is no cycle starting from the provided filter.
     *
     * @param filter       the starting filter
     * @param filterLoader loader used to retrieve referenced filters
     */
    public static void checkNoCycle(AbstractFilterDto filter, FilterLoader filterLoader) {
        checkNoCycle(filter, filterLoader, new ArrayList<>());
    }

    private static void checkNoCycle(AbstractFilterDto filter, FilterLoader loader,
                                     List<UUID> visiting) {
        UUID id = filter.getId();
        if (id != null) {
            if (visiting.contains(id)) {
                visiting.addLast(id);
                int startIndex = visiting.indexOf(id);
                throw new FilterCycleException("Cycle detected in filters", visiting.subList(startIndex, visiting.size()));
            }
            visiting.addLast(id);
        }

        if (filter instanceof ExpertFilterDto expertFilter) {
            checkRule(expertFilter.getRules(), loader, visiting);
        }

        if (id != null && !visiting.isEmpty()) {
            visiting.removeLast();
        }
    }

    private static void checkRule(AbstractExpertRuleDto rule, FilterLoader loader,
                                  List<UUID> visiting) {
        switch (rule) {
            case CombinatorExpertRuleDto combinatorRule -> {
                List<AbstractExpertRuleDto> rules = combinatorRule.getRules();
                if (rules != null) {
                    for (AbstractExpertRuleDto r : rules) {
                        checkRule(r, loader, visiting);
                    }
                }
            }
            case FilterUuidExpertRuleDto uuidRule -> {
                Set<String> values = uuidRule.getValues();
                if (values != null) {
                    for (String value : values) {
                        UUID refId = UUID.fromString(value);
                        List<AbstractFilterDto> referenced = loader.getFilters(List.of(refId));
                        if (!referenced.isEmpty() && referenced.getFirst() != null) {
                            checkNoCycle(referenced.getFirst(), loader, visiting);
                        }
                    }
                }
            }
            default -> {
                // do nothing
            }
        }
    }
}

