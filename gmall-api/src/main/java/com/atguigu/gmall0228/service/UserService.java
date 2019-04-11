package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.UserAddress;
import com.atguigu.gmall0228.bean.UserInfo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
public interface UserService {
    List<UserInfo> userList();
    // List<UserInfo> userSingle(String name);
    UserInfo userSingle(@RequestParam("id") String id);
    void addUser(UserInfo user);
    void deleteUser(@RequestParam("id") String id);
    int updateUser(UserInfo userInfo);
    int updateUserByObj(UserInfo userInfo);

    UserInfo login(UserInfo userInfo);

    boolean verify(String userId);

    List<UserAddress> getAddressListByUserId(String userId);

    UserAddress getUserAddressByAddressId(String addressId);
}
