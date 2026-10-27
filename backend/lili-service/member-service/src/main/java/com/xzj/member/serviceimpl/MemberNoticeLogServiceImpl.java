package com.xzj.member.serviceimpl;

import com.xzj.member.entity.dos.MemberNoticeLog;
import com.xzj.member.mapper.MemberNoticeLogMapper;
import com.xzj.member.service.MemberNoticeLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 客户消息业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 3:44 下午
 */
@Service
public class MemberNoticeLogServiceImpl extends ServiceImpl<MemberNoticeLogMapper, MemberNoticeLog> implements MemberNoticeLogService {
}