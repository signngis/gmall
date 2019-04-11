package com.atguigu.gmall0228.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.SkuInfo;
import com.atguigu.gmall0228.bean.SkuLsInfo;
import com.atguigu.gmall0228.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {
    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;
    @Test
    public void contextLoads() throws Exception, IllegalAccessException {
        // 1. 查询skuInfo数据
        List<SkuInfo> skuInfos = skuService.getSkuInfoByCatalog3Id("61");
        // 2. 把数据封装到skuLsInfo中
        List<SkuLsInfo> skuLsInfos =  new ArrayList<>();
        for (SkuInfo skuInfo : skuInfos) {
            SkuLsInfo skuLsInfo = new SkuLsInfo();
//          把skuInfo中的数据copy到skuLsInfo中.字段相同copy ,不同的不做处理
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
            skuLsInfos.add(skuLsInfo);
        }
        // 3. 把skuLsInfo中的数据导入到es中
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
//            把数据逐条的添加到es中,Builder(skuLsInfo):要添加的数据类型,index("gmall"):es中的数据库名
//            type("SkuInfo"):es中的表名,id(skuLsInfo.getId()):bean的ID,属于自增.build():添加到es库中的执行方法.
            Index build = new Index.Builder(skuLsInfo).index("gmall0228").type("skuLsInfo").id(skuLsInfo.getId()).build();
//            执行
            jestClient.execute(build);
        }
        System.err.print("111111111111111111111111111111111");
    }

//    使用Java程序查询es
    @Test
    public void getData() throws  Exception{
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.toString();
//        封装es的dsl查询字符串
        String dsl = "";
        // 执行es查询对象
//        Builder(dsl):要执行的查询字符串,addIndex("gmall0228"):添加查询的库名,addType("SkuInfo"):查询的表名
        Search build = new Search.Builder(dsl).addIndex("gmall0228").addType("skuLsInfo").build();
//        执行 查询方法
        SearchResult execute = jestClient.execute(build);
        //解析返回结果
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
//            把数据转换为对象
            SkuLsInfo source = hit.source;
            skuLsInfoList.add(source);
        }
        System.err.print("111111111111111111111111111111111");

    }


        @Test
        public void testsort(){
            Integer[] nums = {10,58,72,5,9,7,45,15};//需要排序的数组
            nums = sort(nums,0,nums.length-1);
            System.out.println(Arrays.toString(nums));
        }


    }

}
