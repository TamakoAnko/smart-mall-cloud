package com.xzj.goods.remote.order;

import com.xzj.promotion.promotion.entity.vos.GiftCardCashDeductPreviewVO;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class TradeDTO implements Serializable {
    private String sn;
    private String memberId;
    private String memberAddress;
    private String storeAddress;
    private CartTypeEnum cartTypeEnum;
    private List<CartVO> cartList;
    private List<CartSkuVO> skuList;
    private List<CartSkuVO> checkedSkuList;
    private PriceDetailDTO priceDetailDTO;
    private Double cashPayTotal;
    private Double giftCardDeductTotal;
    private List<GiftCardCashDeductPreviewVO.DeductItem> giftCardDeductItems;
}
