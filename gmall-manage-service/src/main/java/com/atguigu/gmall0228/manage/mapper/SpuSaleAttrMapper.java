package com.atguigu.gmall0228.manage.mapper;

import com.atguigu.gmall0228.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(HashMap<Object,Object> objectObjectHashMap);
}
