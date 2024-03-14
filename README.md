


# 记录一些坑

## 新手坑
1.idea 环境问题处理，没有识别注解

Enable Annotation Processing: 在 IntelliJ IDEA 中，确保启用了注解处理。你可以在 Settings 或 Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors 中找到这个选项。

2. 在初次完成provider尝试package时，报了无法找到myrpc-core myrpc-demo-api的问题，但是pom中清晰的引入了相关的dependency，idea也没有任何报错

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
- This plugin override the normal package of the jar causing the classes and package structure to move under `BOOT-INF/classes`.


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

3. IDEA 没有自动整理import

在IDEA中，打开File > Settings (对于Mac用户是IntelliJ IDEA > Preferences)。
进入Editor > Code Style > Java，选择Imports标签页。
在这里，你可以设置如何管理和优化imports，比如可以勾选`Optimize imports on the fly`来自动整理imports
