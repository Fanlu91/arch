# 1 新手坑

## 1.idea 没有识别注解

Enable Annotation Processing: 在 IntelliJ IDEA 中，确保启用了注解处理。你可以在 Settings 或 Preferences -> Build,
Execution, Deployment -> Compiler -> Annotation Processors 中找到这个选项。

## 2. 在初次完成provider尝试package时，报了无法找到myrpc-core myrpc-demo-api的问题，但是pom中清晰的引入了相关的dependency，idea也没有任何报错

这个问题排查了挺长时间，包括扔了一大堆报错给chatgpt，始终没能找到原因。
后来几乎排除了所有的原因后，找到了原因是spring-boot-maven-plugin 的坑。

- 打包时找不到主类（Main class），而实际上你并不需要一个主类的情况也有同样的出处和解决方案。

新创建module时，idea会自动在pom中创建一个spring-boot-maven-plugin，类似下面这样

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

spring-boot-maven-plugin 主要设计用来构建和打包最终的 Spring Boot 应用，而不是被设计为用在库项目或中间件模块上。
Spring Boot应用被打包成一个可执行jar（uber jar或fat jar）后，如果这个jar被另一个项目作为依赖引入，可能会遇到一些问题。
可执行jar使用了特殊的目录结构来包含它的所有依赖，这可能导致依赖解析时路径问题。具体来说，可执行jar内部的类路径加载机制与传统的Java应用不同，它使用了自定义的类加载器来从jar内部的嵌套jar加载类。

- This plugin override the normal package of the jar causing the classes and package structure to move
  under `BOOT-INF/classes`.

只有在最终打包为可执行应用的模块（通常是包含 main 方法的模块）中才应该真正是用 spring-boot-maven-plugin。

对于库模块或作为依赖被其他模块引用的模块，不应该使用这个插件，因为它们的目的是被包含在其他应用中，而不是独立运行。

可以通过在pom中添加以下配置在不希望打出可执行jar的时候禁用spring-boot-maven-plugin。当然也可以直接删除这个plugin。

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <skip>true</skip>
    </configuration>
</plugin>
```

## 3. IDEA 没有自动整理import

在IDEA中，打开File > Settings (对于Mac用户是IntelliJ IDEA > Preferences)。
进入Editor > Code Style > Java，选择Imports标签页。
在这里，你可以设置如何管理和优化imports，比如可以勾选`Optimize imports on the fly`来自动整理imports

## 4. 创建springboot项目的两种方式

一般情况下，创建一个单个的springboot项目时，在项目的pom.xml文件里，指定当前项目的parent为spring-boot-starter-parent即可

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
```

但是这种方式对于多模块项目存在一个严重问题：就是所有的子模块都必须是spring boot项目。很多时候我们的需求并不是这样。这时就需要第二种方式：**import 依赖管理**

第一步，pom.xml中增加如下配置，这段配置跟parent一样，把需要的springboot依赖都加了进来。

```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>3.2.3</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

这样操作之后，在 IDEA里已经可以编译运行，但mvn package是无法编译运行的，打出来的jar也不是加入了所有依赖的fat jar。

这时就需要第二步，添加如下配置来实现打包springboot项目。

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

这样maven运行时就知道使用spring boot的插件来打包，并做repackage处理，把依赖的jar都打到fat jar里去。

这个时候虽然可以打包了，但是我们会发现mvn package时单元测试没有执行。提示没有找到测试类。

这时需要第三步也是最后一步，手工添加单元测试插件maven-surefire-plugin，以及对应于junit5的引擎surefire-junit-platform。

```xml
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.3</version>
            <dependencies>
                <dependency>
                    <groupId>org.apache.maven.surefire</groupId>
                    <artifactId>surefire-junit-platform</artifactId>
                    <version>3.2.3</version>
                </dependency>
            </dependencies>
        </plugin>
