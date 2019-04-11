package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.BaseAttrInfo;

import java.util.List;

public interface AttrService {
    List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    void deleteAttrInfo(String id);
}
