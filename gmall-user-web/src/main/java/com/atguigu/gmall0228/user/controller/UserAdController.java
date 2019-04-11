package com.atguigu.gmall0228.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.UserAddress;
import com.atguigu.gmall0228.service.UserAddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 单表的增删改查，使用通用mapper生成的方法即可。（通用mapper可以类比为mybatis逆向工程所生成的方法）
@RequestMapping("/userad")
@RestController
public class UserAdController {
    @Reference
    UserAddressService userAddressService;

    // 根据id查询地址
    @GetMapping("/querySingle")
    public UserAddress queryUserAd(@RequestParam("id") String id){
        UserAddress userAddress = userAddressService.selectUserAdById(id);
        return userAddress;
    }
    //查询所有的地址
    @GetMapping("/queryAll")
    public List<UserAddress> queryUserAd( ){
        List<UserAddress> userAddress = userAddressService.selectUserAdAll();
        return userAddress;
    }
    //删除地址信息
    @DeleteMapping("/delete")
    public void deleteAdById(@RequestParam("id")String id){
        userAddressService.deleteAdById(id);
    }
    //添加地址信息，根据所需要的字段，有选择的添加。
    @PostMapping("addAd")
    public void addAd(String id,String userAddress,String userId,String consignee,String phoneNum,String isDefault){
      UserAddress user = new UserAddress(id,userAddress,userId,consignee,phoneNum,isDefault);
      userAddressService.insertAd(user);
    }
    //更新,先查出所要更新的对象，再把要更新的字段设置到对象中，调用mapper的update方法即可。(此方法复杂化了，不建议)
  /*  @PutMapping("/update")
    public int update(String id,String userAddress,String userId){
        UserAddress user = queryUserAd(id);
        user.setUserAddress(userAddress);
        user.setUserId(userId);
        int row = userAddressService.updateUserAd(user);
        return row;
    }*/

    @PutMapping("/update")
    public int update(UserAddress userAddress){
        int row = userAddressService.updateUserAdByObj(userAddress);
        return row;
    }
}
