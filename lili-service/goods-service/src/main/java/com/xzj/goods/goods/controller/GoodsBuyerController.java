package com.xzj.goods.goods.controller;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import com.xzj.goods.search.entity.dos.GoodsIndex;
import com.xzj.goods.search.entity.dos.GoodsRelatedInfo;
import com.xzj.goods.search.entity.dto.GoodsSearchDTO;
import com.xzj.goods.search.service.GoodsSearchService;
import com.xzj.goods.search.service.HotWordsService;
import com.xzj.goods.metrics.PageViewPoint;
import com.xzj.goods.metrics.enums.PageViewEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzj.goods.goods.entity.Goods;
import com.xzj.goods.goods.entity.GoodsSku;
import com.xzj.goods.goods.entity.GoodsSkuQueryParams;
import com.xzj.goods.goods.entity.GoodsVO;
import com.xzj.goods.goods.service.GoodsQueryService;
import com.xzj.goods.goods.service.GoodsSkuQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 买家端商品接口。
 */
@Slf4j
@RestController
@Tag(name = "买家端,商品接口")
@RequestMapping("/buyer/goods/goods")
public class GoodsBuyerController {

    private final GoodsQueryService goodsQueryService;
    private final GoodsSkuQueryService goodsSkuQueryService;
    private final GoodsSearchService goodsSearchService;
    private final HotWordsService hotWordsService;

    /**
     * 注入当前已迁移的商品查询服务，以及暂时兼容旧模块的搜索服务。
     */
    public GoodsBuyerController(
            GoodsQueryService goodsQueryService,
            GoodsSkuQueryService goodsSkuQueryService,
            GoodsSearchService goodsSearchService,
            HotWordsService hotWordsService) {
        this.goodsQueryService = goodsQueryService;
        this.goodsSkuQueryService = goodsSkuQueryService;
        this.goodsSearchService = goodsSearchService;
        this.hotWordsService = hotWordsService;
    }

    /**
     * 根据商品 ID 查询商品详情。
     */
    @Operation(summary = "通过id获取商品信息")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @GetMapping("/get/{goodsId}")
    public ResultMessage<GoodsVO> get(
            @NotNull(message = "商品ID不能为空")
            @PathVariable String goodsId) {
        return ResultUtil.data(goodsQueryService.getGoodsVO(goodsId));
    }

    /**
     * 查询已上架商品分页，并支持按分类筛选。
     */
    @Operation(summary = "获取商品分页列表")
    @GetMapping
    public ResultMessage<IPage<Goods>> getByPage(
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String categoryId) {
        return ResultUtil.data(goodsQueryService.page(pageNumber, pageSize, categoryId));
    }

    /**
     * 根据商品 ID 和 SKU ID 查询具体规格详情。
     */
    @Operation(summary = "通过id获取商品SKU信息")
    @Parameters({
            @Parameter(name = "goodsId", description = "商品ID", required = true),
            @Parameter(name = "skuId", description = "SKU ID", required = true)
    })
    @GetMapping("/sku/{goodsId}/{skuId}")
    @PageViewPoint(type = PageViewEnum.SKU, id = "#skuId")
    public ResultMessage<Map<String, Object>> getSku(
            @NotNull(message = "商品ID不能为空") @PathVariable String goodsId,
            @NotNull(message = "SKU ID不能为空") @PathVariable String skuId) {
        try {
            return ResultUtil.data(goodsSkuQueryService.getGoodsSkuDetail(goodsId, skuId));
        } catch (ServiceException exception) {
            log.info(exception.getMsg(), exception);
            throw exception;
        } catch (Exception exception) {
            log.error(ResultCode.GOODS_ERROR.message(), exception);
            return ResultUtil.error(ResultCode.GOODS_ERROR);
        }
    }

    /**
     * 查询商品 SKU 列表。
     */
    @Operation(summary = "获取商品SKU列表")
    @GetMapping("/sku")
    public ResultMessage<List<GoodsSku>> getSkuByPage(GoodsSkuQueryParams goodsSkuQueryParams) {
        return ResultUtil.data(goodsSkuQueryService.list(goodsSkuQueryParams));
    }

    /**
     * 分页搜索商品。
     */
    @Operation(summary = "商品搜索分页")
    @GetMapping("/search")
    public ResultMessage<Page<GoodsIndex>> searchGoods(
            GoodsSearchDTO goodsSearchParams,
            PageVO pageVO) {
        pageVO.setNotConvert(true);
        return ResultUtil.data(goodsSearchService.searchGoodsByPage(goodsSearchParams, pageVO));
    }

    /**
     * 查询商品搜索筛选器。
     */
    @Operation(summary = "商品搜索筛选器")
    @GetMapping("/search/related")
    public ResultMessage<GoodsRelatedInfo> searchGoodsRelated(
            GoodsSearchDTO goodsSearchParams,
            PageVO pageVO) {
        pageVO.setNotConvert(true);
        return ResultUtil.data(goodsSearchService.getSelector(goodsSearchParams, pageVO));
    }

    /**
     * 查询搜索热词。
     */
    @Operation(summary = "获取搜索热词")
    @GetMapping("/hot-words")
    public ResultMessage<List<String>> getGoodsHotWords(Integer count) {
        return ResultUtil.data(hotWordsService.getHotWords(count));
    }
}
