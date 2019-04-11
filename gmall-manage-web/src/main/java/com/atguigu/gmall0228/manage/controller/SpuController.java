package com.atguigu.gmall0228.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.BaseSaleAttr;
import com.atguigu.gmall0228.bean.SpuImage;
import com.atguigu.gmall0228.bean.SpuInfo;
import com.atguigu.gmall0228.bean.SpuSaleAttr;
import com.atguigu.gmall0228.manage.util.MyFdfsUploadUtil;
import com.atguigu.gmall0228.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class SpuController {

    @Reference
    SpuInfoService spuInfoService;

    @RequestMapping("/getSpuInfoList")
    @ResponseBody
    public List<SpuInfo> getSpuInfoList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("ctg3ForSpuList");
        List<SpuInfo> list = spuInfoService.getSpuInfoList(catalog3Id);
        return list;
    }
    @RequestMapping("/baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){

        List<BaseSaleAttr> list = spuInfoService.baseSaleAttrList();
        return list;
    }

    @RequestMapping("/saveSpuInfo")
    @ResponseBody
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoService.saveSpuInfo(spuInfo);
    }

    @RequestMapping("/deleteSpuInfo")
    @ResponseBody
    public void deleteSpuInfo(@RequestParam Map<String,String> map){
        String id = map.get("id");
        spuInfoService.deleteSpuInfo(id);
    }

    @RequestMapping("uploadSpuImg")
    @ResponseBody
    public String uploadSpuImg(@RequestParam("file") MultipartFile file){
//         调用上传图片工具返回路径
         String httpPath = MyFdfsUploadUtil.uploadImage(file);
        return httpPath;
    }

//    根据supId获取销售属性列表
    @RequestMapping("/getSpuSaleAttrGroup")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrGroup(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");
        List<SpuSaleAttr> spuSaleAttrs =spuInfoService.getSpuSaleAttrGroup(spuId);
        return spuSaleAttrs;
    }

//    获取所有的图片信息
    @RequestMapping("/getSpuImage")
    @ResponseBody
    public List<SpuImage> getSpuImage(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");
        List<SpuImage> spuImages = spuInfoService.getSpuImage(spuId);
        return spuImages;
    }
}
