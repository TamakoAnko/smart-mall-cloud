package com.xzj.other.controller;

import cn.lili.common.enums.ClientTypeEnum;
import com.xzj.message.WechatFeignClient;
import com.xzj.message.entity.dos.ShortLink;
import com.xzj.message.service.ShortLinkService;
import com.xzj.other.wechat.service.WechatMPMessageService;
import com.xzj.other.wechat.util.WechatMpCodeUtil;
import com.xzj.other.wechat.util.WechatAccessTokenUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** message-service 的微信小程序内部接口。 */
@RestController
@RequestMapping("/wechat/inner")
public class WechatInnerController {

    private final WechatMPMessageService wechatMPMessageService;
    private final WechatMpCodeUtil wechatMpCodeUtil;
    private final WechatAccessTokenUtil wechatAccessTokenUtil;
    private final ShortLinkService shortLinkService;

    public WechatInnerController(WechatMPMessageService wechatMPMessageService,
                                 WechatMpCodeUtil wechatMpCodeUtil,
                                 WechatAccessTokenUtil wechatAccessTokenUtil,
                                 ShortLinkService shortLinkService) {
        this.wechatMPMessageService = wechatMPMessageService;
        this.wechatMpCodeUtil = wechatMpCodeUtil;
        this.wechatAccessTokenUtil = wechatAccessTokenUtil;
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/access-token")
    public String getAccessToken(@RequestParam("clientType") String clientType) {
        return wechatAccessTokenUtil.cgiAccessToken(ClientTypeEnum.valueOf(clientType));
    }

    @PostMapping("/access-token/evict")
    public void evictAccessToken(@RequestParam("clientType") String clientType) {
        wechatAccessTokenUtil.removeAccessToken(ClientTypeEnum.valueOf(clientType));
    }

    @GetMapping("/subscribe-message")
    public List<WechatFeignClient.WechatMPMessageDTO> listSubscribeMessages() {
        return wechatMPMessageService.list().stream().map(this::toDTO).toList();
    }

    @GetMapping("/code")
    public String createCode(@RequestParam("page") String page, @RequestParam("scene") String scene) {
        return wechatMpCodeUtil.createCode(page, scene);
    }

    @GetMapping("/qrcode")
    public String createQrCode(@RequestParam("page") String page) {
        return wechatMpCodeUtil.createQrCode(page);
    }

    @GetMapping("/scene/{id}")
    public String getScene(@PathVariable("id") String id) {
        ShortLink shortLink = shortLinkService.getById(id);
        return shortLink == null ? null : shortLink.getOriginalParams();
    }

    private WechatFeignClient.WechatMPMessageDTO toDTO(com.xzj.other.wechat.entity.dos.WechatMPMessage source) {
        WechatFeignClient.WechatMPMessageDTO target = new WechatFeignClient.WechatMPMessageDTO();
        target.setId(source.getId());
        target.setTemplateId(source.getTemplateId());
        target.setName(source.getName());
        target.setCode(source.getCode());
        target.setKeywords(source.getKeywords());
        target.setKeywordsText(source.getKeywordsText());
        target.setEnable(source.getEnable());
        target.setOrderStatus(source.getOrderStatus());
        target.setSceneCode(source.getSceneCode());
        return target;
    }
}
