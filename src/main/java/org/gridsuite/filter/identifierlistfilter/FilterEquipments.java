package org.gridsuite.filter.identifierlistfilter;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterEquipments {
    private UUID filterId;

    private List<IdentifiableAttributes> identifiableAttributes;

    private List<String> notFoundEquipments;
}
