package org.wenyan.wenyan_addon.qi.element;

public enum ElementType {
    METAL("金"),
    WOOD("木"),
    WATER("水"),
    FIRE("火"),
    EARTH("土"),
    NEUTRAL("无属性");

    private final String displayName;

    ElementType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
