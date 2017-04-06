package com.keshasosiska;

import org.easymock.EasyMock;

import static org.easymock.EasyMock.replay;

public class ExpectedCalculatorImplMockBuilder {
    private Calculator mock;

    public ExpectedCalculatorImplMockBuilder() {
        mock = EasyMock.mock(Calculator.class);
    }

    public ExpectedCalculatorImplMockBuilder addInts(final int a, final int b, final int expected) {
        EasyMock.expect(mock.addInts(a, b)).andReturn(expected).once();
        return this;
    }

    public ExpectedCalculatorImplMockBuilder addStrings(final String a, final String b, final String expected) {
        EasyMock.expect(mock.addStrings(a, b)).andReturn(expected).once();
        return this;
    }

    public ExpectedCalculatorImplMockBuilder addClasses(final PublicClass a, final PublicClass b, final PublicClass expected) {
        EasyMock.expect(mock.addClasses(a, b)).andReturn(expected).once();
        return this;
    }

    public ExpectedCalculatorImplMockBuilder voidMethod(final double a, final PublicClass b) {
        mock.voidMethod(a, b);
        EasyMock.expectLastCall().once();
        return this;
    }

    public Calculator buildAndReplay() {
        replay(mock);
        return mock;
    }
}
