package com.atguigu.gmall0228.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0228.bean.*;
import com.atguigu.gmall0228.manage.mapper.*;
import com.atguigu.gmall0228.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Override
    public List<SpuInfo> getSpuInfoList(String id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(id);
        List<SpuInfo> list = spuInfoMapper.select(spuInfo);
        return list;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();
        return baseSaleAttrs;
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
//      插入spuinfo信息
        spuInfoMapper.insertSelective(spuInfo);
        String spuId = spuInfo.getId();
//      插入属性信息
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr :spuSaleAttrList){
            spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
//            插入属性值信息
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue :spuSaleAttrValueList){
                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }
//        根据spu主键保存图片集合
        List<SpuImage> spuImages = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImages) {
            spuImage.setSpuId(spuId);
            spuImageMapper.insert(spuImage);
        }


    }

    @Override
    public void deleteSpuInfo(String id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setId(id);
        spuInfoMapper.delete(spuInfo);
    }
//  根据spuId查询出销售属性列表
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrGroup(String spuId) {
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.select(spuSaleAttr);
//        封装销售属性值
        for (SpuSaleAttr saleAttr : spuSaleAttrs) {
            String saleAttrId = saleAttr.getSaleAttrId();
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSaleAttrId(saleAttrId);
            spuSaleAttrValue.setSpuId(spuId);
            List<SpuSaleAttrValue> select = spuSaleAttrValueMapper.select(spuSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(select);
        }
        return spuSaleAttrs;
    }
//  查询图片信息
    @Override
    public List<SpuImage> getSpuImage(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> select = spuImageMapper.select(spuImage);
        return select;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId) {
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("spuId",spuId);
        objectObjectHashMap.put("skuId",skuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(objectObjectHashMap);
        return spuSaleAttrs;
    }
}
