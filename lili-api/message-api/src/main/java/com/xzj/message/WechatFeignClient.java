package com.xzj.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

/** 微信小程序能力由 message-service 统一提供。 */
@FeignClient(name = "message-service", contextId = "wechatFeignClient", path = "/wechat")
public interface WechatFeignClient {

    @GetMapping("/inner/access-token")
    String getAccessToken(@RequestParam("clientType") String clientType);

    @PostMapping("/inner/access-token/evict")
    void evictAccessToken(@RequestParam("clientType") String clientType);

    @GetMapping("/inner/subscribe-message")
    List<WechatMPMessageDTO> listSubscribeMessages();

    @GetMapping("/inner/code")
    String createCode(@RequestParam("page") String page, @RequestParam("scene") String scene);

    @GetMapping("/inner/qrcode")
    String createQrCode(@RequestParam("page") String page);

    @GetMapping("/inner/scene/{id}")
    String getScene(@PathVariable("id") String id);

    class WechatMPMessageDTO implements Serializable {
        private String id;
        private String templateId;
        private String name;
        private String code;
        private String keywords;
        private String keywordsText;
        private Boolean enable;
        private String orderStatus;
        private String sceneCode;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getKeywords() { return keywords; }
        public void setKeywords(String keywords) { this.keywords = keywords; }
        public String getKeywordsText() { return keywordsText; }
        public void setKeywordsText(String keywordsText) { this.keywordsText = keywordsText; }
        public Boolean getEnable() { return enable; }
        public void setEnable(Boolean enable) { this.enable = enable; }
        public String getOrderStatus() { return orderStatus; }
        public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
        public String getSceneCode() { return sceneCode; }
        public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
    }
}
