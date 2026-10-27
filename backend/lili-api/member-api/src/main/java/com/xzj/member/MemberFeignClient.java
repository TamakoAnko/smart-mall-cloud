package com.xzj.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.Date;

import java.io.Serializable;

@FeignClient(name = "member-service", contextId = "memberFeignClient", path = "/member")
public interface MemberFeignClient {

    @GetMapping("/inner/{memberId}")
    MemberSummaryDTO getMemberById(@PathVariable("memberId") String memberId);

    @GetMapping("/inner/by-mobile")
    MemberSummaryDTO getMemberByMobile(@RequestParam("mobile") String mobile);

    @PostMapping("/inner/store")
    Boolean updateStore(@RequestParam("memberId") String memberId,
                        @RequestParam("haveStore") Boolean haveStore,
                        @RequestParam(value = "storeId", required = false) String storeId);

    @GetMapping("/inner/footprints")
    java.util.List<FootprintDTO> getFootprints(@RequestParam("memberId") String memberId,
                                                @RequestParam("storeId") String storeId);

    @GetMapping("/inner/names")
    java.util.List<java.util.Map<String, Object>> getMemberNames(@RequestParam("ids") java.util.List<String> ids);

    @GetMapping("/inner/address/default")
    MemberAddressDTO getDefaultAddress();

    @GetMapping("/inner/address/{addressId}")
    MemberAddressDTO getAddress(@PathVariable("addressId") String addressId);

    @PostMapping("/inner/point")
    Boolean updateMemberPoint(@RequestParam("point") Long point,
                              @RequestParam("type") String type,
                              @RequestParam("memberId") String memberId,
                              @RequestParam("content") String content,
                              @RequestParam("pointSource") String pointSource);

    @GetMapping("/inner/evaluation/count")
    Long getEvaluationCount(@RequestParam("goodsId") String goodsId,
                             @RequestParam("status") String status,
                             @RequestParam(value = "grade", required = false) String grade);

    @PostMapping("/inner/connect-login")
    ConnectLoginResultDTO connectLogin(@RequestBody ConnectLoginRequestDTO request);

    @GetMapping("/inner/connect")
    ConnectDTO getConnect(@RequestParam("memberId") String memberId,
                          @RequestParam("unionType") String unionType);

    @GetMapping("/inner/connect/by-union")
    ConnectDTO getConnectByUnion(@RequestParam("unionId") String unionId,
                                  @RequestParam("unionType") String unionType);

    @PostMapping("/inner/connect/bind")
    Boolean bindConnect(@RequestParam("memberId") String memberId,
                        @RequestParam("unionId") String unionId,
                        @RequestParam("unionType") String unionType);

    @GetMapping("/inner/receipt")
    MemberReceiptPageDTO getReceiptPage(@RequestParam("memberId") String memberId,
                                        @RequestParam(value = "receiptType", required = false) String receiptType,
                                        @RequestParam("page") long page,
                                        @RequestParam("size") long size);

    @PostMapping("/inner/receipt")
    Boolean addReceipt(@RequestBody MemberReceiptCommandDTO request);

    @PutMapping("/inner/receipt/{id}")
    Boolean updateReceipt(@PathVariable("id") String id, @RequestBody MemberReceiptCommandDTO request);

    @DeleteMapping("/inner/receipt/{id}")
    Boolean deleteReceipt(@PathVariable("id") String id);

    /** 订单服务只需要的会员摘要，避免跨服务暴露会员实体。 */
    class MemberSummaryDTO implements Serializable {
        private String id;
        private String username;
        private String nickName;
        private String mobile;
        private Boolean disabled;
        private String face;
        private Long point;
        private Boolean haveStore;
        private String storeId;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickName() { return nickName; }
        public void setNickName(String nickName) { this.nickName = nickName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public Boolean getDisabled() { return disabled; }
        public void setDisabled(Boolean disabled) { this.disabled = disabled; }
        public String getFace() { return face; }
        public void setFace(String face) { this.face = face; }
        public Long getPoint() { return point; }
        public void setPoint(Long point) { this.point = point; }
        public Boolean getHaveStore() { return haveStore; }
        public void setHaveStore(Boolean haveStore) { this.haveStore = haveStore; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String storeId) { this.storeId = storeId; }
    }

    class FootprintDTO implements Serializable {
        private String memberId;
        private String goodsId;
        private String skuId;
        private String storeId;
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public String getGoodsId() { return goodsId; }
        public void setGoodsId(String value) { goodsId = value; }
        public String getSkuId() { return skuId; }
        public void setSkuId(String value) { skuId = value; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String value) { storeId = value; }
    }

