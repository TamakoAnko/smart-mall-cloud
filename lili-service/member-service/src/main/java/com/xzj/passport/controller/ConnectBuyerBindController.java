package com.xzj.passport.controller;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import com.xzj.passport.connect.service.ConnectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 买家端第三方账号绑定接口。
 * 绑定接口与登录接口共享 passport Controller 包，但通过 URL 和类职责区分功能。
 */
@RestController
@Tag(name = "买家端,第三方账户绑定")
@RequestMapping("/buyer/passport/connect/bind")
public class ConnectBuyerBindController {

    private final ConnectService connectService;

    public ConnectBuyerBindController(ConnectService connectService) {
        this.connectService = connectService;
    }

    /** 功能：绑定第三方 unionID。 */
    @Operation(summary = "unionID 绑定")
    @Parameters({
            @Parameter(name = "unionID", description = "unionID", required = true),
            @Parameter(name = "type", description = "平台类型", required = true)
    })
    @PostMapping
    public void unionIdBind(@RequestParam String unionID, @RequestParam String type) {
        connectService.bind(unionID, type);
    }

    /** 功能：解绑第三方账号。 */
    @Operation(summary = "unionID 解绑")
    @Parameter(name = "type", description = "平台类型", required = true)
    @PostMapping("/unbind")
    public void unionIdUnbind(@RequestParam String type) {
        connectService.unbind(type);
    }

    /** 功能：查询当前会员已绑定的平台。 */
    @GetMapping("/list")
    @Operation(summary = "绑定列表")
    public ResultMessage<List<String>> bindList() {
        return ResultUtil.data(connectService.bindList());
    }
}

