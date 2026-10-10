package com.xzj.member.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.SearchVO;
import com.xzj.member.entity.dos.Member;
import com.xzj.member.entity.dos.MemberNotice;
import com.xzj.member.entity.dos.MemberNoticeSenter;
import com.xzj.member.entity.enums.SendTypeEnum;
import com.xzj.member.mapper.MemberNoticeSenterMapper;
import com.xzj.member.service.MemberNoticeSenterService;
import com.xzj.member.service.MemberNoticeService;
import com.xzj.member.service.MemberService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户消息业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:44 下午
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class MemberNoticeSenterServiceImpl extends ServiceImpl<MemberNoticeSenterMapper, MemberNoticeSenter> implements MemberNoticeSenterService {

    /**
     * 客户
     */
    @Autowired
    private MemberService memberService;
    /**
     * 客户站内信
     */
    @Autowired
    private MemberNoticeService memberNoticeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean customSave(MemberNoticeSenter memberNoticeSenter) {

        if (this.saveOrUpdate(memberNoticeSenter)) {
            List<MemberNotice> memberNotices = new ArrayList<>();
            //如果是选中客户发送
            if (memberNoticeSenter.getSendType().equals(SendTypeEnum.SELECT.name())) {
                //判定消息是否有效
                if (!CharSequenceUtil.isEmpty(memberNoticeSenter.getMemberIds())) {
                    String[] ids = memberNoticeSenter.getMemberIds().split(",");
                    MemberNotice memberNotice;
                    for (String id : ids) {
                        memberNotice = new MemberNotice();
                        memberNotice.setIsRead(false);
                        memberNotice.setContent(memberNoticeSenter.getContent());
                        memberNotice.setMemberId(id);
                        memberNotice.setTitle(memberNoticeSenter.getTitle());
                        memberNotices.add(memberNotice);
                    }
                } else {
                    return true;
                }
            } //否则是全部客户发送
            else {
                List<Member> members = memberService.list();
                MemberNotice memberNotice;
                for (Member member : members) {
                    memberNotice = new MemberNotice();
                    memberNotice.setIsRead(false);
                    memberNotice.setContent(memberNoticeSenter.getContent());
                    memberNotice.setMemberId(member.getId());
                    memberNotice.setTitle(memberNoticeSenter.getTitle());
                    memberNotices.add(memberNotice);
                }
            }
            //防止没有客户导致报错
            if (!memberNotices.isEmpty()) {
                //批量保存
                if (memberNoticeService.saveBatch(memberNotices)) {
                    return true;
                } else {
                    throw new ServiceException(ResultCode.NOTICE_SEND_ERROR);
                }
            }
        }
        return true;
    }

    @Override
    public IPage<MemberNoticeSenter> getByPage(MemberNoticeSenter entity, SearchVO searchVO, PageVO pageVO) {
        return this.page(PageUtil.initPage(pageVO), PageUtil.initWrapper(entity, searchVO));
    }
}
