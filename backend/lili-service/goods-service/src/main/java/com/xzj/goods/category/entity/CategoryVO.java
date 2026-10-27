package com.xzj.goods.category.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryVO extends Category {
    private List<CategoryVO> children;
    public CategoryVO(Category category) {
        setId(category.getId());
        setCreateBy(category.getCreateBy());
        setCreateTime(category.getCreateTime());
        setUpdateBy(category.getUpdateBy());
        setUpdateTime(category.getUpdateTime());
        setDeleteFlag(category.getDeleteFlag());
        setName(category.getName());
        setParentId(category.getParentId());
        setLevel(category.getLevel());
        setSortOrder(category.getSortOrder());
        setCommissionRate(category.getCommissionRate());
        setImage(category.getImage());
        setSupportChannel(category.getSupportChannel());
    }
}