```

加上了上面的配置，就可以正确运行了。

## 5. exclude lombok

下面这段pom配置为什么专门增加这个config？

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

Lombok 是一个在编译时用来帮助简化代码的工具，而不是运行时所需的依赖。Lombok 通过注解处理器在编译时自动生成诸如 getter、setter、构造函数等代码。这意味着一旦代码被编译，Lombok 的工作就已经完成了，生成的字节码中不再需要 Lombok。

这样exclue能够**减少最终包大小**，**避免运行时错误**，**清晰的依赖管理**。

当然通常来说，为 Lombok 设置 `provided` 作用域是更简洁和推荐的做法，因为它通过 Maven 的标准机制自动处理依赖的包含情况，简化了配置并避免了可能的错误或遗漏。

```xml
<scope>provided</scope>
```



# 2 知识点

## @PostConstruct 注解

Person:

- 介绍下jakarta.annotation.PostConstruct，它有哪些主要用途

ChatGPT:

- `jakarta.annotation.PostConstruct` 是 Java EE 5
  引入的一个注解，用于在依赖项注入完成之后，但在类的任何方法使用前，执行初始化方法。这个注解最初在 `javax.annotation` 包下，但从
  Jakarta EE 8 开始，随着 Java EE 转移到了 Eclipse Foundation 并更名为 Jakarta EE，相应地，`PostConstruct`
  注解也被移到了 `jakarta.annotation` 包下。

### 主要用途

1. **资源初始化** ：最常见的用途是进行资源初始化，如启动时加载配置文件、初始化数据库连接池等。这确保了在类的其他方法被调用前，必要的资源已经准备就绪。
2. **验证配置** ：可以用来验证依赖注入是否正确，例如检查是否所有必要的服务都已被注入，或者检查配置参数是否有效。
3. **启动后执行逻辑** ：除了资源初始化外，`PostConstruct` 还常用于在对象创建并注入所有依赖之后，执行一些启动后的逻辑，比如启动后自动运行的任务。
4. **减少对构造函数的复杂逻辑** ：通过将初始化逻辑从构造函数中分离出来，可以使构造函数保持简洁，专注于对象的构造，而不是其它初始化任务。
5. **跨平台的一致性** ：随着 `PostConstruct` 注解的引入，Java EE 提供了一种标准化的方法来处理在构造对象后立即需要执行的初始化代码，提高了不同
   Java EE 应用之间的一致性。

### 使用示例

```java
import jakarta.annotation.PostConstruct;

import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup // 让这个 bean 在应用启动时就被创建和初始化
public class StartupBean {

