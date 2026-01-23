package org.gridsuite.filter.model;

import lombok.*;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterEquipments {

    private List<String> foundEquipments;

    private List<String> notFoundEquipments;
}
