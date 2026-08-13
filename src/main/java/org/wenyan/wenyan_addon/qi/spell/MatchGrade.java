package org.wenyan.wenyan_addon.qi.spell;

/**
 * 标签匹配等级：输入灵气与主/辅属性标签的匹配程度。
 * 等级决定消耗系数与效果增益（定性→定量由 {@link QiSpellMatcher} 给出）。
 */
public enum MatchGrade {
    /**
     * 完美匹配：同时满足主属性标签与辅属性标签。
     */
    PERFECT,
    /**
     * 最佳增益：主属性 + 相生属性。
     */
    BEST,
    /**
     * 恰好：输入灵气恰好只有主属性标签所要求的属性。
     */
    EXACT,
    /**
     * 标准效率：无标签，或仅有主属性标签但不满足辅属性标签。
     */
    STANDARD,
    /**
     * 轻微干扰：主属性 + 中性属性。
     */
    MINOR,
    /**
     * 干扰惩罚：输入灵气包含与主属性或辅属性相克的属性。
     */
    INTERFERENCE,
    /**
     * 强行施放：输入灵气缺少主属性标签所要求的属性。
     */
    MISSING
}
