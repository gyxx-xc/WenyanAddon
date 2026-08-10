package org.wenyan.wenyan_addon.value;

import indi.wenyan.judou.api.WenyanType;
import indi.wenyan.judou.api.language.JudouExceptionText;
import indi.wenyan.judou.api.values.IWenyanObject;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;
import org.wenyan.wenyan_addon.qi.element.ElementType;

public record WenyanElement(ElementType value) implements IWenyanObject {
    public static final WenyanType<WenyanElement> TYPE = new WenyanType<>("元素", WenyanElement.class);

    @Override
    public IWenyanValue getAttribute(String name) throws WenyanException {
        throw new WenyanException(JudouExceptionText.NoAttribute.string(name));
    }

    @Override
    public WenyanType<?> type() {
        return TYPE;
    }
}
