package com.xzj.member.sms.serviceimpl;

import cn.hutool.json.JSONUtil;
import cn.lili.common.properties.RocketmqCustomProperties;
import cn.lili.common.utils.BeanUtil;
import cn.lili.message.LiliMessageTemplate;
import cn.lili.message.MessageSendCallbackBuilder;
import com.xzj.member.sms.entity.dos.SmsReach;
import com.xzj.member.sms.entity.dto.SmsReachDTO;
import com.xzj.member.sms.mapper.SmsReachMapper;
import com.xzj.member.sms.service.SmsReachService;
import cn.lili.rocketmq.tags.OtherTagsEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 短信任务业务层实现
 *
 * @author Bulbasaur
 * @since 2021/1/30 3:19 下午
 */
@Service
public class SmsReachServiceImpl extends ServiceImpl<SmsReachMapper, SmsReach> implements SmsReachService {
    @Autowired
    private LiliMessageTemplate liliMessageTemplate;
    @Autowired
    private RocketmqCustomProperties rocketmqCustomProperties;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSmsReach(SmsReach smsReach,List<String> mobile) {
        String destination = rocketmqCustomProperties.getNoticeSendTopic() + ":" + OtherTagsEnum.SMS.name();
        SmsReachDTO smsReachDTO = new SmsReachDTO();
        BeanUtil.copyProperties(smsReach,smsReachDTO);
        smsReachDTO.setMobile(mobile);
        this.save(smsReach);
        //发送短信批量发送mq消息
        liliMessageTemplate.asyncSend(destination, JSONUtil.toJsonStr(smsReachDTO), MessageSendCallbackBuilder.commonCallback());

    }
}
