# Spring Bean Lifecycle — Project Notes

## 1. What this project demonstrates

This Spring Boot application prints messages from lifecycle callbacks so that you can observe initialization, bean post-processing, and cleanup.

The main example is [`Employee.java`](src/main/java/com/durga/spring_bean_life_cycle/Employee.java). It is annotated with `@Component` and implements four interfaces:

```java
public class Employee implements InitializingBean, DisposableBean,
        BeanNameAware, BeanPostProcessor {
    // Lifecycle callbacks
}
```

The project declares Spring Boot `4.1.1` and Java `17` in `pom.xml`. Its annotations use `jakarta.annotation.PostConstruct` and `jakarta.annotation.PreDestroy`.

## 2. The callbacks in Employee

| Mechanism | Project method | Purpose in this example |
| --- | --- | --- |
| `BeanNameAware` | `setBeanName(String name)` | Prints the name assigned to this bean: `employee`. |
| `@PostConstruct` | `intialize()` | Prints the annotation-based initialization message. |
| `InitializingBean` | `afterPropertiesSet()` | Prints the interface-based initialization message. |
| `BeanPostProcessor` | `postProcessBeforeInitialization(Object bean, String beanName)` | Logs a bean passing through the before-initialization phase. |
| `BeanPostProcessor` | `postProcessAfterInitialization(Object bean, String beanName)` | Logs a bean passing through the after-initialization phase. |
| `@PreDestroy` | `cleanUp()` | Prints the annotation-based cleanup message. |
| `DisposableBean` | `destroy()` | Prints the interface-based destruction message. |

The spelling `intialize()` matches the current source. The annotation identifies the callback, so its name does not need to be `initialize`.

The comments suggest caching and stopping jobs as possible future work. The current implementation only prints messages; it does not create a cache, schedule jobs, or release external resources.

## 3. Lifecycle order

For a normally created Spring bean, the simplified sequence is:

```text
Construct instance
    |
Populate dependencies/properties
    |
Aware callbacks, including setBeanName
    |
Before-initialization processor chain
    |-- @PostConstruct is invoked by an annotation processor here
    |
InitializingBean.afterPropertiesSet
    |
Configured custom init method, if present
    |
After-initialization processor chain
    |
Bean available for use
    |
Context closes normally
    |
@PreDestroy
    |
DisposableBean.destroy
    |
Configured custom destroy method, if present
```

For distinct initialization methods, `@PostConstruct` precedes `afterPropertiesSet()`, which precedes a configured custom init method. The corresponding destruction order is `@PreDestroy`, `destroy()`, then a configured custom destroy method. This project does not configure custom init or destroy methods. See [Spring lifecycle callbacks](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html).

Do not assume every custom `postProcessBeforeInitialization()` runs before `@PostConstruct`: annotation processing happens inside that processor chain, so processor ordering matters.

## 4. BeanPostProcessor implementation

The project adds these two methods to `Employee`:

```java
@Override
public Object postProcessBeforeInitialization(Object bean, String beanName) {
    System.out.println(">>> postProcessBeforeInitialization executed for bean: "
            + beanName + " <<<<<<");
    return bean;
}

@Override
public Object postProcessAfterInitialization(Object bean, String beanName) {
    System.out.println(">>> postProcessAfterInitialization executed for bean: "
            + beanName + " <<<<<<");
    return bean;
}
```

### Parameters and return value

- `bean` is the object currently being processed.
- `beanName` identifies that object in the container.
- Returning `bean` preserves the object and allows processing to continue.
- A processor can return a replacement, such as a compatible proxy.
- Returning `null` stops the remaining processors in that phase; it is not a request to delete the bean.

The before callback runs after property population and before initialization methods such as `afterPropertiesSet()`. The after callback runs after initialization. These callbacks do not run before and after every business-method invocation. See the [BeanPostProcessor API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/BeanPostProcessor.html).

### What this implementation does

Both methods log the callback name and the supplied bean name. Neither changes the object. No cast to `Employee` is needed, and such a cast would fail for other bean types passed to these methods.

