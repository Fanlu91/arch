package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.demo.api.User;
import com.flhai.myrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@MyProvider
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;

    // 通过端口号区分不同的服务提供者
    @Override
    public User findById(int id) {
        return new User(id, "my-"
                + environment.getProperty("server.port")
                + "_" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "KK-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "KK123";
    }

    @Override
    public String getName(int id) {
        return "Cola-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[]{100, 200, 300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1, 2, 3};
    }

    @Override
    public long[] getLongIds(long[] ids) {
        return ids;
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public User[] findUsers(User[] users) {
        return users;
    }

    @Override
    public List<User> getList(List<User> userList) {
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        return userMap;
    }

    @Override
    public Boolean getFlag(boolean flag) {
        return !flag;
    }

    @Override
    public User findById(long id) {
        return new User(Long.valueOf(id).intValue(), "KK");
    }

    @Override
    public User ex(boolean flag) {
        if (flag) throw new RuntimeException("just throw an exception");
        return new User(100, "KK100");
    }

    String timeoutPorts = "8081";

    @Override
    public void setTimeoutPorts(String ports) {
        this.timeoutPorts = ports;
    }

    @Override
    public String timeoutFind(int timeout) {
        String port = environment.getProperty("server.port");
        // timeoutPorts string uses , to split
        // check if current port is in timeoutPorts
        System.out.println("timeoutPorts: " + timeoutPorts + ", port: " + port);
        if (Arrays.stream(timeoutPorts.split(",")).anyMatch(p -> p.equals(port))) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return port;
    }


}
