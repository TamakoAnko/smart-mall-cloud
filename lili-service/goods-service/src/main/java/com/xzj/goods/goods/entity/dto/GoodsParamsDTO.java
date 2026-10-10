package com.xzj.goods.goods.entity.dto;

import com.xzj.goods.goods.entity.dos.CategoryParameter;
import com.xzj.goods.goods.entity.dos.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品关联参数
 *
 * @author pikachu
 * @since 2020-02-23 9:14:33
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "商品参数DTO")
public class GoodsParamsDTO extends Parameters {

    @Schema(description = "参数关联的分类ID")
    private List<CategoryParameter> categoryParameterList;

}
