package com.xzj.goods.goods.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xzj.goods.goods.entity.Goods;
import com.xzj.goods.goods.entity.GoodsSku;
import com.xzj.goods.goods.entity.GoodsSkuQueryParams;
import com.xzj.goods.goods.entity.GoodsSkuVO;
import com.xzj.goods.goods.mapper.GoodsSkuQueryMapper;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import com.xzj.goods.goods.entity.vos.GoodsSkuSpecVO;
import com.xzj.goods.goods.entity.vos.SpecValueVO;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品 SKU 查询服务实现。
 */
@Service
public class GoodsSkuQueryServiceImpl implements GoodsSkuQueryService {

    private final GoodsSkuQueryMapper goodsSkuMapper;
    private final GoodsQueryService goodsQueryService;

    /**
     * 注入 SKU 数据访问对象和商品查询服务。
     */
    public GoodsSkuQueryServiceImpl(
            GoodsSkuQueryMapper goodsSkuMapper,
            GoodsQueryService goodsQueryService) {
        this.goodsSkuMapper = goodsSkuMapper;
        this.goodsQueryService = goodsQueryService;
    }

    /**
     * 查询商品和 SKU，并组装为原接口使用的 Map 返回结构。
     */
    @Override
    public Map<String, Object> getGoodsSkuDetail(String goodsId, String skuId) {
        Goods goods = goodsQueryService.getGoodsVO(goodsId);
        if (goods == null || !Boolean.FALSE.equals(goods.getDeleteFlag())
                || !"UPPER".equals(goods.getMarketEnable())) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        // 首页装修数据可能保留旧 SKU，先按请求的 SKU 查询。
        GoodsSku sku = null;
        if (skuId != null && !skuId.isBlank() && !"undefined".equals(skuId)) {
            sku = goodsSkuMapper.selectOne(new LambdaQueryWrapper<GoodsSku>()
                    .eq(GoodsSku::getId, skuId)
                    .eq(GoodsSku::getGoodsId, goodsId)
                    .eq(GoodsSku::getDeleteFlag, false));
        }
        // 商品仍然上架但 SKU 已失效时，回退到该商品的第一个有效 SKU。
        if (sku == null) {
            sku = goodsSkuMapper.selectOne(new LambdaQueryWrapper<GoodsSku>()
                    .eq(GoodsSku::getGoodsId, goodsId)
                    .eq(GoodsSku::getDeleteFlag, false)
                    .eq(GoodsSku::getMarketEnable, "UPPER")
                    .orderByAsc(GoodsSku::getCreateTime)
                    .last("LIMIT 1"));
        }
        if (sku == null) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", toVO(sku));
        // Return all valid SKUs so the detail page can switch specifications.
        List<GoodsSku> skuList = goodsSkuMapper.selectList(new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getGoodsId, goodsId)
                .eq(GoodsSku::getDeleteFlag, false)
                .eq(GoodsSku::getMarketEnable, "UPPER"));
        result.put("specs", toSkuSpecs(skuList));
        result.put("promotionMap", Collections.emptyMap());
        result.put("categoryName", Collections.emptyList());
        return result;
    }

    /**
     * 按条件查询未删除 SKU。
     */
    @Override
    public List<GoodsSku> list(GoodsSkuQueryParams queryParams) {
        LambdaQueryWrapper<GoodsSku> wrapper = new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getDeleteFlag, false)
                .eq(queryParams.getGoodsId() != null && !queryParams.getGoodsId().isBlank(),
                        GoodsSku::getGoodsId, queryParams.getGoodsId())
                .eq(queryParams.getSkuId() != null && !queryParams.getSkuId().isBlank(),
                        GoodsSku::getId, queryParams.getSkuId())
                .like(queryParams.getGoodsName() != null && !queryParams.getGoodsName().isBlank(),
                        GoodsSku::getGoodsName, queryParams.getGoodsName())
                .like(queryParams.getSn() != null && !queryParams.getSn().isBlank(),
                        GoodsSku::getSn, queryParams.getSn())
                .like(queryParams.getSimpleSpecs() != null && !queryParams.getSimpleSpecs().isBlank(),
                        GoodsSku::getSimpleSpecs, queryParams.getSimpleSpecs())
                .eq(queryParams.getStoreId() != null && !queryParams.getStoreId().isBlank(),
                        GoodsSku::getStoreId, queryParams.getStoreId())
                .like(queryParams.getCategoryPath() != null && !queryParams.getCategoryPath().isBlank(),
                        GoodsSku::getCategoryPath, queryParams.getCategoryPath())
                .eq(queryParams.getMarketEnable() != null && !queryParams.getMarketEnable().isBlank(),
                        GoodsSku::getMarketEnable, queryParams.getMarketEnable());
        if (queryParams.getSkuIds() != null && !queryParams.getSkuIds().isEmpty()) {
            wrapper.in(GoodsSku::getId, queryParams.getSkuIds());
        }
        return goodsSkuMapper.selectList(wrapper);
    }

    /**
     * 将数据库实体转换为接口返回对象。
     */
    private com.xzj.goods.goods.entity.vos.GoodsSkuVO toVO(GoodsSku sku) {
        com.xzj.goods.goods.entity.vos.GoodsSkuVO view = new com.xzj.goods.goods.entity.vos.GoodsSkuVO();
        BeanUtils.copyProperties(sku, view);
        List<SpecValueVO> specList = parseSpecList(sku.getSpecs());
        view.setSpecList(specList);
        List<String> galleryList = new ArrayList<>();
        for (SpecValueVO spec : specList) {
            if ("images".equals(spec.getSpecName()) && spec.getSpecImage() != null) {
                galleryList.addAll(spec.getSpecImage());
            }
        }
        if (galleryList.isEmpty()) {
            if (sku.getOriginal() != null && !sku.getOriginal().isBlank()) {
                galleryList.add(sku.getOriginal());
            } else if (sku.getThumbnail() != null && !sku.getThumbnail().isBlank()) {
                galleryList.add(sku.getThumbnail());
            }
        }
        view.setGoodsGalleryList(galleryList);
        return view;
    }

    /** Convert SKU specification JSON to the detail page structure. */
    private List<GoodsSkuSpecVO> toSkuSpecs(List<GoodsSku> skuList) {
        List<GoodsSkuSpecVO> result = new ArrayList<>();
        for (GoodsSku sku : skuList) {
            GoodsSkuSpecVO spec = new GoodsSkuSpecVO();
            spec.setSkuId(sku.getId());
            spec.setQuantity(sku.getQuantity());
            spec.setSpecValues(parseSpecList(sku.getSpecs()));
            result.add(spec);
        }
        return result;
    }

    /** Parse nullable or malformed historical specification JSON safely. */
    private List<SpecValueVO> parseSpecList(String specs) {
        List<SpecValueVO> result = new ArrayList<>();
        if (specs == null || specs.isBlank()) {
            return result;
        }
        try {
            JSONObject json = JSON.parseObject(specs);
            for (Map.Entry<String, Object> entry : json.entrySet()) {
                SpecValueVO value = new SpecValueVO();
                value.setSpecName(entry.getKey());
                if ("images".equals(entry.getKey()) && entry.getValue() instanceof JSONArray) {
                    List<String> imageUrls = new ArrayList<>();
                    for (Object image : (JSONArray) entry.getValue()) {
                        if (image instanceof JSONObject) {
                            String url = ((JSONObject) image).getString("url");
                            if (url != null && !url.isBlank()) {
                                imageUrls.add(url);
                            }
                        } else if (image != null) {
                            imageUrls.add(String.valueOf(image));
                        }
                    }
                    value.setSpecImage(imageUrls);
                } else if (entry.getValue() != null) {
                    value.setSpecValue(String.valueOf(entry.getValue()));
                }
                result.add(value);
            }
        } catch (RuntimeException ignored) {
            // Keep the product page usable when old data contains invalid JSON.
        }
        return result;
     }
}
