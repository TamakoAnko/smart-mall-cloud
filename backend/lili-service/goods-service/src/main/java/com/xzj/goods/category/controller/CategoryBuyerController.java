package com.xzj.goods.category.controller;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import com.xzj.goods.category.entity.CategoryVO;
import com.xzj.goods.category.service.CategoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类控制器。
 */
@RestController
@Tag(name = "买家端,商品分类接口")
@RequestMapping("/buyer/goods/category")
public class CategoryBuyerController {

    private final CategoryQueryService categoryQueryService;

    /**
     * 注入商品分类查询服务。
     */
    public CategoryBuyerController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    /**
     * 根据父分类 ID 查询分类树。
     * parentId=0 表示从根分类开始查询。
     */
    @Operation(summary = "获取商品分类列表")
    @Parameter(name = "parentId", description = "上级分类ID，全部分类为：0", required = true)
    @GetMapping("/get/{parentId}")
    public ResultMessage<List<CategoryVO>> list(
            @NotNull(message = "分类ID不能为空")
            @PathVariable String parentId) {
        return ResultUtil.data(categoryQueryService.listAllChildren(parentId));
    }
}