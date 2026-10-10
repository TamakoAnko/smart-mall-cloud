package com.xzj.goods.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xzj.goods.category.entity.Category;
import com.xzj.goods.category.entity.CategoryVO;
import com.xzj.goods.category.mapper.GoodsCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 商品分类查询服务实现。
 */
@Service
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final GoodsCategoryMapper categoryMapper;

    /**
     * 注入分类数据访问对象。
     */
    public CategoryQueryServiceImpl(GoodsCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 查询所有未删除分类，再从指定父节点开始递归构建分类树。
     */
    @Override
    public List<CategoryVO> listAllChildren(String parentId) {
        // 一次性查询分类，避免递归过程中重复访问数据库。
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getDeleteFlag, false)
                .orderByAsc(Category::getLevel)
                .orderByAsc(Category::getSortOrder));
        return buildChildren(categories, parentId);
    }

    /**
     * 从分类列表中递归查找指定父节点的直接子节点。
     */
    private List<CategoryVO> buildChildren(List<Category> categories, String parentId) {
        List<CategoryVO> children = new ArrayList<>();
        for (Category category : categories) {
            if (parentId.equals(category.getParentId())) {
                CategoryVO view = new CategoryVO(category);
                view.setChildren(buildChildren(categories, category.getId()));
                children.add(view);
            }
        }
        children.sort(Comparator.comparing(CategoryVO::getSortOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return children;
    }
}