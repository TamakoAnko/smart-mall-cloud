package com.xzj.passport.connect.entity.dto;

import com.xzj.member.entity.dos.Member;
import lombok.Data;

/**
 * 客户联合登录消息
 */
@Data
public class MemberConnectLoginMessage {

    private Member member;
    private ConnectAuthUser connectAuthUser;
}

