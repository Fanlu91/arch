package com.flhai.myrpc.demo.consumer;

import com.flhai.myrpc.core.annotation.MyConsumer;
import com.flhai.myrpc.core.api.Router;
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
        log.info("Case 1. >>===[常规int类型，返回User对象]===");
        User user = userService.findById(1);
        log.info("RPC result userService.findById(1) = " + user);

        // 测试方法重载，同名方法，参数不同
        log.info("Case 2. >>===[测试方法重载，同名方法，参数不同===");
        User user1 = userService.findById(1, "hubao");
        log.info("RPC result userService.findById(1, \"hubao\") = " + user1);

        // 测试返回字符串
        log.info("Case 3. >>===[测试返回字符串]===");
        log.info("userService.getName() = " + userService.getName());

        // 测试重载方法返回字符串
        log.info("Case 4. >>===[测试重载方法返回字符串]===");
        log.info("userService.getName(123) = " + userService.getName(123));

//         测试local toString方法
//        log.info("Case 5. >>===[测试local toString方法]===");
//        log.info("userService.toString() = " + userService.toString());

//         测试long类型
        log.info("Case 6. >>===[常规int类型，返回User对象]===");
        log.info("userService.getId(10) = " + userService.getId(10));

        // 测试long+float类型
        log.info("Case 7. >>===[测试long+float类型]===");
        log.info("userService.getId(10f) = " + userService.getId(10f));

        // 测试参数是User类型
        log.info("Case 8. >>===[测试参数是User类型]===");
        log.info("userService.getId(new User(100,\"KK\")) = " +
                userService.getId(new User(100, "KK")));


        log.info("Case 9. >>===[测试返回long[]]===");
        log.info(" ===> userService.getLongIds(): ");
        for (long id : userService.getLongIds()) {
            log.info(String.valueOf(id));
        }

        log.info("Case 10. >>===[测试参数和返回值都是long[]]===");
        log.info(" ===> userService.getLongIds(): ");
        for (long id : userService.getIds(new int[]{4, 5, 6})) {
            log.info(String.valueOf(id));
        }

        // 测试参数和返回值都是List类型
        log.info("Case 11. >>===[测试参数和返回值都是List类型]===");
        List<User> list = userService.getList(List.of(
                new User(100, "KK100"),
                new User(101, "KK101")));
        list.forEach(System.out::println);

        // 测试参数和返回值都是Map类型
        log.info("Case 12. >>===[测试参数和返回值都是Map类型]===");
        Map<String, User> map = new HashMap<>();
        map.put("A200", new User(200, "KK200"));
        map.put("A201", new User(201, "KK201"));
        userService.getMap(map).forEach(
                (k, v) -> log.info(k + " -> " + v)
        );

        log.info("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
        log.info("userService.getFlag(false) = " + userService.getFlag(false));

        log.info("Case 14. >>===[测试参数和返回值都是User[]类型]===");
        User[] users = new User[]{
                new User(100, "KK100"),
                new User(101, "KK101")};
        Arrays.stream(userService.findUsers(users)).forEach(System.out::println);

        log.info("Case 15. >>===[测试参数为long，返回值是User类型]===");
        User userLong = userService.findById(10000L);
        log.info(userLong.toString());

        log.info("Case 16. >>===[测试参数为boolean，返回值都是User类型]===");
        User user100 = userService.ex(false);
        log.info(user100.toString());

        log.info("Case 17. >>===[测试服务端抛出一个RuntimeException异常]===");
        try {
            User userEx = userService.ex(true);
            log.info(userEx.toString());
        } catch (Exception e) {
            log.info(" ===> exception: " + e.getMessage());
        }
    }
}
