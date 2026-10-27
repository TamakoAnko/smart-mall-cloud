package com.xzj.promotion.promotion.serviceimpl;


import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.vo.PageVO;
import com.xzj.promotion.promotion.entity.dos.KanjiaActivity;
import com.xzj.promotion.promotion.entity.dos.KanjiaActivityGoods;
import com.xzj.promotion.promotion.entity.dos.KanjiaActivityLog;
import com.xzj.promotion.promotion.entity.dto.KanjiaActivityDTO;
import com.xzj.promotion.promotion.entity.dto.search.KanJiaActivityLogQuery;
import com.xzj.promotion.promotion.entity.enums.PromotionsStatusEnum;
import com.xzj.promotion.promotion.mapper.KanJiaActivityLogMapper;
import com.xzj.promotion.promotion.service.KanjiaActivityGoodsService;
import com.xzj.promotion.promotion.service.KanjiaActivityLogService;
import com.xzj.promotion.promotion.service.KanjiaActivityService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


/**
 * 砍价活动日志业务层实现
 *
 * @author qiuqiu
 * @date 2021/7/1
 */
@Service("goodsPromotionKanjiaActivityLogServiceImpl")
public class KanjiaActivityLogServiceImpl extends ServiceImpl<KanJiaActivityLogMapper, KanjiaActivityLog> implements KanjiaActivityLogService {

    @Autowired
    private KanjiaActivityGoodsService kanJiaActivityGoodsService;
    @Lazy
    @Autowired
    private KanjiaActivityService kanJiaActivityService;

    @Override
    public IPage<KanjiaActivityLog> getForPage(KanJiaActivityLogQuery kanJiaActivityLogQuery, PageVO pageVO) {
        QueryWrapper<KanjiaActivityLog> queryWrapper = kanJiaActivityLogQuery.wrapper();
        return this.page(PageUtil.initPage(pageVO), queryWrapper);
    }


    @Override
    public KanjiaActivityLog addKanJiaActivityLog(KanjiaActivityDTO kanjiaActivityDTO) {
        //校验当前客户是否已经参与过此次砍价
        LambdaQueryWrapper<KanjiaActivityLog> queryWrapper = new LambdaQueryWrapper<KanjiaActivityLog>();
        queryWrapper.eq(kanjiaActivityDTO.getKanjiaActivityId() != null, KanjiaActivityLog::getKanjiaActivityId, kanjiaActivityDTO.getKanjiaActivityId());
        queryWrapper.eq(KanjiaActivityLog::getKanjiaMemberId, UserContext.getCurrentUser().getId());
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ResultCode.KANJIA_ACTIVITY_LOG_MEMBER_ERROR);
        }
        //校验当前砍价商品是否有效
        KanjiaActivityGoods kanjiaActivityGoods = kanJiaActivityGoodsService.getById(kanjiaActivityDTO.getKanjiaActivityGoodsId());
        //如果当前活动不为空且还在活动时间内 才可以参与砍价活动
        if (kanjiaActivityGoods != null && kanjiaActivityGoods.getPromotionStatus().equals(PromotionsStatusEnum.START.name())) {
            //获取砍价参与者记录
            KanjiaActivity kanjiaActivity = kanJiaActivityService.getById(kanjiaActivityDTO.getKanjiaActivityId());
            if (kanjiaActivity != null) {
                KanjiaActivityLog kanJiaActivityLog = new KanjiaActivityLog();
                kanJiaActivityLog.setKanjiaActivityId(kanjiaActivity.getId());
                BeanUtil.copyProperties(kanjiaActivityDTO, kanJiaActivityLog);
                boolean result = this.save(kanJiaActivityLog);
                if (result) {
                    return kanJiaActivityLog;
                }
            }
            throw new ServiceException(ResultCode.KANJIA_ACTIVITY_NOT_FOUND_ERROR);
        }
        throw new ServiceException(ResultCode.PROMOTION_STATUS_END);

    }
}