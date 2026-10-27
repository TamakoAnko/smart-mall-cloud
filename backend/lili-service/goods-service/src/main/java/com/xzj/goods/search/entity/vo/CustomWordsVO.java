package com.xzj.goods.search.entity.vo;

import com.xzj.goods.search.entity.dos.CustomWords;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author paulG
 * @since 2020/12/7
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CustomWordsVO extends CustomWords {

    private static final long serialVersionUID = 143299060233417009L;

    public CustomWordsVO(String name) {
        this.setName(name);
        this.setDisabled(1);
    }
}
