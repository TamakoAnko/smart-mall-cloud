package com.xzj.member.controller;

import com.xzj.member.MemberFeignClient.MemberSummaryDTO;
import com.xzj.member.entity.dos.Member;
import com.xzj.member.entity.dto.EvaluationQueryParams;
import com.xzj.member.service.MemberEvaluationService;
import com.xzj.member.service.MemberAddressService;
import com.xzj.member.service.MemberReceiptService;
import com.xzj.member.service.FootprintService;
import com.xzj.member.entity.vo.MemberReceiptVO;
import com.xzj.member.entity.vo.MemberReceiptAddVO;
import com.xzj.member.entity.dos.MemberReceipt;
import cn.lili.common.vo.PageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xzj.member.entity.dos.MemberAddress;
import com.xzj.member.service.MemberService;
import com.xzj.member.token.MemberTokenGenerate;
import com.xzj.passport.connect.config.ConnectAuthEnum;
import com.xzj.passport.connect.entity.enums.ConnectEnum;
import com.xzj.passport.connect.entity.dto.ConnectAuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员服务内部接口，仅返回跨服务业务所需的数据。
 */
@RestController
@RequestMapping("/member/inner")
public class MemberInnerController {

    private final MemberService memberService;
    private final MemberEvaluationService memberEvaluationService;
    private final MemberTokenGenerate memberTokenGenerate;
    private final MemberAddressService memberAddressService;
    private final MemberReceiptService memberReceiptService;
    private final FootprintService footprintService;
    private final com.xzj.passport.connect.service.ConnectService connectService;

    public MemberInnerController(MemberService memberService, MemberEvaluationService memberEvaluationService,
                                 MemberTokenGenerate memberTokenGenerate, MemberAddressService memberAddressService,
                                 MemberReceiptService memberReceiptService,
                                 com.xzj.passport.connect.service.ConnectService connectService,
                                 FootprintService footprintService) {
        this.memberService = memberService;
        this.memberEvaluationService = memberEvaluationService;
        this.memberTokenGenerate = memberTokenGenerate;
        this.memberAddressService = memberAddressService;
        this.memberReceiptService = memberReceiptService;
        this.connectService = connectService;
        this.footprintService = footprintService;
    }

    /** 查询订单和钱包业务需要的会员摘要。 */
    @GetMapping("/{memberId}")
    public MemberSummaryDTO getMemberById(@PathVariable String memberId) {
        Member member = memberService.getById(memberId);
        if (member == null) {
            return null;
        }
        MemberSummaryDTO dto = new MemberSummaryDTO();
        dto.setId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setNickName(member.getNickName());
        dto.setMobile(member.getMobile());
        dto.setDisabled(member.getDisabled());
        dto.setFace(member.getFace());
        dto.setPoint(member.getPoint());
        dto.setHaveStore(member.getHaveStore());
        dto.setStoreId(member.getStoreId());
        return dto;
    }

    @GetMapping("/by-mobile")
    public MemberSummaryDTO getMemberByMobile(@RequestParam String mobile) {
        Member member = memberService.findByMobile(mobile);
        return member == null ? null : toSummary(member);
    }

    @PostMapping("/store")
    public Boolean updateStore(@RequestParam String memberId, @RequestParam Boolean haveStore,
                               @RequestParam(required = false) String storeId) {
        Member member = memberService.getById(memberId);
        if (member == null) return false;
        member.setHaveStore(haveStore);
        member.setStoreId(storeId);
        return memberService.updateById(member);
    }

    @GetMapping("/footprints")
    public List<com.xzj.member.MemberFeignClient.FootprintDTO> getFootprints(
            @RequestParam String memberId, @RequestParam String storeId) {
        return footprintService.lambdaQuery()
                .eq(com.xzj.member.entity.dos.FootPrint::getMemberId, memberId)
                .eq(com.xzj.member.entity.dos.FootPrint::getStoreId, storeId)
                .list().stream().map(item -> {
                    com.xzj.member.MemberFeignClient.FootprintDTO dto = new com.xzj.member.MemberFeignClient.FootprintDTO();
                    dto.setGoodsId(item.getGoodsId());
                    dto.setSkuId(item.getSkuId());
                    dto.setStoreId(item.getStoreId());
                    return dto;
                }).toList();
    }

    @GetMapping("/names")
    public List<java.util.Map<String, Object>> getMemberNames(@RequestParam List<String> ids) {
        return memberService.listFieldsByMemberIds("id,nick_name", ids);
    }

