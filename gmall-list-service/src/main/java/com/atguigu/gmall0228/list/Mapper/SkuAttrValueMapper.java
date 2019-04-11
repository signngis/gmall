package com.atguigu.gmall0228.list.Mapper;

import com.atguigu.gmall0228.bean.BaseAttrInfo;
import com.atguigu.gmall0228.bean.SkuAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuAttrValueMapper extends Mapper<SkuAttrValue> {
    List<BaseAttrInfo> selectAttrListByValueIds(@Param("idJoin") String idJoin);
}
