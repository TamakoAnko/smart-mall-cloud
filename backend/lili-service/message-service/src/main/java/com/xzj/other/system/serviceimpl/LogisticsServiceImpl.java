package com.xzj.other.system.serviceimpl;

import cn.hutool.json.JSONUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.SwitchEnum;
import cn.lili.common.exception.ServiceException;
import com.xzj.message.logistics.LogisticsPluginFactory;
import com.xzj.message.logistics.entity.dto.LabelOrderDTO;
import com.xzj.message.logistics.entity.enums.LogisticsEnum;
import com.xzj.order.OrderFeignClient;
import com.xzj.config.StoreFeignClient;
import com.xzj.other.system.entity.dos.Logistics;
import com.xzj.common.setting.entity.dos.Setting;
import com.xzj.other.system.entity.dto.LogisticsSetting;
import com.xzj.common.setting.entity.enums.SettingEnum;
import com.xzj.other.system.entity.vo.Traces;
import com.xzj.other.system.mapper.LogisticsMapper;
import com.xzj.other.system.service.LogisticsService;
import com.xzj.common.setting.service.SettingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 物流公司业务层实现
 *
 * @author Chopper
 * @since 2020/11/17 8:02 下午
 */
@Slf4j
@Service("messageOtherLogisticsServiceImpl")
public class LogisticsServiceImpl extends ServiceImpl<LogisticsMapper, Logistics> implements LogisticsService {

    @Override
    public Logistics getOneByCode(String code) {
        return this.getOne(new LambdaQueryWrapper<Logistics>().eq(Logistics::getCode, code));
    }

    @Override
    public String createShunfengOrder(String orderSn) {
        return null;
    }
    @Autowired
    private LogisticsPluginFactory logisticsPluginFactory;
    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private StoreFeignClient storeFeignClient;

    @Autowired
    private SettingService settingService;

    @Override
    public Traces getLogisticTrack(String logisticsId, String logisticsNo, String phone) {
        try {
            Logistics logistics = this.getById(logisticsId);
            return logisticsPluginFactory.filePlugin().pollQuery(logistics, logisticsNo, phone);
        } catch (Exception e) {
            log.error("获取物流公司错误", e);

        }
        return null;
    }

    @Override
    public Traces getLogisticMapTrack(String logisticsId, String logisticsNo, String phone, String from, String to) {
        try {
            Logistics logistics = this.getById(logisticsId);
            return logisticsPluginFactory.filePlugin().pollMapTrack(logistics, logisticsNo, phone, from, to);
        } catch (Exception e) {
            log.error("获取物流公司错误", e);

        }
        return null;
    }

    @Override
    public Map labelOrder(String orderSn, String logisticsId) {
        //获取设置
        LogisticsSetting logisticsSetting = this.getLogisticsSetting();
        //获取订单及子订单
        OrderFeignClient.OrderSummaryDTO remoteOrder = orderFeignClient.getOrderBySn(orderSn);
        if (remoteOrder == null) {
            throw new ServiceException(ResultCode.ORDER_NOT_EXIST);
        }
        if ((LogisticsEnum.SHUNFENG.name().equals(logisticsSetting.getType()) && "DELIVERED".equals(remoteOrder.getDeliverStatus()) && "DELIVERED".equals(remoteOrder.getOrderStatus()))
                || ("UNDELIVERED".equals(remoteOrder.getDeliverStatus()) && "UNDELIVERED".equals(remoteOrder.getOrderStatus()))) {
            //订单货物
            List<OrderFeignClient.OrderItemSummaryDTO> orderItems = orderFeignClient.getOrderItems(orderSn);
            //获取对应物流
            Logistics logistics;

            if (LogisticsEnum.SHUNFENG.name().equals(logisticsSetting.getType())) {
                logistics = this.getOne(new LambdaQueryWrapper<Logistics>().eq(Logistics::getCode, "SF"));
            } else {
                logistics = this.getById(logisticsId);
            }
            // 店铺-物流公司设置
            StoreFeignClient.StoreLogisticsDTO storeLogistics = storeFeignClient.getLogistics(remoteOrder.getStoreId(), logistics.getId());
            StoreFeignClient.StoreDeliverAddressDTO storeDeliverGoodsAddressDTO = storeFeignClient.getDeliverAddress(remoteOrder.getStoreId());

            LabelOrderDTO labelOrderDTO = new LabelOrderDTO();
            labelOrderDTO.setOrder(remoteOrder);
            labelOrderDTO.setOrderItems(orderItems);
            labelOrderDTO.setLogistics(logistics);
            labelOrderDTO.setStoreLogistics(storeLogistics);
            labelOrderDTO.setStoreDeliverGoodsAddressDTO(storeDeliverGoodsAddressDTO);
            //触发电子面单
            Map result = logisticsPluginFactory.filePlugin().labelOrder(labelOrderDTO);
            if (result != null && result.get("logisticCode") != null) {
                orderFeignClient.delivery(orderSn, result.get("logisticCode").toString(), logistics.getId());
            }
            return result;
        } else {
            throw new ServiceException(ResultCode.ORDER_LABEL_ORDER_ERROR);
        }

    }

    @Override
    public List<Logistics> getOpenLogistics() {
        LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Logistics::getDisabled, SwitchEnum.OPEN.name());
        return this.list(queryWrapper);
    }

    @Override
    public LogisticsSetting getLogisticsSetting() {
        Setting setting = settingService.get(SettingEnum.LOGISTICS_SETTING.name());
        return JSONUtil.toBean(setting.getSettingValue(), LogisticsSetting.class);
    }


    /** 本地物流实体直接作为插件输入，跨服务数据来自 API DTO。 */

}
