package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.demo.api.User;
import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@MyProvider
@Component
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "my-" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "my-" + name + "-" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId();
    }


    @Override
    public String getName() {
        return "default user name";
    }

    @Override
    public String getName(int number) {
        return "user name with " + number;
    }

    @Override
    public int[] getIds() {
        return new int[]{1, 2, 3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public long[] getLongIds() {
        return new long[]{10, 20, 30};
    }
}
