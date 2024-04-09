package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.annotation.MyConsumer;
import com.flhai.myrpc.core.api.RpcRequest;
import com.flhai.myrpc.core.api.RpcResponse;
import com.flhai.myrpc.core.consumer.ConsumerConfig;
import com.flhai.myrpc.core.provider.ProviderInvoker;
import com.flhai.myrpc.demo.api.OrderService;
import com.flhai.myrpc.demo.api.User;
import com.flhai.myrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import({ConsumerConfig.class})
@RestController
@Disabled("Disabled as the case below doesn't work as intended")
@Slf4j
class MyrpcDemoProviderApplicationTests {

    @Autowired
    ProviderInvoker providerInvoker;

    // use http + json to communicate
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        // find the service
        return providerInvoker.invokeRequest(request);
    }

    @MyConsumer
    private UserService userService;

    @MyConsumer
    private OrderService orderService;

    @Test
    void testFindById() {
        log.info("Case 1. >>===[常规int类型，返回User对象]===");
        User user = userService.findById(1);
        assertNotNull(user);
        log.info("RPC result userService.findById(1) = " + user);
    }

    @Test
    void testFindByIdWithAdditionalParam() {
        log.info("Case 2. >>===[测试方法重载，同名方法，参数不同]===");
        User user = userService.findById(1, "hubao");
        assertNotNull(user);
        log.info("RPC result userService.findById(1, \"hubao\") = " + user);
    }

    @Test
    void testGetName() {
        log.info("Case 3. >>===[测试返回字符串]===");
        String name = userService.getName();
        assertNotNull(name);
        log.info("userService.getName() = " + name);
    }

    @Test
    void testGetNameWithParam() {
        log.info("Case 4. >>===[测试重载方法返回字符串]===");
        String name = userService.getName(123);
        assertNotNull(name);
        log.info("userService.getName(123) = " + name);
    }

    @Test
    void testUserServiceToString() {
        log.info("Case 5. >>===[测试local toString方法]===");
        String toStringResult = userService.toString();
        assertNotNull(toStringResult);
        log.info("userService.toString() = " + toStringResult);
    }

    @Test
    void testGetIdWithLongParam() {
        log.info("Case 6. >>===[测试long类型]===");
        long id = userService.getId(10);
        log.info("userService.getId(10) = " + id);
    }

    @Test
    void testGetIdWithFloatParam() {
        log.info("Case 7. >>===[测试long+float类型]===");
        float id = userService.getId(10f);
        log.info("userService.getId(10f) = " + id);
    }

    @Test
    void testGetIdWithUserParam() {
        log.info("Case 8. >>===[测试参数是User类型]===");
        long id = userService.getId(new User(100, "KK"));
        log.info("userService.getId(new User(100,\"KK\")) = " + id);
    }

    @Test
    void testGetLongIds() {
        log.info("Case 9. >>===[测试返回long[]]===");
        long[] ids = userService.getLongIds();
        assertNotNull(ids);
        log.info(" ===> userService.getLongIds(): ");
        Arrays.stream(ids).forEach(System.out::println);
    }

    @Test
    void testGetIdsWithIntArrayParam() {
        log.info("Case 10. >>===[测试参数和返回值都是long[]]===");
        long[] ids = userService.getLongIds(new long[]{4, 5, 6});
        assertNotNull(ids);
        log.info(" ===> userService.getIds(new int[]{4,5,6}): ");
        Arrays.stream(ids).forEach(System.out::println);
    }

    @Test
    void testGetList() {
        log.info("Case 11. >>===[测试参数和返回值都是List类型]===");
        List<User> list = userService.getList(List.of(new User(100, "KK100"), new User(101, "KK101")));
        assertNotNull(list);
        list.forEach(System.out::println);
    }

    @Test
    void testGetMap() {
        log.info("Case 12. >>===[测试参数和返回值都是Map类型]===");
        Map<String, User> map = new HashMap<>();
        map.put("A200", new User(200, "KK200"));
        map.put("A201", new User(201, "KK201"));
        Map<String, User> resultMap = userService.getMap(map);
        assertNotNull(resultMap);
        resultMap.forEach((k, v) -> log.info(k + " -> " + v));
    }

    @Test
    void testGetFlag() {
        log.info("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
        boolean flag = userService.getFlag(false);
        log.info("userService.getFlag(false) = " + flag);
    }

    @Test
    void testFindUsers() {
        log.info("Case 14. >>===[测试参数和返回值都是User[]类型]===");
        User[] users = userService.findUsers(new User[]{new User(100, "KK100"), new User(101, "KK101")});
        assertNotNull(users);
        Arrays.stream(users).forEach(System.out::println);
    }

    @Test
    void testFindByIdWithLong() {
        log.info("Case 15. >>===[测试参数为long，返回值是User类型]===");
        User user = userService.findById(10000L);
        assertNotNull(user);
        log.info(user.toString());
    }

    @Test
    void testExWithFalse() {
        log.info("Case 16. >>===[测试参数为boolean，返回值都是User类型]===");
        User user = userService.ex(false);
        assertNotNull(user);
        log.info(user.toString());
    }

    @Test
    void testExWithTrue() {
        log.info("Case 17. >>===[测试服务端抛出一个RuntimeException异常]===");
        try {
            User user = userService.ex(true);
            log.info(user.toString());
        } catch (RuntimeException e) {
            log.info(" ===> exception: " + e.getMessage());
        }
    }
}

