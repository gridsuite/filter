package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Network;
import lombok.NonNull;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractGlobalFilterService implements FilterLoader {
    protected abstract Network getNetwork(@NonNull UUID networkUuid, @NonNull String variantId);

    /**
     * Get filtered equipment IDs.
     * @param networkUuid the network to load
     * @param variantId the network variant to work on
     * @param globalFilter the filter(s) to apply
     * @param equipmentTypes the {@link EquipmentType equipment types} to filter
     * @return the {@link List list} of {@link UUID IDs} of filtered {@link EquipmentType equipments}.
     */
    protected List<String> getFilteredIds(@NonNull final UUID networkUuid, @NonNull final String variantId,
                                          @NonNull final GlobalFilter globalFilter, @NonNull final List<EquipmentType> equipmentTypes) {
        final Network network = getNetwork(networkUuid, variantId);
        return GlobalFilterUtils.applyGlobalFilterOnNetwork(network, globalFilter, List.of(), equipmentTypes, this)
            .values()
            .stream()
            .filter(Objects::nonNull)
            // Combine all results into one list
            .flatMap(List::stream)
            .toList();
    }
}
