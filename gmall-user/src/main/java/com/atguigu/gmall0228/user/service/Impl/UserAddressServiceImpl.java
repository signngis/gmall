package com.atguigu.gmall0228.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0228.bean.UserAddress;
import com.atguigu.gmall0228.service.UserAddressService;
import com.atguigu.gmall0228.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public UserAddress selectUserAdById(@RequestParam("id") String id) {
        UserAddress userAddress = userAddressMapper.selectByPrimaryKey(id);
        return userAddress;
    }

    @Override
    public List<UserAddress> selectUserAdAll() {
        List<UserAddress> userAddresses = userAddressMapper.selectAll();
        return userAddresses;
    }

    @Override
    public void deleteAdById(String id) {
        userAddressMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void insertAd(UserAddress user) {
        userAddressMapper.insertSelective(user);
    }

    @Override
    public int updateUserAd(UserAddress user) {
        int row = userAddressMapper.updateByPrimaryKeySelective(user);
        return row;
    }

    @Override
    public int updateUserAdByObj(UserAddress userAddress) {
        int row = userAddressMapper.updateByPrimaryKeySelective(userAddress);
        return row;
    }
}
