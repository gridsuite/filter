/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("checkstyle:AbstractClassName")
class AbstractFilterTest {

    @Test
    void toFilterEquipmentsMapsIdentifiablesAndFilterId() {
        UUID filterId = UUID.randomUUID();
        ExpertFilter filter = ExpertFilter.builder().id(filterId).build();
        Identifiable<?> line = mock(Identifiable.class);
        when(line.getId()).thenReturn("LINE_1");
        when(line.getType()).thenReturn(IdentifiableType.LINE);
        Identifiable<?> generator = mock(Identifiable.class);
        when(generator.getId()).thenReturn("GENERATOR_1");
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);

        FilterEquipments result = filter.toFilterEquipments(List.of(line, generator));

        assertThat(result.getFilterId()).isEqualTo(filterId);
        assertThat(result.getIdentifiableAttributes()).containsExactly(
                new IdentifiableAttributes("LINE_1", IdentifiableType.LINE, null),
                new IdentifiableAttributes("GENERATOR_1", IdentifiableType.GENERATOR, null));
        assertThat(result.getNotFoundEquipments()).isNull();
    }
}
