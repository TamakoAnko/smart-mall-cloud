package com.xzj.promotion.promotion.entity.dto;

import com.xzj.goods.goods.entity.dos.GoodsSku;
import com.xzj.promotion.promotion.entity.dos.PromotionGoods;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 促销商品数据传输对象
 *
 * @author paulG
 * @since 2020/10/9
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PromotionGoodsDTO extends PromotionGoods {

    private static final long serialVersionUID = 9206970681612883421L;

    @Schema(description = "商品id")
    private String goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片")
    private String goodsImage;

    public PromotionGoodsDTO(GoodsSku sku) {
        super(sku);
    }
}
