package com.flhai.myrpc.demo.api;

public interface UserService {
    User findById(int id);

    // 支持重载
    User findById(int id, String name);

    long getId(long id);

    long getId(User user);

    String getName();

    String getName(int number);

    int[] getIds();

    int[] getIds(int[] ids);

    long[] getLongIds();
}
