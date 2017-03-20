package com.keshasosiska;

import org.easymock.EasyMock;

import static org.easymock.EasyMock.replay;

public class ExpectedCalculatorMockBuilder {
    private Calculator mock;

    public ExpectedCalculatorMockBuilder() {
        mock = EasyMock.mock(Calculator.class);
    }

    public Calculator buildAndReplay() {
        replay(mock);
        return mock;
    }
}