    @GetMapping("/address/default")
    public com.xzj.member.MemberFeignClient.MemberAddressDTO getDefaultAddress() {
        return toAddress(memberAddressService.getDefaultMemberAddress());
    }

    @GetMapping("/address/{addressId}")
    public com.xzj.member.MemberFeignClient.MemberAddressDTO getAddress(@PathVariable String addressId) {
        return toAddress(memberAddressService.getMemberAddress(addressId));
    }

    private com.xzj.member.MemberFeignClient.MemberAddressDTO toAddress(MemberAddress source) {
        if (source == null) return null;
        com.xzj.member.MemberFeignClient.MemberAddressDTO target = new com.xzj.member.MemberFeignClient.MemberAddressDTO();
        target.setId(source.getId());
        target.setMemberId(source.getMemberId());
        target.setName(source.getName());
        target.setMobile(source.getMobile());
        target.setConsigneeAddressPath(source.getConsigneeAddressPath());
        target.setConsigneeAddressIdPath(source.getConsigneeAddressIdPath());
        target.setDetail(source.getDetail());
        target.setIsDefault(source.getIsDefault());
        target.setAlias(source.getAlias());
        target.setLon(source.getLon());
        target.setLat(source.getLat());
        return target;
    }

    /** 订单结算扣减或返还会员积分。 */
    @PostMapping("/point")
    public Boolean updateMemberPoint(@RequestParam Long point,
                                     @RequestParam String type,
                                     @RequestParam String memberId,
                                     @RequestParam String content,
                                     @RequestParam String pointSource) {
        return memberService.updateMemberPoint(point, type, memberId, content, pointSource);
    }

    /** 商品服务只需要评价数量，不共享评价实体和 Mapper。 */
    @GetMapping("/evaluation/count")
    public Long getEvaluationCount(@RequestParam String goodsId,
                                   @RequestParam String status,
                                   @RequestParam(required = false) String grade) {
        return memberEvaluationService.getEvaluationCount(EvaluationQueryParams.builder()
                .goodsId(goodsId).status(status).grade(grade).build());
    }

    @PostMapping("/connect-login")
    public com.xzj.member.MemberFeignClient.ConnectLoginResultDTO connectLogin(
            @RequestBody com.xzj.member.MemberFeignClient.ConnectLoginRequestDTO request) {
        ConnectAuthUser authUser = new ConnectAuthUser();
        authUser.setUsername(request.getUsername());
        authUser.setNickname(request.getNickname());
        authUser.setAvatar(request.getAvatar());
        authUser.setPhone(request.getPhone());
        authUser.setSource(ConnectEnum.valueOf(request.getSource()));
        authUser.setType(cn.lili.common.enums.ClientTypeEnum.valueOf(request.getClientType()));
        Member member = request.getMemberId() == null ? null : memberService.getById(request.getMemberId());
        if (member == null && request.getPhone() != null) {
            member = memberService.findByMobile(request.getPhone());
        }
        if (member == null) {
            member = memberService.autoRegister(authUser);
        }
        cn.lili.common.security.token.Token token = memberTokenGenerate.createToken(member,
                Boolean.TRUE.equals(request.getLongTerm()));
        com.xzj.member.MemberFeignClient.ConnectLoginResultDTO result =
                new com.xzj.member.MemberFeignClient.ConnectLoginResultDTO();
        result.setMember(toSummary(member));
        result.setAccessToken(token.getAccessToken());
        result.setRefreshToken(token.getRefreshToken());
        return result;
    }

    @GetMapping("/connect")
    public com.xzj.member.MemberFeignClient.ConnectDTO getConnect(@RequestParam String memberId,
                                                                   @RequestParam String unionType) {
        com.xzj.passport.connect.entity.Connect connect = connectService.queryConnect(
                com.xzj.member.entity.dto.ConnectQueryDTO.builder()
                        .userId(memberId).unionType(unionType).build());
        if (connect == null) {
            return null;
        }
        com.xzj.member.MemberFeignClient.ConnectDTO result = new com.xzj.member.MemberFeignClient.ConnectDTO();
        result.setUserId(connect.getUserId());
        result.setUnionId(connect.getUnionId());
        result.setUnionType(connect.getUnionType());
        return result;
    }

