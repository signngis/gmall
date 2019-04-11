package com.atguigu.gmall0228.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.SkuInfo;
import com.atguigu.gmall0228.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SkuController {
    @Reference
    SkuService skuService;

    @RequestMapping("/saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        skuService.saveSku(skuInfo);
        return  "success";
    }

}
