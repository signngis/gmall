package com.atguigu.gmall0228.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0228.bean.UserAddress;
import com.atguigu.gmall0228.bean.UserInfo;
import com.atguigu.gmall0228.service.UserService;
import com.atguigu.gmall0228.user.mapper.UserAddressMapper;
import com.atguigu.gmall0228.user.mapper.UserInfoMapper;
import com.atguigu.gmall0228.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;

import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public List<UserInfo> userList() {
        List<UserInfo> userInfoList = userInfoMapper.selectAll();
//        UserInfo userInfo = new UserInfo();
//        userInfo.setId(1);
        return userInfoList;
    }
    @Override
    public UserInfo userSingle(@RequestParam("id") String id) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        UserInfo userInfoList = userInfoMapper.selectOne(userInfo);
        return userInfoList;
    }
    @Override
    public void addUser(UserInfo user) {
        userInfoMapper.insertSelective(user);
    }

    @Override
    public void deleteUser( @RequestParam("id") String id) {
        userInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int updateUser(UserInfo userInfo) {
        int row = userInfoMapper.updateByPrimaryKey(userInfo);
        return  row;
    }

    @Override
    public int updateUserByObj(UserInfo userInfo) {

        int row = userInfoMapper.updateByPrimaryKeySelective(userInfo);
        return 1;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
//        验证用户名和密码
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);
//        成功登录,放入缓存用户信息
//        user:userId:info
        if(userInfo1!=null){
            Jedis jedis = redisUtil.getJedis();
//            把用户信息义\以字符串的形式放入到Redis中
            jedis.setex("user:"+userInfo1.getId()+":info",60*30,JSON.toJSONString(userInfo1));
            jedis.close();
        }

        return userInfo1;
    }

//    根据key从缓存中获取对象,判断对象,若为空则返回false,
//    不为空则根据key设置缓存过期的时间
    @Override
    public boolean verify(String userId) {
        Jedis jedis = redisUtil.getJedis();

        String s = jedis.get("user:" + userId + ":info");

        if(StringUtils.isBlank(s)){
            return false;
        }else {
//            根据当前key获取过期时间
            jedis.expire("user:" + userId + ":info",60*30);
            return true;
        }
    }

    @Override
    public List<UserAddress> getAddressListByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);//userId =null
        List<UserAddress> select = userAddressMapper.select(userAddress);
        return select;
    }

    @Override
    public UserAddress getUserAddressByAddressId(String addressId) {//addressId=""有问题
        UserAddress userAddress = new UserAddress();
        userAddress.setId(addressId);
        UserAddress address = userAddressMapper.selectOne(userAddress);
        return address;
    }
}
