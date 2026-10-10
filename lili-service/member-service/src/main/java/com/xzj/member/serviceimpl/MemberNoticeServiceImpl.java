package com.xzj.member.serviceimpl;

import com.xzj.member.entity.dos.MemberNotice;
import com.xzj.member.mapper.MemberNoticeMapper;
import com.xzj.member.service.MemberNoticeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户站内信业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:44 下午
 */
@Service
public class MemberNoticeServiceImpl extends ServiceImpl<MemberNoticeMapper, MemberNotice> implements MemberNoticeService {

    @Override
    public boolean read(List<String> ids) {
        UpdateWrapper<MemberNotice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids);
        updateWrapper.set("is_read", true);
        return this.update(updateWrapper);
    }

    @Override
    public boolean readAll(String memberId) {
        UpdateWrapper<MemberNotice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("member_id", memberId);
        updateWrapper.set("is_read", true);
        return this.update(updateWrapper);
    }

    @Override
    public boolean removeAll(String memberId) {
        QueryWrapper<MemberNotice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        return this.remove(queryWrapper);
    }
}
