package com.xzj.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/** 商品服务内店铺数据接口，店铺模块与商品模块同属 goods-service。 */
@FeignClient(name = "goods-service", contextId = "storeFeignClient", path = "/store")
public interface StoreFeignClient {
    @GetMapping("/inner/deliver-address")
    StoreDeliverAddressDTO getDeliverAddress(@RequestParam("storeId") String storeId);

    @GetMapping("/inner/after-sale-address")
    StoreAfterSaleAddressDTO getAfterSaleAddress(@RequestParam("storeId") String storeId);

    @GetMapping("/inner/logistics")
    StoreLogisticsDTO getLogistics(@RequestParam("storeId") String storeId,
                                   @RequestParam("logisticsId") String logisticsId);

    @GetMapping("/inner/{storeId}")
    StoreSummaryDTO getStore(@org.springframework.web.bind.annotation.PathVariable("storeId") String storeId);

    @org.springframework.web.bind.annotation.PostMapping("/inner/collection-count")
    Boolean updateCollectionCount(@RequestParam("storeId") String storeId, @RequestParam("delta") Integer delta);

    @GetMapping("/inner/address/{addressId}")
    StoreAddressDTO getAddress(@org.springframework.web.bind.annotation.PathVariable("addressId") String addressId);

    class StoreSummaryDTO implements Serializable {
        private String id;
        private String storeName;
        private String storeLogo;
        private String storeDisable;
        private Boolean selfPickFlag;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String value) { storeName = value; }
        public String getStoreLogo() { return storeLogo; }
        public void setStoreLogo(String value) { storeLogo = value; }
        public String getStoreDisable() { return storeDisable; }
        public void setStoreDisable(String value) { storeDisable = value; }
        public Boolean getSelfPickFlag() { return selfPickFlag; }
        public void setSelfPickFlag(Boolean value) { selfPickFlag = value; }
    }

    class StoreDeliverAddressDTO implements Serializable {
        private String salesConsignorName;
        private String salesConsignorMobile;
        private String salesConsignorAddressId;
        private String salesConsignorAddressPath;
        private String salesConsignorDetail;

        public String getSalesConsignorName() { return salesConsignorName; }
        public void setSalesConsignorName(String value) { salesConsignorName = value; }
        public String getSalesConsignorMobile() { return salesConsignorMobile; }
        public void setSalesConsignorMobile(String value) { salesConsignorMobile = value; }
        public String getSalesConsignorAddressId() { return salesConsignorAddressId; }
        public void setSalesConsignorAddressId(String value) { salesConsignorAddressId = value; }
        public String getSalesConsignorAddressPath() { return salesConsignorAddressPath; }
        public void setSalesConsignorAddressPath(String value) { salesConsignorAddressPath = value; }
        public String getSalesConsignorDetail() { return salesConsignorDetail; }
        public void setSalesConsignorDetail(String value) { salesConsignorDetail = value; }
    }

    class StoreLogisticsDTO implements Serializable {
        private String storeId;
        private String logisticsId;
        private String customerName;
        private String customerPwd;
        private String monthCode;
        private String sendSite;
        private String sendStaff;
        private boolean faceSheetFlag;
        private String payType;
        private String expType;
        private String partnerName;

        public String getStoreId() { return storeId; }
        public void setStoreId(String value) { storeId = value; }
        public String getLogisticsId() { return logisticsId; }
        public void setLogisticsId(String value) { logisticsId = value; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String value) { customerName = value; }
        public String getCustomerPwd() { return customerPwd; }
        public void setCustomerPwd(String value) { customerPwd = value; }
        public String getMonthCode() { return monthCode; }
        public void setMonthCode(String value) { monthCode = value; }
        public String getSendSite() { return sendSite; }
        public void setSendSite(String value) { sendSite = value; }
        public String getSendStaff() { return sendStaff; }
        public void setSendStaff(String value) { sendStaff = value; }
        public boolean isFaceSheetFlag() { return faceSheetFlag; }
        public void setFaceSheetFlag(boolean value) { faceSheetFlag = value; }
        public String getPayType() { return payType; }
        public void setPayType(String value) { payType = value; }
        public String getExpType() { return expType; }
        public void setExpType(String value) { expType = value; }
        public String getPartnerName() { return partnerName; }
        public void setPartnerName(String value) { partnerName = value; }
    }

    class StoreAfterSaleAddressDTO implements Serializable {
        private String salesConsigneeName;
        private String salesConsigneeMobile;
        private String salesConsigneeAddressId;
        private String salesConsigneeAddressPath;
        private String salesConsigneeDetail;
        public String getSalesConsigneeName() { return salesConsigneeName; }
        public void setSalesConsigneeName(String value) { salesConsigneeName = value; }
        public String getSalesConsigneeMobile() { return salesConsigneeMobile; }
        public void setSalesConsigneeMobile(String value) { salesConsigneeMobile = value; }
        public String getSalesConsigneeAddressId() { return salesConsigneeAddressId; }
        public void setSalesConsigneeAddressId(String value) { salesConsigneeAddressId = value; }
        public String getSalesConsigneeAddressPath() { return salesConsigneeAddressPath; }
        public void setSalesConsigneeAddressPath(String value) { salesConsigneeAddressPath = value; }
        public String getSalesConsigneeDetail() { return salesConsigneeDetail; }
        public void setSalesConsigneeDetail(String value) { salesConsigneeDetail = value; }
    }

    class StoreAddressDTO implements Serializable {
        private String id;
        private String storeId;
        private String addressName;
        private String center;
        private String address;
        private String mobile;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String value) { storeId = value; }
        public String getAddressName() { return addressName; }
        public void setAddressName(String value) { addressName = value; }
        public String getCenter() { return center; }
        public void setCenter(String value) { center = value; }
        public String getAddress() { return address; }
        public void setAddress(String value) { address = value; }
        public String getMobile() { return mobile; }
        public void setMobile(String value) { mobile = value; }
    }
}
