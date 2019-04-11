package com.atguigu.gmall0228.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.UserInfo;
import com.atguigu.gmall0228.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class UserController {
    @Reference
    UserService userService;
    //查询全部用户
    @RequestMapping("/userList")
    public ResponseEntity<List<UserInfo>> userList(){
      List<UserInfo> userInfoList = userService.userList();
      return ResponseEntity.ok(userInfoList);
    }

    @RequestMapping("/usersingle")
    public UserInfo userSingle(@RequestParam("id") String id){
        UserInfo userInfoList = userService.userSingle(id);
        return userInfoList;
    }

    @PostMapping("/adduser")
    public void addUser(String id,String name,String passwd,String email){
       UserInfo userInfo = new UserInfo(null,name,passwd,email);
       userService.addUser(userInfo);
    }
    //删除单一用户
    @DeleteMapping("/deleteuser")
    public void deleteUser( @RequestParam("id") String id){
        userService.deleteUser(id);
    }


    //更新用户
    @PutMapping("/updateuserAll")
    public int updateUser(UserInfo userInfo){
        int row = userService.updateUserByObj(userInfo);
        return row;
    }

}
