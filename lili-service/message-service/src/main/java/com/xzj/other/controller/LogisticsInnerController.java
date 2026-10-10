package com.xzj.other.controller;

import com.xzj.message.LogisticsFeignClient;
import com.xzj.other.system.entity.dos.Logistics;
import com.xzj.other.system.entity.vo.Traces;
import com.xzj.other.system.service.LogisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Internal logistics API. The order service consumes this instead of the logistics table. */
@RestController
@RequestMapping("/logistics/inner")
public class LogisticsInnerController {
    private final LogisticsService logisticsService;

    public LogisticsInnerController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @GetMapping("/{id}")
    public LogisticsFeignClient.LogisticsDTO getById(@PathVariable String id) {
        return toDTO(logisticsService.getById(id));
    }

    @GetMapping("/code/{code}")
    public LogisticsFeignClient.LogisticsDTO getByCode(@PathVariable String code) {
        return toDTO(logisticsService.getOneByCode(code));
    }

    @GetMapping("/open")
    public List<LogisticsFeignClient.LogisticsDTO> getOpenLogistics() {
        return logisticsService.getOpenLogistics().stream().map(this::toDTO).toList();
    }

    @GetMapping("/track")
    public LogisticsFeignClient.TracesDTO getTrack(@RequestParam String logisticsId,
                                                   @RequestParam String logisticsNo,
                                                   @RequestParam String phone) {
        return toDTO(logisticsService.getLogisticTrack(logisticsId, logisticsNo, phone));
    }

    @GetMapping("/map-track")
    public LogisticsFeignClient.TracesDTO getMapTrack(@RequestParam String logisticsId,
                                                      @RequestParam String logisticsNo,
                                                      @RequestParam String phone,
                                                      @RequestParam String from,
                                                      @RequestParam String to) {
        return toDTO(logisticsService.getLogisticMapTrack(logisticsId, logisticsNo, phone, from, to));
    }

    @PostMapping("/label-order")
    public Map labelOrder(@RequestParam String orderSn, @RequestParam String logisticsId) {
        return logisticsService.labelOrder(orderSn, logisticsId);
    }

    @PostMapping("/shunfeng/{orderSn}")
    public String createShunfengOrder(@PathVariable String orderSn) {
        return logisticsService.createShunfengOrder(orderSn);
    }

    private LogisticsFeignClient.LogisticsDTO toDTO(Logistics source) {
        if (source == null) return null;
        LogisticsFeignClient.LogisticsDTO target = new LogisticsFeignClient.LogisticsDTO();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setCode(source.getCode());
        target.setStandBy(source.getStandBy());
        target.setFormItems(source.getFormItems());
        target.setDisabled(source.getDisabled());
        return target;
    }

    private LogisticsFeignClient.TracesDTO toDTO(Traces source) {
        if (source == null) return null;
        LogisticsFeignClient.TracesDTO target = new LogisticsFeignClient.TracesDTO();
        target.setShipper(source.getShipper());
        target.setLogisticCode(source.getLogisticCode());
        target.setTraces(source.getTraces());
        return target;
    }
}
