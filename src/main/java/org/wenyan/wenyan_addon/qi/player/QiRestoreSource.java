package org.wenyan.wenyan_addon.qi.player;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 灵气恢复源：装备/饰品实现此接口，附加给玩家的环境五行恢复属性。
 * 玩家初始无恢复源时，环境灵气只能恢复无属性。
 */
public interface QiRestoreSource {
    /**
     * 该装备能让玩家从环境恢复的属性。
     */
    List<ElementAttribute> restoreElements();
}
