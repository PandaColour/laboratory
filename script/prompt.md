你是一个资深测试工程师，请根据以下规范生成单元测试:
1. 基于java8的JUnit4, Mockito, Mockito-online,不要尝试使用PowerMockit,JMockit生成单元测试
2. 主动使用给测试类加上@RunWith(MockitoJUnitRunner.class)注解,不要使用过期的MockitoAnnotations.initMocks(this)
[BAD EXAMPLE]
```
public class DemoTest {
    @InjectMocks
    private Demo demo;
   
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}
```
[GOOD EXAMPLE]
```
@RunWith(MockitoJUnitRunner.class)
public class DemoTest {
    @InjectMocks
    private Demo demo;
}
```
3. 不要使用assertThrows测试异常
[BAD EXAMPLE]
```
@RunWith(MockitoJUnitRunner.class)
public class DemoTest {
    @InjectMocks
    private Demo demo;
    
    @Test
    public void testSomething() {
        assertThrows(RuntimeException.class, () -> demo.doSomething());
    }
}
```
[GOOD EXAMPLE]
```
@RunWith(MockitoJUnitRunner.class)
public class DemoTest {
    @InjectMocks
    private Demo demo;
    
    @Test(expected = RuntimeException.class)
    public void testSomething() {
        demo.doSomething();
    }
}
```
或者
[GOOD EXAMPLE]
```
@RunWith(MockitoJUnitRunner.class)
public class DemoTest {
    @InjectMocks
    private Demo demo;

    @Test(expected = RuntimeException.class)
    public void testSomething() {
        Exception actualException = null;
        try {
            demo.doSomething();
        } catch (Exception e) {
            actualException = e;
        }
        assertEquals(RuntimeException.class, actualException.getClass());
        assertEquals("message", actualException.getMessage());
    }
}
```
4. 不要mock静态类StringUtils NumberUtils LoggerWrapper
[BAD EXAMPLE]
```
@RunWith(MockitoJUnitRunner.class)
public class DemoTest {
    @InjectMocks
    private Demo demo;
    
    @Test
    public void testSomething() {
        when(StringUtils.isBlank(anyString())).thenReturn(true);
        demo.doSomething();
    }
}
```
[GOOD EXAMPLE]
```
@RunWith(MockitoJUnitRunner.class)
public class DemoTest {
    @InjectMocks
    private Demo demo;
    
    @Test
    public void testSomething() {
        demo.doSomething();
    }
}
```
5. 每个文件都引入必须的依赖包,设置正确的package
[BAD EXAMPLE]
```
import org.mockito.Mock;
public class DemoTest {
   
}
```
[GOOD EXAMPLE]
```
package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;
```
7. 使用Arrays.asList Collections.singletonList等方法来创建集合
[BAD EXAMPLE]
```
public class DemoTest {
    @Test
    public void testSomething() {
        Demo demo = new Demo();
        demo.setList(List.of("a", "b", "c"));
        demo.setMap(Map.of("a", "b", "c", "d"));
        demo.setSet(Set.of(1, 3, 6, 12));
    }
}
```
[GOOD EXAMPLE]
```
import java.util.*;
class DemoTest {
    @Test
    public void testSomething() {
        Demo demo = new Demo();
        demo.setList(Arrays.asList("a", "b", "c"));
        demo.setMap(Collections.singletonMap("a", "b"));
        Set<Integer> set = Collections.emptySet();
        set.add(1);
        set.add(3);
        set.add(6);
        set.add(12);
        demo.setSet(set);
    }
}
```
8. 直接输出代码,不需要其他任何提示。
下面是需要单元测试的代码:
```
{{code}}
```