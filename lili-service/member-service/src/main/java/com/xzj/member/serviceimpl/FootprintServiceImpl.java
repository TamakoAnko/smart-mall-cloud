package com.xzj.member.serviceimpl;

import cn.lili.common.security.context.UserContext;
import com.xzj.member.entity.dos.FootPrint;
import com.xzj.member.entity.dto.FootPrintQueryParams;
import com.xzj.member.mapper.FootprintMapper;
import com.xzj.member.service.FootprintService;
import com.xzj.member.entity.vo.GoodsIndex;
import cn.lili.mybatis.util.PageUtil;
import com.xzj.config.GoodsSkuFeignClient;
import com.xzj.entity.GoodsSkuDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 客户浏览历史业务层实现
 *
 * @author Chopper
 * @since 2020/11/18 10:46 上午
 */
@Service("memberFootprintService")
public class FootprintServiceImpl extends ServiceImpl<FootprintMapper, FootPrint> implements FootprintService {


    @Autowired
    private GoodsSkuFeignClient goodsSkuFeignClient;

    @Override
    public FootPrint saveFootprint(FootPrint footPrint) {
        LambdaQueryWrapper<FootPrint> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(FootPrint::getMemberId, footPrint.getMemberId());
        queryWrapper.eq(FootPrint::getGoodsId, footPrint.getGoodsId());
        //如果已存在某商品记录，则更新其修改时间
        //如果不存在则添加记录
        //为了保证足迹的排序,将原本足迹删除后重新添加
        List<FootPrint> oldPrints = list(queryWrapper);
        if (oldPrints != null && !oldPrints.isEmpty()) {
            FootPrint oldPrint = oldPrints.get(0);
            this.removeById(oldPrint.getId());
        }
        footPrint.setCreateTime(new Date());
        this.save(footPrint);
        //删除超过100条后的记录
        this.baseMapper.deleteLastFootPrint(footPrint.getMemberId());
        return footPrint;
    }

    @Override
    public boolean clean() {
        LambdaQueryWrapper<FootPrint> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FootPrint::getMemberId, UserContext.getCurrentUser().getId());
        return this.remove(lambdaQueryWrapper);
    }

    @Override
    public boolean deleteByIds(List<String> ids) {
        LambdaQueryWrapper<FootPrint> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FootPrint::getMemberId, UserContext.getCurrentUser().getId());
        lambdaQueryWrapper.in(FootPrint::getGoodsId, ids);
        return this.remove(lambdaQueryWrapper);
    }

    @Override
    public IPage<GoodsIndex> footPrintPage(FootPrintQueryParams params) {
        params.setSort("createTime");
        Page<FootPrint> footPrintPages = this.page(PageUtil.initPage(params), params.queryWrapper());
        //定义结果
        Page<GoodsIndex> goodsIndexIPage = new Page<>();

        if (footPrintPages.getRecords() != null && !footPrintPages.getRecords().isEmpty()) {
            List<String> skuIds = footPrintPages.getRecords().stream().map(FootPrint::getSkuId).collect(Collectors.toList());
            List<GoodsSkuDTO> skuList = goodsSkuFeignClient.findByIds(skuIds);
            if (skuList == null || skuList.isEmpty()) {
                return goodsIndexIPage;
            }

            var skuById = skuList.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(GoodsSkuDTO::getId, item -> item, (first, ignored) -> first));
            // 按会员足迹分页顺序组装，避免依赖远程数据库返回顺序。
            List<GoodsIndex> collect = footPrintPages.getRecords().stream()
                    .map(footPrint -> Optional.ofNullable(skuById.get(footPrint.getSkuId()))
                            .map(sku -> toGoodsIndex(sku, footPrint.getCreateTime()))
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            goodsIndexIPage.setPages(footPrintPages.getPages());
            goodsIndexIPage.setRecords(collect);
            goodsIndexIPage.setTotal(footPrintPages.getTotal());
            goodsIndexIPage.setSize(footPrintPages.getSize());
            goodsIndexIPage.setCurrent(footPrintPages.getCurrent());
            return goodsIndexIPage;
        }
        return goodsIndexIPage;
    }

    /** 将商品服务返回的 DTO 转换为会员服务自己的展示对象。 */
    private GoodsIndex toGoodsIndex(GoodsSkuDTO sku, Date createTime) {
        GoodsIndex index = new GoodsIndex();
        BeanUtils.copyProperties(sku, index);
        index.setBuyCount((sku.getBuyCount() == null ? 0 : sku.getBuyCount())
                + (sku.getVirtualSales() == null ? 0 : sku.getVirtualSales()));
        index.setReleaseTime(createTime == null ? new Date() : createTime);
        return index;
    }

    @Override
    public long getFootprintNum() {
        LambdaQueryWrapper<FootPrint> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FootPrint::getMemberId, Objects.requireNonNull(UserContext.getCurrentUser()).getId());
        lambdaQueryWrapper.eq(FootPrint::getDeleteFlag, false);
        return this.count(lambdaQueryWrapper);
    }
}