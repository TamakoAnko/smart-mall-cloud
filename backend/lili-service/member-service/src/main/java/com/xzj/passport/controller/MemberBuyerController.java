package com.xzj.passport.controller;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.Token;
import cn.lili.common.vo.ResultMessage;
import com.xzj.member.sms.SmsUtil;
import com.xzj.member.verification.entity.enums.VerificationEnums;
import com.xzj.member.verification.service.VerificationService;
import com.xzj.member.entity.dos.Member;
import com.xzj.member.entity.dto.MemberEditDTO;
import com.xzj.member.entity.enums.QRCodeLoginSessionStatusEnum;
import com.xzj.member.entity.vo.QRLoginResultVo;
import com.xzj.member.service.MemberService;
import com.xzj.member.service.MemberShareLogService;
import com.xzj.member.service.MemberShareRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 买家端会员认证接口。
 *
 * Controller 只负责接收请求、校验参数和组织返回值；会员数据和登录业务
 * 仍由 member 包中的领域服务处理，避免在 Passport 层复制会员数据访问代码。
 */
@Slf4j
@RestController
@Tag(name = "买家端,客户登录接口")
@RequestMapping("/buyer/passport/member")
public class MemberBuyerController {

    private static final int QR_LOGIN_WAIT_SECONDS = 20;

    private final MemberService memberService;
    private final SmsUtil smsUtil;
    private final VerificationService verificationService;
    private final MemberShareLogService memberShareLogService;
    private final MemberShareRegisterService memberShareRegisterService;

    public MemberBuyerController(MemberService memberService,
                                 SmsUtil smsUtil,
                                 VerificationService verificationService,
                                 MemberShareLogService memberShareLogService,
                                 MemberShareRegisterService memberShareRegisterService) {
        this.memberService = memberService;
        this.smsUtil = smsUtil;
        this.verificationService = verificationService;
        this.memberShareLogService = memberShareLogService;
        this.memberShareRegisterService = memberShareRegisterService;
    }

    /** 功能：创建 PC 扫码登录会话。 */
    @Operation(summary = "web-获取登录二维码")
    @PostMapping("/pc_session")
    public ResultMessage<Object> createPcSession() {
        return ResultUtil.data(memberService.createPcSession());
    }

