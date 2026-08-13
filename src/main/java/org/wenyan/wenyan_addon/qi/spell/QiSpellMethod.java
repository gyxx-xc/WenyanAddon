package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;

/**
 * 符咒方法体：无论文言传入多少参数，都进入同一个方法。
 * 方法体可通过 {@link QiSpellContext#require} 登记自定义灵力消耗
 * （适应五行相生相克的增益与减益逻辑），方法体完成后统一扣费；
 * 未登记时按注解 baseCost × 匹配系数执行默认消耗。
 */
@FunctionalInterface
public interface QiSpellMethod {
    IWenyanValue invoke(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException;
}
