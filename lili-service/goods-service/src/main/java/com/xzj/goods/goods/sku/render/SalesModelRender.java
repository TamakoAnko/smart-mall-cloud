package com.xzj.goods.goods.sku.render;

import com.xzj.goods.goods.entity.dos.GoodsSku;
import com.xzj.goods.goods.entity.dto.GoodsOperationDTO;

import java.util.List;

/**
 * 根据商品销售模型渲染商品sku
 *
 * @author paulG
 * @since 2022/5/20
 **/
public interface SalesModelRender {


    String getSalesMode();

    void renderSingle(GoodsSku goodsSku, GoodsOperationDTO goodsOperationDTO);

    void renderBatch(List<GoodsSku> goodsSkus, GoodsOperationDTO goodsOperationDTO);

}
