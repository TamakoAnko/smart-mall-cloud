package com.xzj.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/** Read-only live data used by promotion-service. */
@FeignClient(name = "message-service", contextId = "liveFeignClient", path = "/live")
public interface LiveFeignClient {
    @GetMapping("/inner/room")
    LiveRoomDTO getRoom(@RequestParam("id") String id);

    @GetMapping("/inner/coupon")
    LiveCouponDTO getCoupon(@RequestParam("roomId") String roomId, @RequestParam("couponId") String couponId);

    class LiveRoomDTO implements Serializable {
        private String id;
        private String title;
        private String liveStatus;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getTitle() { return title; }
        public void setTitle(String value) { title = value; }
        public String getLiveStatus() { return liveStatus; }
        public void setLiveStatus(String value) { liveStatus = value; }
    }

    class LiveCouponDTO implements Serializable {
        private String couponId;
        private String liveRoomName;
        public String getCouponId() { return couponId; }
        public void setCouponId(String value) { couponId = value; }
        public String getLiveRoomName() { return liveRoomName; }
        public void setLiveRoomName(String value) { liveRoomName = value; }
    }
}