    /** 功能：长轮询扫码登录状态，最多等待 20 秒。 */
    @Operation(summary = "web-二维码登录")
    @PostMapping("/session_login/{token}")
    public Object loginWithSession(@PathVariable String token,
                                   @RequestParam(required = false) Integer beforeSessionStatus) {
        log.info("receive login with session key {}", token);
        int previousStatus = beforeSessionStatus == null
                ? QRCodeLoginSessionStatusEnum.WAIT_SCANNING.getCode()
                : beforeSessionStatus;
        ResponseEntity<ResultMessage<Object>> timeoutResponse =
                new ResponseEntity<>(ResultUtil.error(ResultCode.ERROR), HttpStatus.OK);
        DeferredResult<ResponseEntity<Object>> deferredResult =
                new DeferredResult<>(QR_LOGIN_WAIT_SECONDS * 1000L, timeoutResponse);

        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < QR_LOGIN_WAIT_SECONDS; i++) {
                    QRLoginResultVo queryResult = memberService.loginWithSession(token);
                    int status = queryResult.getStatus();
                    boolean stillWaiting = status == previousStatus
                            && (QRCodeLoginSessionStatusEnum.WAIT_SCANNING.getCode() == status
                            || QRCodeLoginSessionStatusEnum.SCANNING.getCode() == status);
                    if (!stillWaiting) {
                        deferredResult.setResult(new ResponseEntity<>(ResultUtil.data(queryResult), HttpStatus.OK));
                        return;
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("二维码登录轮询被中断，token={}", token);
            } catch (Exception e) {
                log.error("获取登录状态异常，token={}", token, e);
                deferredResult.setResult(new ResponseEntity<>(ResultUtil.error(ResultCode.ERROR), HttpStatus.OK));
            }
        });
        return deferredResult;
    }

    /** 功能：APP 扫描 PC 端登录二维码。 */
    @Operation(summary = "app扫码")
    @PostMapping("/app_scanner")
    public ResultMessage<Object> appScanner(@RequestParam String token) {
        return ResultUtil.data(memberService.appScanner(token));
    }

    /** 功能：APP 确认或拒绝 PC 端登录。 */
    @Operation(summary = "app扫码-登录确认：同意/拒绝")
    @Parameters({
            @Parameter(name = "token", description = "sessionToken", required = true),
            @Parameter(name = "code", description = "操作：0拒绝登录，1同意登录", required = true)
    })
    @PostMapping("/app_confirm")
    public ResultMessage<Object> appConfirm(@RequestParam String token, @RequestParam Integer code) {
        return memberService.appSConfirm(token, code) ? ResultUtil.success() : ResultUtil.error(ResultCode.ERROR);
    }

    /** 功能：用户名密码登录。 */
    @Operation(summary = "登录接口")
    @PostMapping("/userLogin")
    public ResultMessage<Token> userLogin(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                          @NotNull(message = "密码不能为空") @RequestParam String password,
                                          @RequestHeader String uuid) {
        verificationService.check(uuid, VerificationEnums.LOGIN);
        return ResultUtil.data(memberService.usernameLogin(username, password));
    }

    /** 功能：退出当前会员登录。 */
    @Operation(summary = "注销接口")
    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        memberService.logout(UserEnums.MEMBER);
        return ResultUtil.success();
    }

    /** 功能：短信验证码登录。 */
    @Operation(summary = "短信登录接口")
    @PostMapping("/smsLogin")
    public ResultMessage<Token> smsLogin(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                         @NotNull(message = "验证码为空") @RequestParam String code,
                                         @RequestHeader String uuid) {
        if (!smsUtil.verifyCode(mobile, VerificationEnums.LOGIN, uuid, code)) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
        return ResultUtil.data(memberService.mobilePhoneLogin(mobile));
    }

    /** 功能：绑定新的手机号。 */
    @Operation(summary = "绑定手机号")
    @PostMapping("/bindMobile")
    public ResultMessage<Boolean> bindMobile(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                             @NotNull(message = "手机号为空") @RequestParam String mobile,
                                             @NotNull(message = "验证码为空") @RequestParam String code,
                                             @RequestHeader String uuid) {
        if (!smsUtil.verifyCode(mobile, VerificationEnums.BIND_MOBILE, uuid, code)) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
        Member member = memberService.findByUsername(username);
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        if (memberService.findByMobile(mobile) != null) {
            throw new ServiceException(ResultCode.USER_MOBILE_REPEATABLE_ERROR);
        }
        return ResultUtil.data(memberService.changeMobile(member.getId(), mobile));
    }

    /** 功能：注册会员。 */
    @Operation(summary = "注册用户")
    @PostMapping("/register")
    public ResultMessage<Token> register(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                         @NotNull(message = "密码不能为空") @RequestParam String password,
                                         @NotNull(message = "手机号为空") @RequestParam String mobilePhone,
                                         @RequestHeader String uuid,
                                         @NotNull(message = "验证码不能为空") @RequestParam String code,
                                         @RequestParam(required = false) String shareCode) {
        if (!smsUtil.verifyCode(mobilePhone, VerificationEnums.REGISTER, uuid, code)) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
        return ResultUtil.data(memberService.register(username, password, mobilePhone, shareCode));
    }

    /** 功能：获取当前登录会员。 */
    @Operation(summary = "获取当前登录用户接口")
    @GetMapping
    public ResultMessage<Member> getUserInfo() {
        return ResultUtil.data(memberService.getUserInfo());
    }

    /** 功能：发送短信并准备通过手机号重置密码。 */
    @Operation(summary = "通过短信重置密码")
    @PostMapping("/resetByMobile")
    public ResultMessage<Object> resetByMobile(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                               @NotNull(message = "验证码为空") @RequestParam String code,
                                               @RequestHeader String uuid) {
        if (!smsUtil.verifyCode(mobile, VerificationEnums.FIND_USER, uuid, code)) {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
        memberService.findByMobile(uuid, mobile);
        return ResultUtil.success();
    }

    /** 功能：根据短信验证结果修改密码。 */
    @Operation(summary = "修改密码")
    @PostMapping("/resetPassword")
    public ResultMessage<Boolean> resetPassword(@NotNull(message = "密码为空") @RequestParam String password,
                                                @RequestHeader String uuid) {
        return ResultUtil.data(memberService.resetByMobile(uuid, password));
    }

    /** 功能：修改当前会员资料。 */
    @Operation(summary = "修改用户自己资料")
    @PutMapping("/editOwn")
    public ResultMessage<Member> editOwn(MemberEditDTO memberEditDTO) {
        return ResultUtil.data(memberService.editOwn(memberEditDTO));
    }

    /** 功能：修改当前会员密码。 */
    @Operation(summary = "修改密码")
    @PutMapping("/modifyPass")
    public ResultMessage<Member> modifyPass(@NotNull(message = "旧密码不能为空") @RequestParam String password,
                                            @NotNull(message = "新密码不能为空") @RequestParam String newPassword) {
        return ResultUtil.data(memberService.modifyPass(password, newPassword));
    }

    /** 功能：判断当前会员是否需要初始化密码。 */
    @Operation(summary = "初始设置密码")
    @PutMapping("/canInitPassword")
    public ResultMessage<Boolean> canInitPassword() {
        return ResultUtil.data(memberService.canInitPass());
    }

    /** 功能：初始化会员密码。 */
    @Operation(summary = "初始设置密码")
    @PutMapping("/initPassword")
    public ResultMessage<Object> initPassword(@NotNull(message = "密码不能为空") @RequestParam String password) {
        memberService.initPass(password);
        return ResultUtil.success();
    }

    /** 功能：注销会员账号。 */
    @Operation(summary = "注销账号")
    @PutMapping("/cancellation")
    public ResultMessage<Object> cancellation() {
        memberService.cancellation();
        return ResultUtil.success();
    }

    /** 功能：刷新会员登录 token。 */
    @Operation(summary = "刷新token")
    @GetMapping("/refresh/{refreshToken}")
    public ResultMessage<Token> refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        return ResultUtil.data(memberService.refreshToken(refreshToken));
    }

    /** 功能：获取即时通讯使用的当前会员信息。 */
    @GetMapping("/getImUser")
    @Operation(summary = "获取用户信息")
    public ResultMessage<Member> getImUser() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(memberService.getById(authUser.getId()));
    }

    /** 功能：根据会员 ID 获取即时通讯用户信息。 */
    @GetMapping("/getImUserDetail/{memberId}")
    @Parameter(name = "memberId", description = "会员Id", required = true)
    @Operation(summary = "获取用户信息")
    public ResultMessage<Member> getImUserDetail(@PathVariable String memberId) {
        return ResultUtil.data(memberService.getById(memberId));
    }

    /** 功能：分享商城页面并赠送经验值。 */
    @PostMapping("/share/mall")
    @Operation(summary = "分享商城页面赠送经验值")
    public ResultMessage<Object> shareMall() {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        memberShareLogService.saveShareAndGrantExperience(currentUser.getId(), "MALL", "MALL_HOME", null);
        return ResultUtil.success();
    }

    /** 功能：获取会员分享注册链接。 */
    @GetMapping("/share/register/code")
    @Operation(summary = "获取分享注册码")
    public ResultMessage<String> getRegisterShareCode() {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        return ResultUtil.data(memberShareRegisterService.getOrCreateRegisterShareCode(currentUser.getId()));
    }
}

