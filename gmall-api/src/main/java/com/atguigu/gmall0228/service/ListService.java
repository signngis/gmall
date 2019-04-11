package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.BaseAttrInfo;
import com.atguigu.gmall0228.bean.SkuLsInfo;
import com.atguigu.gmall0228.bean.SkuLsParam;

import java.util.List;

public interface ListService {


    List<SkuLsInfo> search(SkuLsParam skuLsParam);

    List<BaseAttrInfo> getAttrListByValueIds(String idJoin);
}
