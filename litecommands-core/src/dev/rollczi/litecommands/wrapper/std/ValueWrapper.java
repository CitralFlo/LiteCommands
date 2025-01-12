package dev.rollczi.litecommands.wrapper.std;

import dev.rollczi.litecommands.wrapper.Wrapper;
import dev.rollczi.litecommands.wrapper.WrapFormat;

import java.util.function.Supplier;

public class ValueWrapper extends AbstractWrapper<Object> implements Wrapper {

    public ValueWrapper() {
        super(Object.class);
    }

    @Override
    protected <EXPECTED> Supplier<Object> wrapValue(EXPECTED valueToWrap, WrapFormat<EXPECTED, ?> info) {
        return () -> valueToWrap;
    }

}
