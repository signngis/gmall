package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.BaseSaleAttr;
import com.atguigu.gmall0228.bean.SpuImage;
import com.atguigu.gmall0228.bean.SpuInfo;
import com.atguigu.gmall0228.bean.SpuSaleAttr;

import java.util.List;

public interface SpuInfoService {
    List<SpuInfo> getSpuInfoList(String id);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    void deleteSpuInfo(String id);

    List<SpuSaleAttr> getSpuSaleAttrGroup(String spuId);

    List<SpuImage> getSpuImage(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId);
}
