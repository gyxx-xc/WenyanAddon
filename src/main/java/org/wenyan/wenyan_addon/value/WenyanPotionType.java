package org.wenyan.wenyan_addon.value;

import indi.wenyan.judou.api.WenyanType;
import indi.wenyan.judou.api.language.JudouExceptionText;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanObject;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;
import net.minecraft.world.effect.MobEffectInstance;

public record WenyanPotionType(MobEffectInstance value) implements IWenyanObject {
    public static final String ATTRIBUTE_NAME = "「名」";
    public static final String ATTRIBUTE_ID = "「号」";
    public static final String ATTRIBUTE_AMPLIFIER = "「级」";
    public static final String ATTRIBUTE_DURATION = "「时」";
    public static final WenyanType<WenyanPotionType> TYPE = new WenyanType<>("药效", WenyanPotionType.class);

    @Override
    public IWenyanValue getAttribute(String name) throws WenyanException {
        return switch (name) {
            case ATTRIBUTE_NAME -> WenyanValues.of(value.getEffect().value().getDisplayName().getString());
            case ATTRIBUTE_ID -> WenyanValues.of(value.getEffect()
                    .unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse(""));
            case ATTRIBUTE_AMPLIFIER -> WenyanValues.of(value.getAmplifier());
            case ATTRIBUTE_DURATION -> WenyanValues.of(value.getDuration());
            default -> throw new WenyanException(JudouExceptionText.NoAttribute.string(name));
        };
    }

    @Override
    public WenyanType<?> type() {
        return TYPE;
    }
}
