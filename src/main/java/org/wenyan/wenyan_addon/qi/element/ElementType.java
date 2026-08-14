package org.wenyan.wenyan_addon.qi.element;

import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.List;

/**
 * 五行 / 阴阳 / 无属性（基底属性），实现 {@link ElementAttribute}；
 * 衍生属性通过 {@link ElementRegistry} 运行时注册。
 */
public enum ElementType implements ElementAttribute {
    METAL("金", 0xFFFFFFFF),
    WOOD("木", 0xFF00FF00),
    WATER("水", 0xFF000000),
    FIRE("火", 0xFFFF0000),
    EARTH("土", 0xFFFFFF00),
    YIN("阴", 0xFF7B5FD3),
    YANG("阳", 0xFFFFD75F),
    NEUTRAL("无属性", 0xFF9AA5B1) {
        @Override
        public ElementCoefficients defaultCoefficients() {
            return ElementCoefficients.DEFAULT
                    .withExtra("environmentMainRatio",1.0)
                    .withExtra("environmentSubRatio",0.0)
                    .withExtra("environmentGainBase",0.0);
        }
    };

    private final String displayName;
    private final int color;

    ElementType(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    @Override
    public String id() {
        return name().toLowerCase();
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public List<ElementType> bases() {
        return List.of(this);
    }

    @Override
    public int color() {
        return color;
    }
}
