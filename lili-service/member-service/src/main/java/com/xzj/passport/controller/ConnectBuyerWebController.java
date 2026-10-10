package com.xzj.passport.controller;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.token.Token;
import cn.lili.common.utils.UuidUtils;
import cn.lili.common.vo.ResultMessage;
import com.xzj.passport.connect.entity.dto.AuthCallback;
import com.xzj.passport.connect.entity.dto.ConnectAuthUser;
import com.xzj.passport.connect.request.AuthRequest;
import com.xzj.passport.connect.service.ConnectService;
import com.xzj.passport.connect.util.ConnectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 买家端网页第三方登录接口。
 * 授权、回调、结果查询属于同一条 OAuth 流程，因此集中在此 Controller 中。
 */
@Slf4j
@RestController
@Tag(name = "买家端,web联合登录")
@RequestMapping("/buyer/passport/connect/connect")
public class ConnectBuyerWebController {

    private final ConnectService connectService;
    private final ConnectUtil connectUtil;

    public ConnectBuyerWebController(ConnectService connectService, ConnectUtil connectUtil) {
        this.connectService = connectService;
        this.connectUtil = connectUtil;
    }

    /** 功能：生成 QQ 或微信网页授权地址并跳转。 */
    @GetMapping("/login/web/{type}")
    @Operation(summary = "WEB信任登录授权,包含PC、WAP")
    @Parameters(@Parameter(name = "type", description = "登录方式:QQ,微信,微信_PC"))
    public ResultMessage<String> webAuthorize(@PathVariable String type, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = connectUtil.getAuthRequest(type);
        String authorizeUrl = authRequest.authorize(UuidUtils.getUUID());
        response.sendRedirect(authorizeUrl);
        return ResultUtil.data(authorizeUrl);
    }

    /** 功能：接收第三方平台 OAuth 回调。 */
    @Operation(summary = "信任登录统一回调地址", hidden = true)
    @GetMapping("/callback/{type}")
    public void callback(@PathVariable String type,
                         AuthCallback callback,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        connectUtil.callback(type, callback, request, response);
    }

    /** 功能：供前端查询网页授权完成后的登录结果。 */
    @Operation(summary = "信任登录响应结果获取")
    @GetMapping("/result")
    public ResultMessage<Object> callbackResult(String state) {
        if (state == null) {
            throw new ServiceException(ResultCode.USER_CONNECT_LOGIN_ERROR);
        }
        return connectUtil.getResult(state);
    }

    /** 功能：APP 使用第三方 unionID 登录。 */
    @Operation(summary = "APP-unionID登录")
    @PostMapping("/app/login")
    public ResultMessage<Token> unionLogin(@RequestBody ConnectAuthUser authUser,
                                           @RequestHeader("uuid") String uuid) {
        try {
            return ResultUtil.data(connectService.unionLoginCallback(authUser, uuid));
        } catch (Exception e) {
            log.error("unionID登录错误", e);
            throw new ServiceException(ResultCode.USER_CONNECT_LOGIN_ERROR);
        }
    }
}


