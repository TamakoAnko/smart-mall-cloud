package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class CartVO implements Serializable {
    private String deliveryMethod;
    private List<CartSkuVO> skuList;
}
