package com.flhai.myrpc.demo.consumer;

import com.flhai.myrpc.core.annotation.MyConsumer;
import com.flhai.myrpc.core.api.Router;
import com.flhai.myrpc.core.api.RpcContext;
import com.flhai.myrpc.core.cluster.GreyRouter;
import com.flhai.myrpc.core.consumer.ConsumerConfig;
import com.flhai.myrpc.demo.api.User;
import com.flhai.myrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Import({ConsumerConfig.class})
@SpringBootApplication
@RestController
@Slf4j
public class MyrpcDemoConsumerApplication {

    // 如果@Autowired，就是本地调用
    // 如果@MyConsumer，就是远程调用
    //    @Autowired
    @MyConsumer
    UserService userService;

//    @MyConsumer
//    OrderService orderService;

    @RequestMapping("/")
    public User findBy(@RequestParam("id") int id) {
        // find the service
//        log.debug("findBy id = " + id);
//        long start = System.currentTimeMillis();
//        userService.timeoutFind(800);
//        System.out.println("timeoutFind cost: " + (System.currentTimeMillis() - start) + "ms");
        return userService.findById(id);
    }

    /**
     * http://localhost:9088/timeout?t=800
     *
     * @param t
     * @return
     */
    @RequestMapping("/timeout")
    public String testTimeout(@RequestParam("t") int t) {
        long start = System.currentTimeMillis();
        String service = userService.timeoutFind(t);
        return "service " + service + " time cost: " + (System.currentTimeMillis() - start) + "ms";
    }

    /**
     * todo 这里指改变了1个服务提供者的超时时间，可能真正想改的那个没有被改到
     * http://localhost:9088/timeoutPorts?ports=8081,8094
     * 修改模拟超时端口
     *
     * @param ports
     * @return
     */
    @RequestMapping("/timeoutPorts")
    public String testTimeoutPorts(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        return "timeoutPorts: " + ports;
    }

    @Autowired
    Router greyRouter;

    /**
     * http://localhost:9088/grey?rate=50
     *
     * @param rate
     * @return
     */
    @RequestMapping("/grey")
    public String testGrey(@RequestParam("rate") int rate) {
        ((GreyRouter) greyRouter).setGreyRate(rate);
        return "grey rate: " + rate;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyrpcDemoConsumerApplication.class, args);
    }


    @Bean
    public ApplicationRunner runner() {
        return args -> {
            testAll();
        };
    }

    private void testAll() {
        // 常规int类型，返回User对象
        System.out.println("Case 1. >>===[常规int类型，返回User对象]===");
        User user = userService.findById(1);
        System.out.println("RPC result userService.findById(1) = " + user);

        // 测试方法重载，同名方法，参数不同
        System.out.println("Case 2. >>===[测试方法重载，同名方法，参数不同===");
        User user1 = userService.findById(1, "hubao");
        System.out.println("RPC result userService.findById(1, \"hubao\") = " + user1);

        // 测试返回字符串
        System.out.println("Case 3. >>===[测试返回字符串]===");
        System.out.println("userService.getName() = " + userService.getName());

        // 测试重载方法返回字符串
        System.out.println("Case 4. >>===[测试重载方法返回字符串]===");
        System.out.println("userService.getName(123) = " + userService.getName(123));

        // 测试local toString方法
        System.out.println("Case 5. >>===[测试local toString方法]===");
        System.out.println("userService.toString() = " + userService.toString());

        // 测试long类型
        System.out.println("Case 6. >>===[常规int类型，返回User对象]===");
        System.out.println("userService.getId(10) = " + userService.getId(10));

        // 测试long+float类型
        System.out.println("Case 7. >>===[测试long+float类型]===");
        System.out.println("userService.getId(10f) = " + userService.getId(10f));

        // 测试参数是User类型
        System.out.println("Case 8. >>===[测试参数是User类型]===");
        System.out.println("userService.getId(new User(100,\"KK\")) = " +
                userService.getId(new User(100, "KK")));


        System.out.println("Case 9. >>===[测试返回long[]]===");
        System.out.println(" ===> userService.getLongIds(): ");
        for (long id : userService.getLongIds()) {
            System.out.println(id);
        }

        System.out.println("Case 10. >>===[测试参数和返回值都是long[]]===");
        System.out.println(" ===> userService.getLongIds(): ");
        for (long id : userService.getIds(new int[]{4, 5, 6})) {
            System.out.println(id);
        }

        // 测试参数和返回值都是List类型
        System.out.println("Case 11. >>===[测试参数和返回值都是List类型]===");
        List<User> list = userService.getList(List.of(
                new User(100, "KK100"),
                new User(101, "KK101")));
        list.forEach(System.out::println);

        // 测试参数和返回值都是Map类型
        System.out.println("Case 12. >>===[测试参数和返回值都是Map类型]===");
        Map<String, User> map = new HashMap<>();
        map.put("A200", new User(200, "KK200"));
        map.put("A201", new User(201, "KK201"));
        userService.getMap(map).forEach(
                (k, v) -> System.out.println(k + " -> " + v)
        );

        System.out.println("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
        System.out.println("userService.getFlag(false) = " + userService.getFlag(false));

        System.out.println("Case 14. >>===[测试参数和返回值都是User[]类型]===");
        User[] users = new User[]{
                new User(100, "KK100"),
                new User(101, "KK101")};
        Arrays.stream(userService.findUsers(users)).forEach(System.out::println);

        System.out.println("Case 15. >>===[测试参数为long，返回值是User类型]===");
        User userLong = userService.findById(10000L);
        System.out.println(userLong);

        System.out.println("Case 16. >>===[测试参数为boolean，返回值都是User类型]===");
        User user100 = userService.ex(false);
        System.out.println(user100);

        System.out.println("Case 17. >>===[测试服务端抛出一个RuntimeException异常]===");
        try {
            User userEx = userService.ex(true);
            System.out.println(userEx);
        } catch (RuntimeException e) {
            System.out.println(" ===> exception: " + e.getMessage());
        }

        System.out.println("Case 18. >>===[测试服务端抛出一个超时重试后成功的场景]===");
//         超时设置的【漏斗原则】
//        A 2000 -> B 1500 ->C 1200 -> D 1000
        long start = System.currentTimeMillis();
        userService.timeoutFind(1100);
        userService.timeoutFind(1100);
        System.out.println("userService.find take "
                + (System.currentTimeMillis() - start) + " ms");

        System.out.println("Case 19. >>===[测试通过Context跨消费者和提供者进行传参]===");
        String Key_Version = "rpc.version";
        String Key_Message = "rpc.message";
        RpcContext.setContextParameter(Key_Version, "v8");
        String version = userService.echoParameter(Key_Version);
        System.out.println(" ===> echo parameter from c->p->c: " + Key_Version + " -> " + version);

        RpcContext.setContextParameter(Key_Message, "this is a test message");
        String message = userService.echoParameter(Key_Message);
        System.out.println(" ===> echo parameter from c->p->c: " + Key_Message + " -> " + message);
    }
}
