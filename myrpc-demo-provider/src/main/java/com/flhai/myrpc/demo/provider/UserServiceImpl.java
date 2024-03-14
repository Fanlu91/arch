package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.demo.api.User;
import com.flhai.myrpc.demo.api.UserService;
import org.springframework.stereotype.Component;


@MyProvider
@Component
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id,"my-"+System.currentTimeMillis());
    }
}
