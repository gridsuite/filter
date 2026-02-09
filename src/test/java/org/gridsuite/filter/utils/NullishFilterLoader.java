package org.gridsuite.filter.utils;

import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;

import java.util.List;
import java.util.UUID;

public class NullishFilterLoader implements FilterLoader {
    @Override
    public List<AbstractFilter> getFilters(List<UUID> uuids) {
        return null;
    }

}
