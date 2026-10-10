package com.xzj.wallet.controller;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import com.xzj.member.MemberFeignClient;
import com.xzj.order.system.entity.dto.WithdrawalSetting;
import com.xzj.order.system.entity.enums.SettingEnum;
import com.xzj.order.system.entity.vo.WithdrawalSettingVO;
import com.xzj.message.SettingFeignClient;
import com.xzj.order.verification.entity.enums.VerificationEnums;
import com.xzj.order.verification.service.VerificationService;
import com.xzj.wallet.entity.dos.MemberWallet;
import com.xzj.wallet.entity.vo.MemberWalletVO;
import com.xzj.wallet.service.MemberWalletService;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端,客户余额接口
 *
 * @author pikachu
 * @since 2020/11/16 10:07 下午
 */
@RestController
@Tag(name = "买家端,客户余额接口")
@RequestMapping("/buyer/wallet/wallet")
@Validated
public class MemberWalletBuyerController {

    /**
     * 客户
     */
    @Autowired
    private MemberFeignClient memberFeignClient;
    /**
     * 客户余额
     */
    @Autowired
    private MemberWalletService memberWalletService;
    /**
     * 验证码
     */
    @Autowired
    private VerificationService verificationService;

    @Autowired
    private SettingFeignClient settingFeignClient;

    @GetMapping
    @Operation(description = "查询客户预存款余额")
    public ResultMessage<MemberWalletVO> get() {
        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser != null) {
            return ResultUtil.data(memberWalletService.getMemberWallet(authUser.getId()));
        }
        throw new ServiceException(ResultCode.USER_NOT_LOGIN);
    }

    @GetMapping("/withdrawalSettingVO")
    @Operation(description = "获取提现设置VO")
    public ResultMessage<Object> minPrice() {
        SettingFeignClient.SettingDTO setting = settingFeignClient.get(SettingEnum.WITHDRAWAL_SETTING.name());
        WithdrawalSetting withdrawalSetting = new Gson().fromJson(setting.getSettingValue(), WithdrawalSetting.class);

        WithdrawalSettingVO withdrawalSettingVO = new WithdrawalSettingVO();
        withdrawalSettingVO.setMinPrice(withdrawalSetting.getMinPrice());
        withdrawalSettingVO.setFee(withdrawalSetting.getFee());
        withdrawalSettingVO.setType(withdrawalSetting.getType());
        return ResultUtil.data(withdrawalSettingVO);
    }

    @PreventDuplicateSubmissions
    @PostMapping("/withdrawal")
    @Operation(description = "客户中心余额提现")
    @Parameter(name = "price", description = "提现金额", required = true)
    @Parameter(name = "realName", description = "真实姓名", required = true)
    @Parameter(name = "connectNumber", description = "第三方登录账号", required = true)
    public ResultMessage<Boolean> withdrawal(@Max(value = 9999, message = "充值金额单次最多允许提现9999元") Double price, @RequestParam String realName, @RequestParam String connectNumber) {
        return ResultUtil.data(memberWalletService.applyWithdrawal(price, realName, connectNumber));
    }

    @PostMapping("/set-password")
    @Operation(description = "设置支付密码")
    @Parameter(name = "password", description = "支付密码", required = true)
    public ResultMessage<Object> setPassword(String password, @RequestHeader String uuid) {
        AuthUser authUser = UserContext.getCurrentUser();
        //校验当前用户是否存在
        MemberFeignClient.MemberSummaryDTO member = memberFeignClient.getMemberById(authUser.getId());
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        //校验验证码
        if (verificationService.check(uuid, VerificationEnums.WALLET_PASSWORD)) {
            memberWalletService.setMemberWalletPassword(member.getId(), password);
            throw new ServiceException(ResultCode.SUCCESS);
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_ERROR);
        }

    }
    

    @PostMapping("/update-password/ordinary")
    @Operation(description = "普通方式进行支付密码的修改")
    @Parameter(name = "oldPassword", description = "旧支付密码", required = true)
    @Parameter(name = "newPassword", description = "新支付密码", required = true)
    public ResultMessage updatePassword(@RequestParam @Pattern(regexp = "[a-fA-F0-9]{32}", message = "旧密码格式不正确") String oldPassword,
                                        @RequestParam @Pattern(regexp = "[a-fA-F0-9]{32}", message = "新密码格式不正确") String newPassword) {
        AuthUser authUser = UserContext.getCurrentUser();
        //校验当前用户是否存在
        MemberFeignClient.MemberSummaryDTO member = memberFeignClient.getMemberById(authUser.getId());
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        MemberWallet memberWallet = this.memberWalletService.getMemberWalletDO(member.getId());
        //校验新旧密码是否一致
        if (memberWallet != null) {
            if (!new BCryptPasswordEncoder().matches(oldPassword, memberWallet.getWalletPassword())) {
                throw new ServiceException(ResultCode.USER_OLD_PASSWORD_ERROR);
            }
            this.memberWalletService.setMemberWalletPassword(member.getId(), newPassword);
            return ResultUtil.data("修改成功");
        } else {
            throw new ServiceException(ResultCode.WALLET_NOT_EXIT_ERROR);
        }
    }


    @GetMapping("/check")
    @Operation(description = "检测客户是否设置过支付密码,客户中心设置或者修改密码时使用")
    public ResultMessage<Boolean> checkPassword() {
        return ResultUtil.data(memberWalletService.checkPassword());
    }

}
