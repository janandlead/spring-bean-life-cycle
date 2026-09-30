# Spring Bean Lifecycle

A Spring Boot learning project that logs bean initialization, post-processing, and destruction callbacks. Run the application and follow the console output to see the lifecycle in action.

For detailed explanations, lifecycle ordering, sample output, and common questions, read [Bean Lifecycle Notes](BEAN_LIFECYCLE_NOTES.md).

## Technology

- Spring Boot `4.1.1`
- Java `17` compilation target
- Maven Wrapper
- Spring Web MVC
- JUnit and Spring Boot testing support

## Lifecycle callbacks demonstrated

The [`Employee`](src/main/java/com/durga/spring_bean_life_cycle/Employee.java) component implements the following callbacks:

| Mechanism | Method | Demonstration |
| --- | --- | --- |
| `BeanNameAware` | `setBeanName()` | Logs Employee's bean name. |
| `@PostConstruct` | `intialize()` | Logs annotation-based initialization. |
| `InitializingBean` | `afterPropertiesSet()` | Logs interface-based initialization. |
| `BeanPostProcessor` | `postProcessBeforeInitialization()` | Logs beans passing through the before-initialization phase. |
| `BeanPostProcessor` | `postProcessAfterInitialization()` | Logs beans passing through the after-initialization phase. |
| `@PreDestroy` | `cleanUp()` | Logs annotation-based cleanup. |
| `DisposableBean` | `destroy()` | Logs interface-based destruction. |

Both post-processor methods return the original bean. The example logs lifecycle activity; it does not modify beans or allocate external resources.

Employee also acts as the post-processor, so its callbacks log other eligible beans, including framework beans. It is initialized before its own processor is registered and therefore does not log its own initialization through those two methods. See the [detailed notes](BEAN_LIFECYCLE_NOTES.md#5-why-employee-logs-other-beans) for an explanation.

## Run locally

Install a JDK compatible with the project's Java 17 target and configure `JAVA_HOME`. The Maven Wrapper downloads Maven and dependencies as needed, so a separate Maven installation is unnecessary.

From the project root, run:

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS or Linux

```bash
sh ./mvnw spring-boot:run
```

Watch the console for messages such as:

```text
Bean Name : employee
>>> @PostConstruct executed <<<<<<
>>> afterPropertiesSet executed <<<<<<
```

Other beans produce messages such as:

```text
>>> postProcessBeforeInitialization executed for bean: characterEncodingFilter <<<<<<
>>> postProcessAfterInitialization executed for bean: characterEncodingFilter <<<<<<
```

These are excerpts, not the full output. Stop the application normally with Ctrl+C to observe cleanup:

```text
>>> @PreDestroy executed <<<<<<
>>> destroy executed <<<<<<
```

This example is observed through console logs; it does not define a custom HTTP endpoint.

## Run tests

On Windows:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
sh ./mvnw test
```

The existing `contextLoads()` test checks that the Spring application context starts. It does not explicitly assert lifecycle callback order or shutdown messages.

## Project files

```text
src/
  main/
    java/com/durga/spring_bean_life_cycle/
      SpringBeanLifeCycleApplication.java
      Employee.java
    resources/
      application.properties
  test/java/com/durga/spring_bean_life_cycle/
    SpringBeanLifeCycleApplicationTests.java
pom.xml
README.md
BEAN_LIFECYCLE_NOTES.md
```

- [Application entry point](src/main/java/com/durga/spring_bean_life_cycle/SpringBeanLifeCycleApplication.java)
- [Lifecycle implementation](src/main/java/com/durga/spring_bean_life_cycle/Employee.java)
- [Context-loading test](src/test/java/com/durga/spring_bean_life_cycle/SpringBeanLifeCycleApplicationTests.java)
- [Detailed study notes](BEAN_LIFECYCLE_NOTES.md)
