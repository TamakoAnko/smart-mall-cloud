package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import com.xzj.goods.goods.entity.vo.CardKeyVO;

@Data
public class OrderItem implements Serializable {
    private String sn;
    private String orderSn;
    private String skuId;
    private String goodsName;
    private Integer num;
    private Double flowPrice;
    private PriceDetailDTO priceDetailDTO;
    private String cardKeyFulfillStatus;
    private String cardKeyFulfillMessage;
    private List<CardKeyVO> cardKeys;
    private Boolean cardKeyDelivered;
}
