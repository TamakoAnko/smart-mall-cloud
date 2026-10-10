package com.xzj.other.page.serviceimpl;


import com.xzj.other.page.entity.dos.Feedback;
import com.xzj.other.page.mapper.FeedbackMapper;
import com.xzj.other.page.service.FeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 意见反馈业务层实现
 *
 * @author Chopper
 * @since 2020/11/18 11:40 上午
 */
@Service("messageOtherFeedbackServiceImpl")
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

}