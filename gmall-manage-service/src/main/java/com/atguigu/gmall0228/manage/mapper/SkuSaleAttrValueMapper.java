package com.atguigu.gmall0228.manage.mapper;

import com.atguigu.gmall0228.bean.SkuInfo;
import com.atguigu.gmall0228.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuInfo> selectSkuSaleAttrValueListBySpu(String spuId);
}
