package com.xzj.store.controller;

import com.xzj.config.StoreFeignClient;
import com.xzj.store.entity.dos.StoreLogistics;
import com.xzj.store.entity.dto.StoreDeliverGoodsAddressDTO;
import com.xzj.store.entity.dto.StoreAfterSaleAddressDTO;
import com.xzj.store.entity.dos.Store;
import com.xzj.store.service.StoreService;
import com.xzj.store.service.StoreDetailService;
import com.xzj.store.service.StoreAddressService;
import com.xzj.store.mapper.StoreLogisticsMapper;
import com.xzj.store.entity.dos.StoreAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store/inner")
public class StoreInnerController {
    private final StoreDetailService storeDetailService;
    private final StoreLogisticsMapper storeLogisticsMapper;
    private final StoreService storeService;
    private final StoreAddressService storeAddressService;

    public StoreInnerController(StoreDetailService storeDetailService, StoreLogisticsMapper storeLogisticsMapper,
                                StoreService storeService, StoreAddressService storeAddressService) {
        this.storeDetailService = storeDetailService;
        this.storeLogisticsMapper = storeLogisticsMapper;
        this.storeService = storeService;
        this.storeAddressService = storeAddressService;
    }

    @GetMapping("/deliver-address")
    public StoreFeignClient.StoreDeliverAddressDTO getDeliverAddress(@RequestParam("storeId") String storeId) {
        StoreDeliverGoodsAddressDTO source = storeDetailService.getStoreDeliverGoodsAddressDto(storeId);
        if (source == null) return null;
        StoreFeignClient.StoreDeliverAddressDTO target = new StoreFeignClient.StoreDeliverAddressDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @GetMapping("/after-sale-address")
    public StoreFeignClient.StoreAfterSaleAddressDTO getAfterSaleAddress(@RequestParam("storeId") String storeId) {
        StoreAfterSaleAddressDTO source = storeDetailService.getStoreAfterSaleAddressDTO(storeId);
        if (source == null) return null;
        StoreFeignClient.StoreAfterSaleAddressDTO target = new StoreFeignClient.StoreAfterSaleAddressDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @GetMapping("/logistics")
    public StoreFeignClient.StoreLogisticsDTO getLogistics(@RequestParam("storeId") String storeId,
                                                           @RequestParam("logisticsId") String logisticsId) {
        StoreLogistics source = storeLogisticsMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StoreLogistics>()
                .eq(StoreLogistics::getStoreId, storeId)
                .eq(StoreLogistics::getLogisticsId, logisticsId));
        if (source == null) return null;
        StoreFeignClient.StoreLogisticsDTO target = new StoreFeignClient.StoreLogisticsDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @GetMapping("/{storeId}")
    public StoreFeignClient.StoreSummaryDTO getStore(@org.springframework.web.bind.annotation.PathVariable String storeId) {
        Store source = storeService.getById(storeId);
        if (source == null) return null;
        StoreFeignClient.StoreSummaryDTO target = new StoreFeignClient.StoreSummaryDTO();
        target.setId(source.getId());
        target.setStoreName(source.getStoreName());
        target.setStoreLogo(source.getStoreLogo());
        target.setStoreDisable(source.getStoreDisable());
        target.setSelfPickFlag(source.getSelfPickFlag());
        return target;
    }

    @GetMapping("/address/{addressId}")
    public StoreFeignClient.StoreAddressDTO getAddress(@org.springframework.web.bind.annotation.PathVariable String addressId) {
        StoreAddress source = storeAddressService.getById(addressId);
        if (source == null) return null;
        StoreFeignClient.StoreAddressDTO target = new StoreFeignClient.StoreAddressDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @org.springframework.web.bind.annotation.PostMapping("/collection-count")
    public Boolean updateCollectionCount(@RequestParam String storeId, @RequestParam Integer delta) {
        storeService.updateStoreCollectionNum(new com.xzj.store.entity.dto.StoreCollectionCommand(storeId, delta));
        return true;
    }
}
