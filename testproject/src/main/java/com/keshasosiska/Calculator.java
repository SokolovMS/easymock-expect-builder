package com.keshasosiska;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Calculator {
    public int addInts(final int a, final int b) {
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
