# Easymock expect builder

Plugin will help to create Builder which mocks each method of source class.  
All code regarding mocking of current class is placed under one testBuilderClass.

See [plugin page](https://plugins.jetbrains.com/plugin/9586-easymock-expect-builder) in JetBrains repo.

## Generate expect builder
1. **"Alt + insert"** inside of your source class
1. Navigate to **"Easymock expect builder"**
1. Choose appropriate methods
1. OK
1. Check test directory for generated builder

## How to use expect builder
``` java
new YourClassExpectBuilder()
    .method1(arg11, ..., argK, expected1)
    ...
    .methodN(argN1, ..., argL, expectedN)
    .buildAndReplay();
```

## Example of usage
### Before
``` java
// Used in one place
private YourClass mockYourClassForTest1() {
    YourClass mock = EasyMock.mock(YourClass.class);
    EasyMock.expect(mock.method1(specificArg1)).andReturn(expected1).once();
    EasyMock.expect(mock.method2(specificArg2)).andReturn(expected2).once();
    EasyMock.replay(mock);
    return mock;
}

// Used in one place
private YourClass mockYourClassForTest2() {
    YourClass mock = EasyMock.mock(YourClass.class);
    EasyMock.expect(mock.method1(specificArg1)).andReturn(expected1).once();
    EasyMock.expect(mock.method2(specificArg2)).andReturn(expected2).once();
    EasyMock.expect(mock.method3(specificArg3)).andReturn(expected3).once();
    EasyMock.replay(mock);
    return mock;
}

// Or other combination of methods from YourClass.

@Test
public void test1() {
    YourClass myClass = mockYourClassForTest1();
    ...
}

@Test
public void test2() {
    YourClass myClass = mockYourClassForTest2();
    ...
}
```
### After
``` java
@Test
public void test1() {
    YourClass myClass = new YourClassMockBuilder()
            .method1(specificArg1, expected1)
            .method2(specificArg2, expected2)
            .buildAndReplay();
    ...
}

@Test
public void test2() {
    YourClass myClass = new YourClassMockBuilder()
            .method1(specificArg1, expected1)
            .method2(specificArg2, expected2)
            .method3(specificArg3, expected3)
            .buildAndReplay();
    ...
}
```
## Examples of translations
### 1
Source method
``` java
public int addInts(final int a, final int b) {
    return a + b;
}
```
Expect builder method
``` java
public ExpectedCalculatorMockBuilder addInts(final int a, final int b, final int expected) {
    EasyMock.expect(mock.addInts(a, b)).andReturn(expected).once();
    return this;
}
```
### 2
Source method
``` java
public void voidMethod(final double a, final PublicClass b) {
    throw new NotImplementedException();
}
```
Expect builder method
``` java
public ExpectedCalculatorMockBuilder voidMethod(final double a, final PublicClass b) {
    mock.voidMethod(a, b);
    EasyMock.expectLastCall().once();
    return this;
}
```
## Known issues
1. Most of checks (like null checks) are not done.
1. After the second usage on the same class YourClassMockBuilder will be overwritten.
## Developer statements regarding plugin
1. Plugin was done for personal purposes
1. Plugin has a lot of things to improve
