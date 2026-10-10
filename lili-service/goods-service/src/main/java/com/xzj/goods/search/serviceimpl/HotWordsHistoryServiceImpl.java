package com.xzj.goods.search.serviceimpl;

import com.xzj.goods.search.entity.dos.HotWordsHistory;
import com.xzj.goods.search.entity.dto.HotWordsSearchParams;
import com.xzj.goods.search.mapper.HotWordsHistoryMapper;
import com.xzj.goods.search.service.HotWordsHistoryService;
import com.xzj.goods.search.service.HotWordsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 历史热词
 *
 * @author paulG
 * @since 2020/10/15
 **/
@Service
public class HotWordsHistoryServiceImpl extends ServiceImpl<HotWordsHistoryMapper, HotWordsHistory> implements HotWordsHistoryService {

    @Autowired
    private HotWordsService hotWordsService;

    /**
     * 热词数量上限，避免前端传入超大值造成全表扫描
     */
    private static final int MAX_TOP = 500;

    private static final int DEFAULT_TOP = 50;

    @Override
    public List<HotWordsHistory> statistics(HotWordsSearchParams hotWordsSearchParams) {
        int top = limitTop(hotWordsSearchParams.getTop());
        if ("TODAY".equalsIgnoreCase(hotWordsSearchParams.getSearchType())) {
            return hotWordsService.getHotWordsVO(top);
        }
        QueryWrapper queryWrapper = hotWordsSearchParams.queryWrapper();

        queryWrapper.groupBy("keywords");
        queryWrapper.orderByDesc("score");
        queryWrapper.last("limit " + top);
        List<HotWordsHistory> list = baseMapper.statistics(queryWrapper);
        return list;
    }

    private int limitTop(Integer top) {
        if (top == null || top <= 0) {
            return DEFAULT_TOP;
        }
        return Math.min(top, MAX_TOP);
    }

    @Override
    public List<HotWordsHistory> queryByDay(Date queryTime) {
        QueryWrapper queryWrapper = new QueryWrapper();

        java.util.Calendar start = java.util.Calendar.getInstance();
        start.setTime(queryTime);
        start.set(java.util.Calendar.HOUR_OF_DAY, 0);
        start.set(java.util.Calendar.MINUTE, 0);
        start.set(java.util.Calendar.SECOND, 0);
        start.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Calendar end = (java.util.Calendar) start.clone();
        end.add(java.util.Calendar.DAY_OF_MONTH, 1);
        queryWrapper.between("create_time", start.getTime(), end.getTime());
        return list(queryWrapper);
    }

}
