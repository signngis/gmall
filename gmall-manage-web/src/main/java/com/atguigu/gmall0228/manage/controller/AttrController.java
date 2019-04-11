package com.atguigu.gmall0228.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.BaseAttrInfo;
import com.atguigu.gmall0228.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class AttrController {
    @Reference
    AttrService attrService;


    @RequestMapping("getAttrListByCtg3")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListByCtg3(@RequestParam Map<String,String> map){
      String catalog3Id = map.get("catalog3Id");
      List<BaseAttrInfo> attrInfos = attrService.getAttrListByCtg3(catalog3Id);
      return attrInfos;
    }

    @RequestMapping("saveAttr")
    @ResponseBody
    public void saveAttr(BaseAttrInfo baseAttrInfo){
        attrService.saveAttr(baseAttrInfo);
    }

    @RequestMapping("/deleteAttrInfo")
    @ResponseBody
    public void deleteAttrInfo(@RequestParam Map<String,String> map){
//        获取属性的ID
        String id = map.get("id");
        attrService.deleteAttrInfo(id);
    }
}