    @PostConstruct
    public void init() {
        // 执行初始化逻辑，比如加载配置文件
        System.out.println("系统启动，执行初始化任务...");
    }
}
```

在上面的示例中，`StartupBean` 类使用了 `@Singleton` 和 `@Startup` 注解，这意味着它会在应用启动时被创建。`@PostConstruct`
注解的 `init` 方法会在类的实例化及依赖注入完成后自动调用，这里可以放置初始化代码。

Person:

- 依赖项注入完成之后 怎么理解，如果直接new一个对象是用，PostConstruct会生效吗

ChatGPT:

- 依赖项注入（Dependency Injection，DI）是一种允许对象在不需要知道如何构造其依赖项的情况下接收它们的设计模式。在 Java EE 或
  Spring 等依赖注入容器中，当一个对象被创建并管理时，这些容器负责注入对象所依赖的其他对象。依赖项注入完成之后，意味着所有标记为需要被注入的属性或者方法已经被容器处理，相应的依赖项都已经赋值完毕。

对于 `@PostConstruct` 注解的方法来说，它被调用的时机是在依赖注入完成之后，即所有需要注入的属性都已经被注入值之后。这个特性确保了在执行初始化代码之前，所有必要的依赖项都已经就绪，使得初始化代码可以安全地使用这些依赖项。

### 如果直接使用 `new` 关键字创建对象

如果你直接使用 `new` 关键字创建一个对象的实例，依赖注入容器（如 Spring 容器或 Java EE 容器）并不会介入这个过程。这意味着：

- 依赖注入不会发生。容器不会自动注入属性或者调用任何标记为注入点的构造函数、方法。
- `@PostConstruct` 注解的方法不会被自动调用。因为容器不管理通过 `new`
  关键字创建的对象，所以它也就不会处理 `@PostConstruct` 注解。

简而言之，如果你直接使用 `new` 创建对象实例，`@PostConstruct` 和其他依赖注入相关的注解（如 `@Autowired` 在 Spring
中）不会生效。这些特性需要容器的支持才能工作，而通过 `new` 创建的对象实例不由容器管理。

要让 `@PostConstruct` 生效，对象需要通过容器进行管理和创建。在 Spring 应用中，这通常通过组件扫描（标记为 `@Component`
或其它派生注解如 `@Service`、`@Repository`）实现，或者通过配置类明确声明 bean。在 Java EE
应用中，这通常通过标记为 `@Stateless`、`@Singleton` 等 EJB 注解，或者在 CDI（Contexts and Dependency Injection）中使用类似的机制。

## Pattern Matching

Pattern matching involves testing whether an object has a particular structure, then extracting data from that object if
there's a match.

```java
if(rpcResponse.isStatus()){
    Object data = rpcResponse.getData();
    if(data instanceof JSONObject jsonResult) {
        return jsonResult.toJavaObject(method.getReturnType());  // 1
    }else if(data instanceof JSONArray jsonArray){
        return jsonArray.toJavaObject(method.getReturnType());  // 2
    }else {
        return cast(data,method.getReturnType());
    }
}
```

data instanceof JSONObject jsonResult 和 data instanceof JSONArray jsonArray 检查data对象是否是特定类型（JSONObject 或
JSONArray），
**并且如果是**，就将该对象自动转换为相应的类型变量（jsonResult 或 jsonArray）。这种自动转换是Java 14引入的模式匹配（Pattern
Matching）for instanceof的特性的一个例子，它简化了代码并减少了需要进行显式类型转换的地方。
所以注释中的 1 和 2 两块代码并非等价，前者处理的是JSONObject类型的数据，后者处理的是JSONArray类型的数据。

- https://docs.oracle.com/en/java/javase/17/language/pattern-matching.html

## router 和 loadbalance 在选址上的区别

router 在springCloud中是没有的，dubbo有这个设计。
loadbalance 可以实现router的功能，但是不符合单一职责原则。

router 是根据一定规则（非负载角度的），进行服务的路由，比如根据tag等。

## 不同负载均衡算法

在规模和并发不大的情况下，性能角度 RR 和复杂算法没有区别。
只有在规模和并发大的情况下，复杂算法才能体现出优势。

## SpringApplication.exit(context, () -> 0)

```java
@PreDestroy
void destroy() {
    SpringApplication.exit(context, () -> 0);
}
```

- **`SpringApplication.exit(context, () -> 0);`**：这行代码的功能是优雅地关闭 Spring 应用上下文。`SpringApplication.exit` 方法接收两个参数：
  
  - **`context`**：这是你想要关闭的 `ConfigurableApplicationContext` 对象，即 Spring 应用的上下文。
  - **`() -> 0`**：这是一个 Java Lambda 表达式，它实现了 `org.springframework.boot.ExitCodeGenerator` 接口。这个接口用于在应用退出时提供一个退出代码。在这个例子中，它总是返回 `0`，通常表示程序正常退出。



## provider节点故障处理

1. 有节点宕机时，通过多个provider集群+注册中心，可以保障整体的可用

2. 有节点偶尔异常但没有宕机，可以通过重试+LB重选节点，实现这次调用

3. 有节点在一段时间内异常（这个实例上有很多服务，其中个别服务慢），没宕机，其他服务一直是好的，把这些节点识别出来做到故障隔离
   
   - 通过探活再做故障恢复，full open
   
   - 每次定时探活，放一部分流量，half open
   
   

# 3 工具

## wrk

```less
wrk "http://localhost:9088/?id=101"
Running 10s test @ http://localhost:9088/\?id\=101
  2 threads and 10 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   136.80ms  402.64ms   1.99s    90.00%
    Req/Sec     5.36k     2.25k    7.39k    83.33%
  16352 requests in 10.01s, 9.20MB read
  Non-2xx or 3xx responses: 16352
Requests/sec:   1633.78
Transfer/sec:      0.92MB


wrk "http://localhost:9088/?id=101" -d3s
```

## arthas

[命令列表 | arthas](https://arthas.aliyun.com/doc/commands.html)

```less
➜  arthas-bin java -jar arthas-boot.jar

dashboard

