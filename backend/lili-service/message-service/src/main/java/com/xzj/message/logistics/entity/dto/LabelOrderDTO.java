package com.xzj.message.logistics.entity.dto;

import com.xzj.config.StoreFeignClient;
import com.xzj.order.OrderFeignClient;
import com.xzj.other.system.entity.dos.Logistics;
import lombok.Data;

import java.util.List;

/**
 * 电子面单DTO
 *
 * @author Bulbasaur
 * @since 2023-02-16
 */
@Data
public class LabelOrderDTO {

    //订单
    OrderFeignClient.OrderSummaryDTO order;
    //订单货物
    List<OrderFeignClient.OrderItemSummaryDTO> orderItems;
    //物流公司
    Logistics logistics;
    //店铺物流公司配置
    StoreFeignClient.StoreLogisticsDTO storeLogistics;
    //店铺发件地址
    StoreFeignClient.StoreDeliverAddressDTO storeDeliverGoodsAddressDTO;
}
