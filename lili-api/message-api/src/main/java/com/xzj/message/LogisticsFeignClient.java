package com.xzj.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** Logistics operations owned by message-service. */
@FeignClient(name = "message-service", contextId = "logisticsFeignClient", path = "/logistics")
public interface LogisticsFeignClient {
    @GetMapping("/inner/{id}")
    LogisticsDTO getById(@PathVariable("id") String id);

    @GetMapping("/inner/code/{code}")
    LogisticsDTO getByCode(@PathVariable("code") String code);

    @GetMapping("/inner/open")
    List<LogisticsDTO> getOpenLogistics();

    @GetMapping("/inner/track")
    TracesDTO getTrack(@RequestParam("logisticsId") String logisticsId,
                       @RequestParam("logisticsNo") String logisticsNo,
                       @RequestParam("phone") String phone);

    @GetMapping("/inner/map-track")
    TracesDTO getMapTrack(@RequestParam("logisticsId") String logisticsId,
                          @RequestParam("logisticsNo") String logisticsNo,
                          @RequestParam("phone") String phone,
                          @RequestParam("from") String from,
                          @RequestParam("to") String to);

    @PostMapping("/inner/label-order")
    Map<String, Object> labelOrder(@RequestParam("orderSn") String orderSn,
                                   @RequestParam("logisticsId") String logisticsId);

    @PostMapping("/inner/shunfeng/{orderSn}")
    String createShunfengOrder(@PathVariable("orderSn") String orderSn);

    class LogisticsDTO implements Serializable {
        private String id;
        private String name;
        private String code;
        private String standBy;
        private String formItems;
        private String disabled;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getName() { return name; }
        public void setName(String value) { name = value; }
        public String getCode() { return code; }
        public void setCode(String value) { code = value; }
        public String getStandBy() { return standBy; }
        public void setStandBy(String value) { standBy = value; }
        public String getFormItems() { return formItems; }
        public void setFormItems(String value) { formItems = value; }
        public String getDisabled() { return disabled; }
        public void setDisabled(String value) { disabled = value; }
    }

    class TracesDTO implements Serializable {
        private String shipper;
        private String logisticCode;
        private List<Map> traces;
        public String getShipper() { return shipper; }
        public void setShipper(String value) { shipper = value; }
        public String getLogisticCode() { return logisticCode; }
        public void setLogisticCode(String value) { logisticCode = value; }
        public List<Map> getTraces() { return traces; }
        public void setTraces(List<Map> value) { traces = value; }
    }
}