`setBeanName(name)` and the `beanName` parameter have different roles in this class: the former receives Employee's own name; the latter describes whichever bean Employee is currently processing.

## 5. Why Employee logs other beans

`Employee` has two roles: a component with its own lifecycle, and a container extension that observes other beans. Spring detects processor beans and registers them early. The processor applies to eligible beans created afterward in the same container, including framework beans. A `BeanFactoryPostProcessor`, by comparison, works on bean-definition metadata. See [Spring container extension points](https://docs.spring.io/spring-framework/reference/core/beans/factory-extension.html).

In this project, Employee is initialized before its own processor instance is registered. Consequently, you should not expect its own methods to print:

```text
postProcessBeforeInitialization executed for bean: employee
postProcessAfterInitialization executed for bean: employee
```

Its own initialization messages still appear. This explains the observed test output:

```text
Bean Name : employee
>>> @PostConstruct executed <<<<<<
>>> afterPropertiesSet executed <<<<<<
```

Later output includes pairs like this excerpt from the project test run:

```text
>>> postProcessBeforeInitialization executed for bean: characterEncodingFilter <<<<<<
>>> postProcessAfterInitialization executed for bean: characterEncodingFilter <<<<<<
```

Bean names and output volume depend on configuration. The excerpt is not the complete log, and messages for different beans may interleave during dependency creation.

For a clearer learning exercise, a future refactor could move the two processor methods into a separate `LifecycleLoggingBeanPostProcessor` component and remove `BeanPostProcessor` from Employee. That would allow the registered processor to observe Employee's initialization. This note describes a possible extension; the current code keeps both roles in Employee.

## 6. Initialization versus cleanup

Use initialization callbacks for work that requires injected dependencies. Use cleanup callbacks to release resources owned by the bean. Avoid treating initialization as proof that the entire application is ready: other beans may still be starting.

For the current Employee, a normal context shutdown should produce:

```text
>>> @PreDestroy executed <<<<<<
>>> destroy executed <<<<<<
```

These messages represent two distinct callbacks; if real cleanup is added to both, avoid releasing the same resource twice. A forced process termination can prevent cleanup callbacks from executing. Spring does not automatically invoke destruction callbacks for prototype beans. See [Spring lifecycle documentation](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html).

## 7. Run and inspect the example

From the project root in PowerShell:

```powershell
# Start the application and observe initialization output.
.\mvnw.cmd spring-boot:run

# Run the existing context-loading test.
.\mvnw.cmd test
```

Run these commands separately: the application command remains running until stopped. Use a normal shutdown, such as Ctrl+C, to observe cleanup; avoid forcibly killing the process when studying destruction callbacks.

The previous validation of the callback implementation completed with `BUILD SUCCESS`: one test ran, with zero failures and zero errors. That run used Java `26.0.2.1`, while compilation targeted Java `17` as configured by the project.

The existing `contextLoads()` test verifies that the application context starts. It does not explicitly assert callback order, returned-object identity, or shutdown output. Console output provides a useful demonstration but is not an automated assertion of all lifecycle behavior.

## 8. Questions to check your understanding

**Why did implementing BeanPostProcessor previously compile without these methods?**

The interface supplies default implementations. Overriding the methods adds the project's logging behavior.

**Does `afterPropertiesSet()` require setter methods in Employee?**

No. It is a lifecycle callback; this Employee has no injected fields or setters and still receives it.

**Why are there many log lines when Employee is the only custom component?**

The processor logs eligible framework beans as well as application beans. It has no filtering condition.

**Does `postProcessAfterInitialization()` mean that the application has shut down?**

No. It belongs to startup initialization. `cleanUp()` and `destroy()` demonstrate shutdown callbacks.

**Does directly calling `new Employee()` reproduce the log sequence?**

No. Direct construction does not ask Spring to manage the instance, so these container callbacks are not automatically invoked.

**Why keep `return bean` in both methods?**

The example observes lifecycle activity without replacing the processed object. Returning that object preserves this behavior throughout the processing chain.
