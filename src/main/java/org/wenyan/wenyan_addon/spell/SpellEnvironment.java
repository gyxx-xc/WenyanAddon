package org.wenyan.wenyan_addon.spell;

import indi.wenyan.judou.api.compile.IWenyanBytecode;

import java.util.Map;

/**
 * 法术运行环境：编译好的字节码 + 背包符咒拓展包扫描结果。
 * scrollPackages：拓展包内符咒代码（包名 → 代码，编译后作为环境函数）。
 * devicePackages：拓展包内符咒石等文言设备（包名 → 设备条目，提供函数包）。
 */
public record SpellEnvironment(
        IWenyanBytecode bytecode,
        Map<String, String> scrollPackages,
        Map<String, SpellEnvironmentScanner.DeviceEntry> devicePackages) {
}