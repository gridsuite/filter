/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.identifierlistfilter;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.wip.Filter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdentifierListFilterTest {

    @Test
    void toFilterEquipmentsReturnsMatchingAttributesInFilterOrderAndMissingIds() {
        UUID filterId = UUID.randomUUID();
        IdentifierListFilter filter = IdentifierListFilter.builder()
                .id(filterId)
                .filterEquipmentsAttributes(List.of(
                        new IdentifierListFilterEquipmentAttributes("GENERATOR_1", 0.5),
                        new IdentifierListFilterEquipmentAttributes("MISSING", 1.0),
                        new IdentifierListFilterEquipmentAttributes("LINE_1", 0.5)))
                .build();
        Identifiable<?> line = mock(Identifiable.class);
        when(line.getId()).thenReturn("LINE_1");
        when(line.getType()).thenReturn(IdentifiableType.LINE);
        Identifiable<?> generator = mock(Identifiable.class);
        when(generator.getId()).thenReturn("GENERATOR_1");
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);

        FilterEquipments result = filter.toFilterEquipments(List.of(line, generator));

        assertThat(result.getFilterId()).isEqualTo(filterId);
        assertThat(result.getIdentifiableAttributes()).containsExactly(
                new IdentifiableAttributes("GENERATOR_1", IdentifiableType.GENERATOR, null),
                new IdentifiableAttributes("LINE_1", IdentifiableType.LINE, null));
        assertThat(result.getNotFoundEquipments()).containsExactly("MISSING");
    }

    @Test
    void toFilterCopiesEquipmentTypeAndIds() {
        IdentifierListFilter filter = IdentifierListFilter.builder()
                .equipmentType(EquipmentType.GENERATOR)
                .filterEquipmentsAttributes(List.of(
                        new IdentifierListFilterEquipmentAttributes("GENERATOR_1", null),
                        new IdentifierListFilterEquipmentAttributes("GENERATOR_2", null)))
                .build();

        Filter converted = filter.toFilter(null);

        assertThat(converted).isInstanceOf(org.gridsuite.filter.wip.IdentifierListFilter.class);
        org.gridsuite.filter.wip.IdentifierListFilter convertedFilter =
                (org.gridsuite.filter.wip.IdentifierListFilter) converted;
        assertThat(convertedFilter.getEquipmentType()).isEqualTo(EquipmentType.GENERATOR);
        assertThat(convertedFilter.getEquipmentIds()).containsExactlyInAnyOrder("GENERATOR_1", "GENERATOR_2");
    }
}
