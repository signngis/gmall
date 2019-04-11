package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.CartInfo;
import com.atguigu.gmall0228.bean.SkuInfo;

import java.util.List;

public interface SkuService {
    void saveSku(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    String getSpuIdBySkuId(String skuId);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getSkuInfoByCatalog3Id(String catalog3Id);

    boolean checkSkuPrice(CartInfo cartInfo);
}
