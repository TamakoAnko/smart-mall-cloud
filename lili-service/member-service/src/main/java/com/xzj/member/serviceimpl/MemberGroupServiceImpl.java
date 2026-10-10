package com.xzj.member.serviceimpl;

import com.xzj.member.entity.dos.MemberGroup;
import com.xzj.member.mapper.MemberGroupMapper;
import com.xzj.member.service.MemberGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MemberGroupServiceImpl extends ServiceImpl<MemberGroupMapper, MemberGroup> implements MemberGroupService {
}
