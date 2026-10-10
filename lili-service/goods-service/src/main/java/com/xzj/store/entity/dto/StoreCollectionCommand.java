package com.xzj.store.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Store collection counter command owned by the goods service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreCollectionCommand {
    private String id;
    private Integer num;
}
