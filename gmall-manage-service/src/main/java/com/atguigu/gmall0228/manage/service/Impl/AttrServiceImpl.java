package com.atguigu.gmall0228.manage.service.Impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0228.bean.BaseAttrInfo;
import com.atguigu.gmall0228.bean.BaseAttrValue;
import com.atguigu.gmall0228.manage.mapper.AttrMapper;
import com.atguigu.gmall0228.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall0228.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    AttrMapper attrMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;


    @Override
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfos = attrMapper.select(baseAttrInfo);
//        放入属性值列表
//        1.遍历查询到的baseAttrInfos,
        for (BaseAttrInfo attrInfo : baseAttrInfos) {
//            获得attrID
            String attrId = attrInfo.getId();
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrId);
            List<BaseAttrValue> select = baseAttrValueMapper.select(baseAttrValue);
//          将赋值完后的对象,放入到BaseAttrInfo中.
            attrInfo.setAttrValueList(select);
        }
        return baseAttrInfos;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        String id = baseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
//            保存操作
//            插入属性表数据,生成主键
            attrMapper.insertSelective(baseAttrInfo);
//            根据属性主键批量插入属性值
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }

        }
    }

    @Override
    public void deleteAttrInfo(String id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setId(id);
        attrMapper.delete(baseAttrInfo);
    }


}
