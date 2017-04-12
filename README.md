# Easymock expect builder

Plugin will help to create Builder which mocks each method of source class.  

See [plugin page](https://plugins.jetbrains.com/plugin/9586-easymock-expect-builder) in JetBrains repo.

## Generate expect builder
1. **"Alt + insert"** inside of your source class
1. Navigate to **"Easymock expect builder"**
1. Choose appropriate methods
1. OK
1. Check test directory for generated builder

## How to use expect builder
```java
new YourClassExpectBuilder()
    .method1(arg11, ..., argK, expected1)
    ...
    .methodN(argN1, ..., argL, expectedN)
    .buildAndReplay();
```

## Examples
### 1
Before
```
public int addInts(final int a, final int b) {
    return a + b;
}
```
After
```
public ExpectedCalculatorMockBuilder addInts(final int a, final int b, final int expected) {
    EasyMock.expect(mock.addInts(a, b)).andReturn(expected).once();
    return this;
}
```
### 2
Before
```
public void voidMethod(final double a, final PublicClass b) {
    throw new NotImplementedException();
}
```
After
```
public ExpectedCalculatorMockBuilder voidMethod(final double a, final PublicClass b) {
    mock.voidMethod(a, b);
    EasyMock.expectLastCall().once();
    return this;
}
```
## Known issues
1. Most of checks (like null checks) are not done.
## Developer statements regarding plugin
1. Plugin was done for personal purposes
1. Plugin has a lot of things to improve
