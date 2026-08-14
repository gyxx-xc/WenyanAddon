package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 标签匹配结果：匹配等级 + 消耗系数 + 效果增益 + 输入灵气主导属性 + 判定说明。
 */
public record QiMatch(MatchGrade grade, double costMultiplier, double gainMultiplier,
                      ElementAttribute dominant, String reason, QiComposition input) {

    public static final QiMatch STANDARD = new QiMatch(
            MatchGrade.STANDARD, 1.0, 1.0, null, "无属性倾向，标准消耗", QiComposition.EMPTY);
}
