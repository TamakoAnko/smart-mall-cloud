package com.xzj.goods.category.service;

import com.xzj.goods.category.entity.CategoryVO;

import java.util.List;

/**
 * 商品分类查询服务。
 */
public interface CategoryQueryService {

    /**
     * 查询指定父分类下的子分类，并组装成树形结构。
     */
    List<CategoryVO> listAllChildren(String parentId);
}