package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Network;
import lombok.NonNull;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractGlobalFilterService implements FilterLoader {
    protected abstract Network getNetwork(@NonNull UUID networkUuid, @NonNull String variantId);

    protected List<String> getIdsFilter(@NonNull final UUID networkUuid, @NonNull final String variantId,
                                        @NonNull final GlobalFilter globalFilter, @NonNull final List<EquipmentType> equipmentTypes) {
        final Network network = getNetwork(networkUuid, variantId);
        final List<AbstractFilter> genericFilters = getFilters(globalFilter.getGenericFilter());
        return GlobalFilterUtils.applyGlobalFilterOnNetwork(network, globalFilter, genericFilters, equipmentTypes, this)
                // Filter equipments by type
                .values()
                .stream()
                .filter(Objects::nonNull)
                // Combine all results into one list
                .flatMap(List::stream)
                .toList();
    }
}