sc com.flhai.myrpc.*
trace com.flhai.myrpc.core.consumer.MyInvocationHandler invoke
```



## springboot log

### `@Slf4j`

`@Slf4j` 是一个 Lombok 提供的注解，用于在 Java 类中自动注入一个日志对象（即 `Logger`）。即一般情况下在为类加上@Slf4j，就可以直接使用Log及其方法了，非常方便。

```java
@Slf4j
public class MyInvocationHandler implements InvocationHandler {
    public static void main(String[] args) {
        log.debug("hello");
    }
```

这个注解背后的 SLF4J（Simple Logging Facade for Java）是一个日志门面，它提供了一个 Java 的日志 API。SLF4J 允许开发者在代码中使用统一的日志 API，而不依赖于具体的日志实现框架，从而使得可以在不同的日志框架之间（如 Logback、Log4J）切换而不需要修改主代码。

### logging

在 `application.yml` 或 `application.properties` 文件中的 `logging` 配置是 Spring Boot 提供的一种方式，允许开发者在不直接接触底层日志框架的情况下配置日志行为。这种配置方式是对底层日志框架（如 Logback 或 Log4J2）的封装，使得配置更加简洁并且与 Spring Boot 应用的集成更加无缝。

```yaml
logging:
  level:
    root: INFO
    org.springframework: INFO
    com.flhai: DEBUG
  pattern:
    console: "%d{HH:mm:ss} %-5level - %msg%n"
```

#### pattern

上面yaml中，对日志的pattern进行了定制

- `application.yml` 中使用 `logging.pattern.console` 来设置你的自定义格式

打印出的日志会很简洁，类似这样

```less
11:28:05 DEBUG - retry = 1
```

- `%-5level`：显示日志级别，例如 `INFO`，并保留固定宽度5。
- `%msg`：显示实际的日志消息。
- `%n`：表示新行。

这种设置比较适合本地开发调试。

在 Spring Boot 中，如果没有显式设置日志格式，它会使用 Logback 的默认配置，类似这样

```less
2024-04-15T11:24:33.785+08:00  INFO 99167 --- [myrpc-demo-api] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
```

它对应的pattern是

```less
%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX}  %5p [%appName] [%thread] %c{1} : %m%n
```

- `%5p`：日志级别（如 INFO、DEBUG），这里的 `5` 表示日志级别字段至少占用五个字符，左对齐。
- `%c{1}`：记录器的名称，这里的 `{1}` 表示只显示最后一个元素的类名或记录器名。
- `[%thread]`：表示生成日志条目的线程名。
- `[app name]`：应用程序的名称，通常在日志配置中通过 `spring.application.name` 属性设置。

### Logback

当你在使用 `@Slf4j` 注解的项目中，实际上你的代码与具体的日志实现（如 Logback 或 Log4j）是解耦的。你需要查看项目的依赖管理文件（如 Maven 的 `pom.xml` 或 Gradle 的 `build.gradle`），以确定你的项目是引用了 Logback 还是 Log4j 的依赖。

Spring Boot，或者说`spring-boot-starter-web` 包括 `spring-boot-starter-logging`默认使用 Logback。

如果需要简单的配置和良好的性能，且不需要复杂的日志处理功能，Logback 是一个很好的选择。

### Log4j2

Log4j2 通常提供比 Logback 更高的性能，特别是在多线程环境中，因为 Log4j2 的设计包括了异步日志处理的支持。

两者都兼容 SLF4J，但 Log4j2 提供了更多高级配置选项，包括更复杂的过滤器和异步日志处理，自身有一个单独的 Log4j2 API，这可能在某些使用场景下提供额外的功能。

Log4j2 是较新的技术，其可能拥有更多关于现代日志处理特性（如对 **JSON 格式的天然支持**）的支持

要使用 Log4j2，你需要排除默认的 Logback 依赖，并添加对 Log4j2 的支持

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```



## 灰度发布和蓝绿发布

灰度发布（Canary Release）和蓝绿发布（Blue-Green Deployment）都是现代软件开发中用于减少部署风险的技术，但它们的实施方式和目的有所不同。

1. **蓝绿发布**：
   
   - 蓝绿发布是通过两套完全相同的生产环境（一套蓝色，一套绿色）来进行的。在一套环境中部署新版本的软件，在另一套环境中运行旧版本。
   - 当新版本部署并经过足够测试无误后，流量会从旧版本（比如蓝色环境）切换到新版本（绿色环境）。如果新版本出现问题，可以快速回滚到旧版本，只需切换流量即可。
   - 这种部署方式的优点是切换快速、风险较低，但成本较高，因为需要维护两套几乎相同的生产环境。

2. **灰度发布**：
   
   - 灰度发布通常是逐步将新版本引入到生产环境中的一部分用户上，开始时只有小部分用户（比如5%的流量）接触到新版本。
   - 如果这部分用户的反馈良好，不出现重大问题，会逐渐扩大范围，最终全部用户都会使用新版本。
   - 灰度发布的优点是风险控制更细致，可以根据用户反馈逐步优化或修正问题，但缺点是可能需要较长时间才能完全部署新版本，且在部署过程中需要处理多版本并存的复杂性。



# 4 问题暂存

[kkrpc-core/src/main/java/cn/kimmking/kkrpc/core/consumer/KKInvocationHandler.java · ArchCamp/kkrpc - Gitee.com](https://gitee.com/ArchCamp/kkrpc/blob/V09/kkrpc-core/src/main/java/cn/kimmking/kkrpc/core/consumer/KKInvocationHandler.java)

```java
                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                        log.debug("instance {} is recovered, isolatedProviders={}, providers={}", instance, isolatedProviders, providers);
                    }
                }
```

这里第一次隔离之后就又恢复了。没有起效呀