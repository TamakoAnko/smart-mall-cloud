package com.xzj.other.broadcast.util;

import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.HttpUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xzj.other.broadcast.entity.dos.Studio;
import com.xzj.other.wechat.util.WechatAccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** WeChat live-room client owned by message-service. */
@Slf4j
@Component("messageWechatLivePlayerUtil")
public class WechatLivePlayerUtil {
    private final WechatAccessTokenUtil accessTokenUtil;

    public WechatLivePlayerUtil(WechatAccessTokenUtil accessTokenUtil) {
        this.accessTokenUtil = accessTokenUtil;
    }

    public Map<String, String> create(Studio studio) {
        JSONObject result = post("https://api.weixin.qq.com/wxaapi/broadcast/room/create", roomPayload(studio));
        Map<String, String> response = new HashMap<>(2);
        response.put("roomId", result.getString("roomId"));
        response.put("qrcodeUrl", result.getString("qrcode_url"));
        return response;
    }

    public boolean editRoom(Studio studio) {
        Map<String, Object> payload = roomPayload(studio);
        payload.put("id", studio.getRoomId());
        post("https://api.weixin.qq.com/wxaapi/broadcast/room/editroom", payload);
        return true;
    }

    public String getLiveInfo(Integer roomId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "get_replay");
        payload.put("room_id", roomId);
        payload.put("start", "0");
        payload.put("limit", "1");
        return post("https://api.weixin.qq.com/wxa/business/getliveinfo", payload).getString("live_replay");
    }

    public Boolean pushGoods(Integer roomId, Integer goodsId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ids", new Integer[]{goodsId});
        payload.put("roomId", roomId);
        post("https://api.weixin.qq.com/wxaapi/broadcast/room/addgoods", payload);
        return true;
    }

    public Boolean goodsDeleteInRoom(Integer roomId, Integer goodsId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("goodsId", goodsId);
        payload.put("roomId", roomId);
        post("https://api.weixin.qq.com/wxaapi/broadcast/goods/deleteInRoom", payload);
        return true;
    }

    private Map<String, Object> roomPayload(Studio studio) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("coverImg", studio.getCoverImg());
        payload.put("shareImg", studio.getShareImg());
        payload.put("feedsImg", studio.getFeedsImg());
        payload.put("name", studio.getName());
        payload.put("startTime", studio.getStartTime());
        payload.put("endTime", studio.getEndTime());
        payload.put("anchorName", studio.getAnchorName());
        payload.put("anchorWechat", studio.getAnchorWechat());
        payload.put("type", 0);
        payload.put("closeLike", 0);
        payload.put("closeGoods", 0);
        payload.put("closeComment", 0);
        payload.put("closeReplay", 0);
        return payload;
    }

    private JSONObject post(String endpoint, Map<String, ?> payload) {
        String token = accessTokenUtil.cgiAccessToken(ClientTypeEnum.WECHAT_MP);
        if (token == null || token.isBlank()) {
            throw new ServiceException("微信小程序直播未配置");
        }
        String response = HttpUtils.doPostWithJson(endpoint + "?access_token=" + token, payload);
        JSONObject result = JSON.parseObject(response);
        if (result.getIntValue("errcode") != 0) {
            if (result.getIntValue("errcode") == 40001) {
                accessTokenUtil.removeAccessToken(ClientTypeEnum.WECHAT_MP);
            }
            throw new ServiceException(result.getString("errmsg"));
        }
        return result;
    }
}
