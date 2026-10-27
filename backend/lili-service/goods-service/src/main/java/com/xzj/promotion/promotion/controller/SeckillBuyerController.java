package com.xzj.promotion.promotion.controller;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import com.xzj.promotion.promotion.entity.vos.SeckillGoodsVO;
import com.xzj.promotion.promotion.entity.vos.SeckillTimelineVO;
import com.xzj.promotion.promotion.service.SeckillApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 买家端秒杀活动接口。
 */
@Tag(name = "买家端,秒杀活动接口")
@RestController
@RequestMapping("/buyer/promotion/seckill")
public class SeckillBuyerController {

    @Autowired
    private SeckillApplyService seckillApplyService;

    /**
     * 获取当天秒杀活动及时间轴商品。
     */
    @Operation(summary = "获取当天秒杀活动信息")
    @GetMapping
    public ResultMessage<List<SeckillTimelineVO>> getSeckillTime() {
        return ResultUtil.data(seckillApplyService.getSeckillTimeline());
    }

    /**
     * 获取指定时间段的秒杀商品。
     */
    @Operation(summary = "获取某个时刻的秒杀活动商品信息")
    @GetMapping("/{timeline}")
    public ResultMessage<List<SeckillGoodsVO>> getSeckillGoods(@PathVariable Integer timeline) {
        return ResultUtil.data(seckillApplyService.getSeckillGoods(timeline));
    }
}
