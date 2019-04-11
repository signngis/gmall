package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.UserAddress;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface UserAddressService {

    UserAddress selectUserAdById(@RequestParam("id") String id);

    List<UserAddress> selectUserAdAll();

    void deleteAdById(String id);

    void insertAd(UserAddress user);

    int updateUserAd(UserAddress user);

    int updateUserAdByObj(UserAddress userAddress);
}
