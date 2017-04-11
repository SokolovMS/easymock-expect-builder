package com.keshasosiska;

public interface Calculator {
    int addInts(final int a, final int b) throws InterruptedException;

    String addStrings(final String a, final String b);

    PublicClass addClasses(final PublicClass a, final PublicClass b);

    void voidMethod(final double a, final PublicClass b);
}
