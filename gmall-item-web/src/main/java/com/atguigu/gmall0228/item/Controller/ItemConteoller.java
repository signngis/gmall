package com.atguigu.gmall0228.item.Controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0228.bean.SkuInfo;
import com.atguigu.gmall0228.bean.SkuSaleAttrValue;
import com.atguigu.gmall0228.bean.SpuSaleAttr;
import com.atguigu.gmall0228.service.SkuService;
import com.atguigu.gmall0228.service.SpuInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemConteoller {

    @Reference
    SkuService skuService;
    @Reference
    SpuInfoService spuInfoService;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable String skuId,ModelMap map){
        /*查出skuInfo信息*/
        SkuInfo skuInfo = skuService.getSkuInfo(skuId);
        /*查出这个skuInfo对应的spuId*/
        String spuId = skuService.getSpuIdBySkuId(skuId);
        /*根据spuId和spuId查出skuId的兄弟sku信息的集合(销售属性列表)  表述有问题*/
        List<SpuSaleAttr> spuSaleAttrs =spuInfoService.getSpuSaleAttrListCheckBySku(spuId,skuId);
//        动态切换销售属性值对应的skuId的hash表.
//        1. 从数据库查询出同一个spu下的所有的sku信息,即当前选择的sku的兄弟sku信息
        List<SkuInfo> skuSaleAttrValueListBySpu = skuService.getSkuSaleAttrValueListBySpu(spuId);
//        hash表的作用是:从页面选择销售属性值信息,根据选择的销售属性值信息与hash表对比,定位到那个产品
//        根据此销售属性值获取spuId,然后跳转到该商品的相关页面
//        2. 创建map,来以key,value的形式存储从页面获得到的产品销售属性信息
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (SkuInfo info : skuSaleAttrValueListBySpu) {
            String v = info.getId();
            String k ="";
//            拼接销售属性值的id,把key按照一定格式进行封装.页面的信息以相同的格式封装,当做key来去对应的value.
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                if(StringUtils.isNoneBlank(k)){
                    k = k+"|";
                }
                k = k + skuSaleAttrValue.getSaleAttrValueId();
            }
            stringStringHashMap.put(k,v);
        }
//       把对象转换为json格式,方便页面的取用.
        map.put("skuJsonBrother", JSON.toJSONString(stringStringHashMap));
        /*把当前的sku对象放入域中*/
        map.put("skuInfo",skuInfo);
//        把商品销售属性放入域中
        map.put("spuSaleAttrListCheckBySku",spuSaleAttrs);
        return "item";
    }
    @RequestMapping("index")
    public String index(ModelMap map){
        map.put("hello","hello thymeleaf");

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            strings.add("集合元素" +i);
        }
        map.put("strings",strings);
        return "index";
    }
}
