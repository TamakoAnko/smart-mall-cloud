package com.xzj.live.controller;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import com.xzj.live.entity.dos.LiveUser;
import com.xzj.live.service.LiveOperateService;
import com.xzj.live.service.LiveUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chc
 * @since 2022/6/2114:46
 */
@Slf4j
@Validated
@RestController
@Tag(name = "直播用户接口")
@RequestMapping("/buyer/live/user")
@RequiredArgsConstructor
public class LiveUserBuyerController {

    private final LiveOperateService liveOperateService;

    private final LiveUserService liveUserService;

    @Operation(summary = "观看的同时注册用户")
    @PostMapping(value = "/view")
    public ResultMessage<LiveUser> viewUser(@RequestParam String liveRoomId) {
        return ResultUtil.data(liveOperateService.checkLiveUser(liveRoomId));
    }

}
