package com.atguigu.gmall0228.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0228.bean.BaseCatalog1;
import com.atguigu.gmall0228.bean.BaseCatalog2;
import com.atguigu.gmall0228.bean.BaseCatalog3;
import com.atguigu.gmall0228.manage.mapper.Catalog1Mapper;
import com.atguigu.gmall0228.manage.mapper.Catalog2Mapper;
import com.atguigu.gmall0228.manage.mapper.Catalog3Mapper;
import com.atguigu.gmall0228.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    Catalog1Mapper catalog1Mapper;
    @Autowired
    Catalog2Mapper catalog2Mapper;
    @Autowired
    Catalog3Mapper catalog3Mapper;
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return catalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog2Id) {
        BaseCatalog2 catalog2 = new BaseCatalog2();
        catalog2.setCatalog1Id(catalog2Id);
        return catalog2Mapper.select(catalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return catalog3Mapper.select(baseCatalog3);
    }

}