    class MemberAddressDTO implements Serializable {
        private String id;
        private String memberId;
        private String name;
        private String mobile;
        private String consigneeAddressPath;
        private String consigneeAddressIdPath;
        private String detail;
        private Boolean isDefault;
        private String alias;
        private String lon;
        private String lat;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public String getName() { return name; }
        public void setName(String value) { name = value; }
        public String getMobile() { return mobile; }
        public void setMobile(String value) { mobile = value; }
        public String getConsigneeAddressPath() { return consigneeAddressPath; }
        public void setConsigneeAddressPath(String value) { consigneeAddressPath = value; }
        public String getConsigneeAddressIdPath() { return consigneeAddressIdPath; }
        public void setConsigneeAddressIdPath(String value) { consigneeAddressIdPath = value; }
        public String getDetail() { return detail; }
        public void setDetail(String value) { detail = value; }
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean value) { isDefault = value; }
        public String getAlias() { return alias; }
        public void setAlias(String value) { alias = value; }
        public String getLon() { return lon; }
        public void setLon(String value) { lon = value; }
        public String getLat() { return lat; }
        public void setLat(String value) { lat = value; }
    }

    class ConnectLoginRequestDTO implements Serializable {
        private String username;
        private String nickname;
        private String avatar;
        private String phone;
        private String source;
        private String clientType;
        private String unionId;
        private String memberId;
        private Boolean longTerm;
        public String getUsername() { return username; }
        public void setUsername(String value) { username = value; }
        public String getNickname() { return nickname; }
        public void setNickname(String value) { nickname = value; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String value) { avatar = value; }
        public String getPhone() { return phone; }
        public void setPhone(String value) { phone = value; }
        public String getSource() { return source; }
        public void setSource(String value) { source = value; }
        public String getClientType() { return clientType; }
        public void setClientType(String value) { clientType = value; }
        public String getUnionId() { return unionId; }
        public void setUnionId(String value) { unionId = value; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public Boolean getLongTerm() { return longTerm; }
        public void setLongTerm(Boolean value) { longTerm = value; }
    }

    class ConnectLoginResultDTO implements Serializable {
        private MemberSummaryDTO member;
        private String accessToken;
        private String refreshToken;
        public MemberSummaryDTO getMember() { return member; }
        public void setMember(MemberSummaryDTO value) { member = value; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String value) { accessToken = value; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String value) { refreshToken = value; }
    }

    class ConnectDTO implements Serializable {
        private String userId;
        private String unionId;
        private String unionType;
        public String getUserId() { return userId; }
        public void setUserId(String value) { userId = value; }
        public String getUnionId() { return unionId; }
        public void setUnionId(String value) { unionId = value; }
        public String getUnionType() { return unionType; }
        public void setUnionType(String value) { unionType = value; }
    }

    class MemberReceiptCommandDTO implements Serializable {
        private String id;
        private String memberId;
        private String memberName;
        private String receiptTitle;
        private String taxpayerId;
        private String receiptContent;
        private String receiptType;
        private Integer isDefault;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String value) { memberName = value; }
        public String getReceiptTitle() { return receiptTitle; }
        public void setReceiptTitle(String value) { receiptTitle = value; }
        public String getTaxpayerId() { return taxpayerId; }
        public void setTaxpayerId(String value) { taxpayerId = value; }
        public String getReceiptContent() { return receiptContent; }
        public void setReceiptContent(String value) { receiptContent = value; }
        public String getReceiptType() { return receiptType; }
        public void setReceiptType(String value) { receiptType = value; }
        public Integer getIsDefault() { return isDefault; }
        public void setIsDefault(Integer value) { isDefault = value; }
    }

    class MemberReceiptDTO implements Serializable {
        private String id;
        private String memberId;
        private String memberName;
        private String receiptTitle;
        private String taxpayerId;
        private String receiptContent;
        private String receiptType;
        private Integer isDefault;
        private Boolean deleteFlag;
        private Date createTime;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String value) { memberName = value; }
        public String getReceiptTitle() { return receiptTitle; }
        public void setReceiptTitle(String value) { receiptTitle = value; }
        public String getTaxpayerId() { return taxpayerId; }
        public void setTaxpayerId(String value) { taxpayerId = value; }
        public String getReceiptContent() { return receiptContent; }
        public void setReceiptContent(String value) { receiptContent = value; }
        public String getReceiptType() { return receiptType; }
        public void setReceiptType(String value) { receiptType = value; }
        public Integer getIsDefault() { return isDefault; }
        public void setIsDefault(Integer value) { isDefault = value; }
        public Boolean getDeleteFlag() { return deleteFlag; }
        public void setDeleteFlag(Boolean value) { deleteFlag = value; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date value) { createTime = value; }
    }

    class MemberReceiptPageDTO implements Serializable {
        private long current;
        private long size;
        private long total;
        private java.util.List<MemberReceiptDTO> records;
        public long getCurrent() { return current; }
        public void setCurrent(long value) { current = value; }
        public long getSize() { return size; }
        public void setSize(long value) { size = value; }
        public long getTotal() { return total; }
        public void setTotal(long value) { total = value; }
        public java.util.List<MemberReceiptDTO> getRecords() { return records; }
        public void setRecords(java.util.List<MemberReceiptDTO> value) { records = value; }
    }
}
