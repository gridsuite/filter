package org.gridsuite.filter.model;

import java.util.List;

public record FilterEquipments(List<String> foundEquipments, List<String> notFoundEquipments) {
}
