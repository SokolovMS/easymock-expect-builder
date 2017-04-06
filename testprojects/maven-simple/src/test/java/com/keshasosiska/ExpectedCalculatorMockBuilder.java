package com.keshasosiska;

import org.easymock.EasyMock;

public class ExpectedCalculatorMockBuilder {
    private Calculator mock;

    public ExpectedCalculatorMockBuilder() {
        mock = EasyMock.mock(Calculator.class);
    }

    public ExpectedCalculatorMockBuilder addInts(final int a, final int b, final int expected) {
        EasyMock.expect(mock.addInts(a, b)).andReturn(expected).once();
        return this;
    }

    public ExpectedCalculatorMockBuilder addStrings(final String a, final String b, final String expected) {
        EasyMock.expect(mock.addStrings(a, b)).andReturn(expected).once();
        return this;
    }

    public ExpectedCalculatorMockBuilder addClasses(final PublicClass a, final PublicClass b, final PublicClass expected) {
        EasyMock.expect(mock.addClasses(a, b)).andReturn(expected).once();
        return this;
    }

    public ExpectedCalculatorMockBuilder voidMethod(final double a, final PublicClass b) {
        mock.voidMethod(a, b);
        EasyMock.expectLastCall().once();
        return this;
    }

    public Calculator buildAndReplay() {
        EasyMock.replay(mock);
        return mock;
    }
}
