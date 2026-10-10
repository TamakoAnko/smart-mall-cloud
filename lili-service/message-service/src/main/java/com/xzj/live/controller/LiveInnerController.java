package com.xzj.live.controller;

import com.xzj.live.entity.dos.LiveCoupon;
import com.xzj.live.entity.dos.LiveRoom;
import com.xzj.live.service.LiveCouponService;
import com.xzj.live.service.LiveRoomService;
import com.xzj.message.LiveFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/live/inner")
public class LiveInnerController {
    private final LiveRoomService roomService;
    private final LiveCouponService couponService;

    public LiveInnerController(LiveRoomService roomService, LiveCouponService couponService) {
        this.roomService = roomService;
        this.couponService = couponService;
    }

    @GetMapping("/room")
    public LiveFeignClient.LiveRoomDTO getRoom(@RequestParam String id) {
        LiveRoom source = roomService.getById(id);
        if (source == null) return null;
        LiveFeignClient.LiveRoomDTO result = new LiveFeignClient.LiveRoomDTO();
        result.setId(source.getId());
        result.setTitle(source.getTitle());
        result.setLiveStatus(source.getLiveStatus());
        return result;
    }

    @GetMapping("/coupon")
    public LiveFeignClient.LiveCouponDTO getCoupon(@RequestParam String roomId, @RequestParam String couponId) {
        LiveCoupon source = couponService.getByLiveRoomIdAndCouponId(roomId, couponId);
        if (source == null) return null;
        LiveFeignClient.LiveCouponDTO result = new LiveFeignClient.LiveCouponDTO();
        result.setCouponId(source.getCouponId());
        result.setLiveRoomName(source.getLiveRoomName());
        return result;
    }
}
