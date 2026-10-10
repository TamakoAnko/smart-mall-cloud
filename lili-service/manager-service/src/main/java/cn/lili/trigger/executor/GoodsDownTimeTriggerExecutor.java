package cn.lili.trigger.executor;

import cn.hutool.json.JSONUtil;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import com.xzj.config.GoodsFeignClient;
import cn.lili.trigger.TimeTriggerExecutor;
import cn.lili.trigger.message.GoodsMarketMessage;
import cn.lili.trigger.model.TimeExecuteConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品定时下架执行器
 */
@Slf4j
@Component(TimeExecuteConstant.GOODS_DOWN_EXECUTOR)
public class GoodsDownTimeTriggerExecutor implements TimeTriggerExecutor {

    @Autowired
    private GoodsFeignClient goodsFeignClient;

    @Override
    public void execute(Object object) {
        GoodsMarketMessage message = JSONUtil.toBean(JSONUtil.parseObj(object), GoodsMarketMessage.class);
        if (message == null || message.getGoodsIds() == null || message.getGoodsIds().isEmpty()) {
            return;
        }
        log.info("定时下架商品：{}", message.getGoodsIds());
        goodsFeignClient.updateMarketStatus(message.getGoodsIds(), GoodsStatusEnum.DOWN.name(), message.getReason() == null ? "定时下架" : message.getReason());
    }
}
