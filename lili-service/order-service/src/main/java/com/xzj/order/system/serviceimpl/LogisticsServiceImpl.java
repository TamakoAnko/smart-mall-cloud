package com.xzj.order.system.serviceimpl;

import com.xzj.message.LogisticsFeignClient;
import com.xzj.order.system.entity.dos.Logistics;
import com.xzj.order.system.entity.vo.Traces;
import com.xzj.order.system.service.LogisticsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Adapter for logistics owned by message-service. */
@Service
public class LogisticsServiceImpl implements LogisticsService {
    private final LogisticsFeignClient logisticsFeignClient;
    public LogisticsServiceImpl(LogisticsFeignClient logisticsFeignClient) {
        this.logisticsFeignClient = logisticsFeignClient;
    }

    @Override
    public Logistics getById(String id) {
        return toLocal(logisticsFeignClient.getById(id));
    }

    @Override
    public Logistics getByCode(String code) {
        return toLocal(logisticsFeignClient.getByCode(code));
    }

    @Override
    public List<Logistics> list() {
        return logisticsFeignClient.getOpenLogistics().stream().map(this::toLocal).toList();
    }

    @Override
    public Traces getLogisticTrack(String logisticsId, String logisticsNo, String phone) {
        return toLocal(logisticsFeignClient.getTrack(logisticsId, logisticsNo, phone));
    }

    @Override
    public Traces getLogisticMapTrack(String logisticsId, String logisticsNo, String phone, String from, String to) {
        return toLocal(logisticsFeignClient.getMapTrack(logisticsId, logisticsNo, phone, from, to));
    }

    @Override
    public Map<String, Object> labelOrder(String orderSn, String logisticsId) {
        return logisticsFeignClient.labelOrder(orderSn, logisticsId);
    }

    @Override
    public String sfCreateOrder(String orderSn) {
        return logisticsFeignClient.createShunfengOrder(orderSn);
    }

    @Override
    public List<Logistics> getOpenLogistics() {
        return list();
    }

    private Logistics toLocal(LogisticsFeignClient.LogisticsDTO source) {
        if (source == null) return null;
        Logistics target = new Logistics();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setCode(source.getCode());
        target.setStandBy(source.getStandBy());
        target.setFormItems(source.getFormItems());
        target.setDisabled(source.getDisabled());
        return target;
    }

    private Traces toLocal(LogisticsFeignClient.TracesDTO source) {
        if (source == null) return null;
        Traces target = new Traces();
        target.setShipper(source.getShipper());
        target.setLogisticCode(source.getLogisticCode());
        target.setTraces(source.getTraces());
        return target;
    }
}
