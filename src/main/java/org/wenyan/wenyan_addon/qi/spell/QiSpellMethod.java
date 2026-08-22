package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.structure.IHandleContext;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;

/**
 * 符咒方法体：无论文言传入多少参数，都进入同一个方法。
 * 上下文与请求按方法声明签名注入：第一个参数为设备上下文
 * （方块：{@link indi.wenyan.content.block.runner.BlockRequest.BlockContext}；
 * 投掷物品：{@link indi.wenyan.content.entity.ThrowEntityContext}；
 * 玩家施法：{@link PlayerCastContext}；
 * 通用：{@link IHandleContext}），第二个参数为请求，第三个参数为 {@link QiSpellContext}。
 * 方法体可通过 {@link QiSpellContext#require} 登记自定义灵力消耗
 * （适应五行相生相克的增益与减益逻辑），方法体完成后统一扣费；
 * 未登记时按注解 baseCost × 匹配系数执行默认消耗。
 */
@FunctionalInterface
public interface QiSpellMethod {
    IWenyanValue invoke(IHandleContext ctx, IArgsRequest request, QiSpellContext context) throws WenyanException;
}