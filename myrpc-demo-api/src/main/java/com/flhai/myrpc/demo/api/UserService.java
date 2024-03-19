package com.flhai.myrpc.demo.api;

public interface UserService {
    User findById(int id);

    // 支持重载
    User findById(int id, String name);

    int getId();
    String getName();
    String getName(int number);
}