    @GetMapping("/connect/by-union")
    public com.xzj.member.MemberFeignClient.ConnectDTO getConnectByUnion(@RequestParam String unionId,
                                                                          @RequestParam String unionType) {
        com.xzj.passport.connect.entity.Connect connect = connectService.queryConnect(
                com.xzj.member.entity.dto.ConnectQueryDTO.builder()
                        .unionId(unionId).unionType(unionType).build());
        if (connect == null) {
            return null;
        }
        com.xzj.member.MemberFeignClient.ConnectDTO result = new com.xzj.member.MemberFeignClient.ConnectDTO();
        result.setUserId(connect.getUserId());
        result.setUnionId(connect.getUnionId());
        result.setUnionType(connect.getUnionType());
        return result;
    }

    @PostMapping("/connect/bind")
    public Boolean bindConnect(@RequestParam String memberId, @RequestParam String unionId,
                               @RequestParam String unionType) {
        connectService.loginBindUser(memberId, unionId, unionType);
        return true;
    }

    @GetMapping("/receipt")
    public com.xzj.member.MemberFeignClient.MemberReceiptPageDTO getReceiptPage(
            @RequestParam String memberId,
            @RequestParam(required = false) String receiptType,
            @RequestParam long page,
            @RequestParam long size) {
        MemberReceiptVO query = new MemberReceiptVO();
        query.setMemberId(memberId);
        query.setReceiptType(receiptType);
        PageVO pageVO = new PageVO();
        pageVO.setPageNumber((int) page);
        pageVO.setPageSize((int) size);
        IPage<MemberReceipt> source = memberReceiptService.getPage(query, pageVO);
        com.xzj.member.MemberFeignClient.MemberReceiptPageDTO result = new com.xzj.member.MemberFeignClient.MemberReceiptPageDTO();
        result.setCurrent(source.getCurrent());
        result.setSize(source.getSize());
        result.setTotal(source.getTotal());
        result.setRecords(source.getRecords().stream().map(this::toReceipt).toList());
        return result;
    }

    @PostMapping("/receipt")
    public Boolean addReceipt(@RequestBody com.xzj.member.MemberFeignClient.MemberReceiptCommandDTO request) {
        return memberReceiptService.addMemberReceipt(toReceiptAdd(request), request.getMemberId());
    }

    @org.springframework.web.bind.annotation.PutMapping("/receipt/{id}")
    public Boolean updateReceipt(@PathVariable String id,
                                 @RequestBody com.xzj.member.MemberFeignClient.MemberReceiptCommandDTO request) {
        request.setId(id);
        return memberReceiptService.editMemberReceipt(toReceiptAdd(request), request.getMemberId());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/receipt/{id}")
    public Boolean deleteReceipt(@PathVariable String id) {
        return memberReceiptService.deleteMemberReceipt(id);
    }

    private com.xzj.member.MemberFeignClient.MemberReceiptDTO toReceipt(MemberReceipt source) {
        com.xzj.member.MemberFeignClient.MemberReceiptDTO target = new com.xzj.member.MemberFeignClient.MemberReceiptDTO();
        target.setId(source.getId());
        target.setMemberId(source.getMemberId());
        target.setMemberName(source.getMemberName());
        target.setReceiptTitle(source.getReceiptTitle());
        target.setTaxpayerId(source.getTaxpayerId());
        target.setReceiptContent(source.getReceiptContent());
        target.setReceiptType(source.getReceiptType());
        target.setIsDefault(source.getIsDefault());
        target.setDeleteFlag(source.getDeleteFlag());
        target.setCreateTime(source.getCreateTime());
        return target;
    }

    private MemberReceiptAddVO toReceiptAdd(com.xzj.member.MemberFeignClient.MemberReceiptCommandDTO source) {
        MemberReceiptAddVO target = new MemberReceiptAddVO();
        target.setId(source.getId());
        target.setReceiptTitle(source.getReceiptTitle());
        target.setTaxpayerId(source.getTaxpayerId());
        target.setReceiptContent(source.getReceiptContent());
        target.setReceiptType(source.getReceiptType());
        target.setIsDefault(source.getIsDefault());
        return target;
    }

    private MemberSummaryDTO toSummary(Member member) {
        MemberSummaryDTO dto = new MemberSummaryDTO();
        dto.setId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setNickName(member.getNickName());
        dto.setMobile(member.getMobile());
        dto.setDisabled(member.getDisabled());
        dto.setFace(member.getFace());
        dto.setPoint(member.getPoint());
        dto.setHaveStore(member.getHaveStore());
        dto.setStoreId(member.getStoreId());
        return dto;
    }
}
