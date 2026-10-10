package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;
import com.xzj.goods.goods.entity.dos.GoodsSku;

@Data
public class CartSkuVO implements Serializable {
    private String storeId;
    private Integer num;
    private String deliveryMethod;
    private GoodsSku goodsSku;
}
