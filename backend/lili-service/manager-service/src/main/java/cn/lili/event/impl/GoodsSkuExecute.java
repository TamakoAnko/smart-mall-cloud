package cn.lili.event.impl;


import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.event.GoodsCommentCompleteEvent;
import cn.lili.event.StoreSettingChangeEvent;
import cn.lili.modules.member.entity.dos.MemberEvaluation;
import cn.lili.modules.store.entity.dos.Store;
import com.xzj.config.GoodsFeignClient;
import com.xzj.config.GoodsSkuFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品SKU变化
 *
 * @author Chopper
 * @since 2020-07-03 11:20
 */
@Service
public class GoodsSkuExecute implements GoodsCommentCompleteEvent, StoreSettingChangeEvent {

    /**
     * 商品
     */
    @Autowired
    private GoodsFeignClient goodsFeignClient;

    @Autowired
    private GoodsSkuFeignClient goodsSkuFeignClient;

    @Autowired
    private Cache cache;

    @Override
    public void goodsComment(MemberEvaluation memberEvaluation) {
        goodsFeignClient.updateCommentCount(memberEvaluation.getGoodsId(), memberEvaluation.getSkuId());
    }

    @Override
    public void storeSettingChange(Store store) {
        //修改数据后，清除商品索引
        List<String> goodsSkuKeys = new ArrayList<>();
        for (com.xzj.entity.GoodsSkuDTO goodsSku : goodsSkuFeignClient.findByStoreId(store.getId())) {
            goodsSkuKeys.add(CachePrefix.GOODS_SKU.getPrefix() + goodsSku.getId());
        }
        cache.multiDel(goodsSkuKeys);
    }
}
