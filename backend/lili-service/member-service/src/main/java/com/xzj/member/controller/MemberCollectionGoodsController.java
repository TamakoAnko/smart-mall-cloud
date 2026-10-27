package com.xzj.member.controller;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import com.xzj.member.service.GoodsCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端,客户收藏接口
 *
 * @author Chopper
 * @since 2020/11/17 2:32 下午
 */
@RestController
@Tag(name = "买家端,客户商品收藏接口")
@RequestMapping("/buyer/member/collection")
public class MemberCollectionGoodsController {

    /**
     * 客户商品收藏
     */
    @Autowired
    private GoodsCollectionService goodsCollectionService;

    @Operation(summary = "查询客户收藏列表")
    @GetMapping("/GOODS")
    public ResultMessage<Object> goodsList(PageVO page) {
        return ResultUtil.data(goodsCollectionService.goodsCollection(page));
    }

    @Operation(summary = "添加客户收藏")
    @Parameters({
            @Parameter(name = "num", description = "值")
    })
    @PostMapping("/add/GOODS/{id}")
    public ResultMessage<Object> addGoodsCollection(@NotNull(message = "值不能为空") @PathVariable String id) {
        return ResultUtil.data(goodsCollectionService.addGoodsCollection(id));

    }

    @Operation(summary = "删除客户收藏")
    @Parameters({
            @Parameter(name = "type", description = "类型", example = "GOODS:商品,STORE:店铺"),
            @Parameter(name = "num", description = "值")
    })
    @DeleteMapping("/delete/GOODS/{id}")
    public ResultMessage<Object> deleteGoodsCollection(@NotNull(message = "值不能为空") @PathVariable String id) {
        return ResultUtil.data(goodsCollectionService.deleteGoodsCollection(id));
    }

    @Operation(summary = "查询客户是否收藏")
    @Parameters({
            @Parameter(name = "type", description = "类型", example = "GOODS:商品,STORE:店铺"),
            @Parameter(name = "id", description = "值")
    })
    @GetMapping("/isCollection/GOODS/{id}")
    public ResultMessage<Boolean> isCollection(@NotNull(message = "值不能为空") @PathVariable String id) {
        // 未登录时商品详情页也会查询收藏状态，此时直接返回未收藏，避免下游服务访问空用户。
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return ResultUtil.data(false);
        }
        return ResultUtil.data(this.goodsCollectionService.isCollection(id));
    }
}