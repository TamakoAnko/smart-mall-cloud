package com.xzj.wallet.controller;


import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import com.xzj.member.MemberFeignClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * 买家端,客户发票接口
 *
 * @author paulG
 * @since 2021-03-29 14:10:16
 */
@RestController
@Tag(name = "买家端,客户发票接口")
@RequestMapping("/buyer/wallet/receipt")
public class MemberReceiptController {

    @Autowired
    private MemberFeignClient memberFeignClient;

    @Operation(summary = "查询客户发票列表")
    @GetMapping
    public ResultMessage<Object> page(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtil.data(memberFeignClient.getReceiptPage(
                UserContext.getCurrentUser().getId(), null,
                pageNumber == null ? 1 : pageNumber,
                pageSize == null ? 10 : pageSize));
    }

    @PreventDuplicateSubmissions
    @Operation(summary = "新增客户发票")
    @PostMapping
    public ResultMessage<Object> add(MemberFeignClient.MemberReceiptCommandDTO request) {
        request.setMemberId(UserContext.getCurrentUser().getId());
        return ResultUtil.data(memberFeignClient.addReceipt(request));
    }

    @Operation(summary = "修改客户发票")
    @Parameter(name = "id", description = "客户发票id", required = true)
    @PutMapping("/{id}")
    public ResultMessage<Object> update(@PathVariable String id, MemberFeignClient.MemberReceiptCommandDTO request) {
        request.setMemberId(UserContext.getCurrentUser().getId());
        return ResultUtil.data(memberFeignClient.updateReceipt(id, request));
    }

    @Operation(summary = "客户发票删除")
    @Parameter(name = "id", description = "客户发票id", required = true)
    @DeleteMapping
    public ResultMessage<Boolean> deleteMessage(@PathVariable String id) {
        return ResultUtil.data(memberFeignClient.deleteReceipt(id));
    }

}
