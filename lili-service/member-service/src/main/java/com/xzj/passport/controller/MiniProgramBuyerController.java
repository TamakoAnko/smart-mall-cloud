package com.xzj.passport.controller;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.token.Token;
import cn.lili.common.vo.ResultMessage;
import com.xzj.message.WechatFeignClient;
import com.xzj.passport.connect.entity.dto.WechatMPLoginParams;
import com.xzj.passport.connect.service.ConnectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 买家端微信小程序登录及二维码接口。
 * 小程序流程独立于网页 OAuth，但同属于 Passport 认证入口，因此统一放置。
 */
@RestController
@RequestMapping("/buyer/passport/connect/miniProgram")
@Tag(name = "买家端,小程序登录接口")
public class MiniProgramBuyerController {

    private final ConnectService connectService;
    private final WechatFeignClient wechatFeignClient;

    public MiniProgramBuyerController(ConnectService connectService,
                                      WechatFeignClient wechatFeignClient) {
        this.connectService = connectService;
        this.wechatFeignClient = wechatFeignClient;
    }

    /** 功能：微信小程序自动登录，不存在会员时自动注册。 */
    @GetMapping("/auto-login")
    @Operation(summary = "小程序自动登录")
    public ResultMessage<Token> autoLogin(@RequestHeader String uuid, WechatMPLoginParams params) {
        params.setUuid(uuid);
        return ResultUtil.data(connectService.miniProgramAutoLogin(params));
    }

    /** 功能：查询小程序订阅消息模板。 */
    @GetMapping("/subscribeMessage")
    @Operation(summary = "消息订阅")
    public ResultMessage<List<WechatFeignClient.WechatMPMessageDTO>> subscribeMessage() {
        return ResultUtil.data(wechatFeignClient.listSubscribeMessages());
    }

    /** 功能：生成不限制数量的小程序码。 */
    @GetMapping("/mp/unlimited")
    @Operation(summary = "小程序二维码生成：不限制数量")
    public ResultMessage<String> unlimited(String page, String scene) {
        return ResultUtil.data(wechatFeignClient.createCode(page, scene));
    }

    /** 功能：生成普通小程序二维码。 */
    @GetMapping("/mp/qrcode")
    @Operation(summary = "小程序二维码生成")
    public ResultMessage<String> qrcode(String page) {
        return ResultUtil.data(wechatFeignClient.createQrCode(page));
    }

    /** 功能：根据短链接 ID 获取小程序页面参数。 */
    @GetMapping("/mp/unlimited/scene")
    @Operation(summary = "根据 shortlink 获取页面参数")
    public ResultMessage<String> getScene(String id) {
        return ResultUtil.data(wechatFeignClient.getScene(id));
    }
}


