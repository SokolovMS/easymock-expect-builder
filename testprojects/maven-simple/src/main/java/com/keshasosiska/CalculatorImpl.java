package com.keshasosiska;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CalculatorImpl implements Calculator {
    public String name;

    public CalculatorImpl(final String name) {
        this.name = name;
    }

    public int addInts(final int a, final int b) throws InterruptedException {
        Thread.sleep(1);
        return a + b;
    }

    public String addStrings(final String a, final String b) {
        return a + b;
    }

    public PublicClass addClasses(final PublicClass a, final PublicClass b) {
        return new PublicClass();
    }

    public void voidMethod(final double a, final PublicClass b) {
        throw new NotImplementedException();
    }
}
