package com.atguigu.gmall0228.list.ListService.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0228.bean.BaseAttrInfo;
import com.atguigu.gmall0228.bean.SkuLsInfo;
import com.atguigu.gmall0228.bean.SkuLsParam;
import com.atguigu.gmall0228.list.Mapper.SkuAttrValueMapper;
import com.atguigu.gmall0228.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(String idJoin) {

        List<BaseAttrInfo> baseAttrInfos = skuAttrValueMapper.selectAttrListByValueIds(idJoin);

        return baseAttrInfos;
    }

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
//      封装es的dsl查询字符串
        String dsl = getDslQuery(skuLsParam);
        System.err.print(dsl);
//        执行es的查询对象
        Search build = new Search.Builder(dsl).addIndex("gmall0228").addType("skuLsInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        解析返回结果
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;
//            用高亮字段代替原始字段
//            1.判断有没有关键字
            if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
//                取出高亮字段
                Map<String, List<String>> highlight = hit.highlight;
                List<String> skuNameList = highlight.get("skuName");
                System.err.print(skuNameList);
                String skuNameHihlight = skuNameList.get(0);
//                替换原始字段
                source.setSkuName(skuNameHihlight);
            }
            skuLsInfos.add(source);
        }
        return skuLsInfos;
    }



    private String getDslQuery(SkuLsParam skuLsParam) {
        // 使用dslquery工具封装参数
        // query bool
        // filter term
        // must match

//        类似于stringBuffer,用来存储封装的查询语句
        SearchSourceBuilder ssb = new SearchSourceBuilder();
//        符合参数(过滤/搜索)
        BoolQueryBuilder bool = new BoolQueryBuilder();
//        分类过滤,根据三级分类的id,分类过滤
        if(StringUtils.isNotBlank(skuLsParam.getCatalog3Id())){
            TermsQueryBuilder t = new TermsQueryBuilder("catalog3Id", skuLsParam.getCatalog3Id());
            bool.filter(t);
        }
//        属性值过滤,根据选择的属性值过滤
        if(skuLsParam.getValueId()!=null&&skuLsParam.getValueId().length>0){
            String[] valueIds = skuLsParam.getValueId();
            for (String valueId : valueIds) {
                TermsQueryBuilder t = new TermsQueryBuilder("skuAttrValueList.valueId", valueId);
                bool.filter(t);
            }
        }
//        根据关键字搜索
        if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
            MatchQueryBuilder m = new MatchQueryBuilder("skuName", skuLsParam.getKeyword());
            bool.must(m);
        }
        ssb.query(bool);
//        分页信息
        ssb.size(100);// 显示数据的数量
        ssb.from(0);// 从第几条开始
        HighlightBuilder highlightBuilder = new HighlightBuilder();
//        高亮显示就是添加一个<span>标签,用样式来控制
//        添加前缀信息
        highlightBuilder.preTags("<span style='color:blue'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        ssb.highlight(highlightBuilder);
        System.err.print(ssb);
        return ssb.toString();
    }

    // 对应的查询语句
    /*
    * {
  "from" : 0,
  "size" : 100,
  "query" : {
    "bool" : {
      "filter" : {
        "terms" : {
          "catalog3Id" : [ "61" ]
        }
      }
    }
  },
  "highlight" : {
    "pre_tags" : [ "<span style='color:blue'>" ],
    "post_tags" : [ "</span>" ],
    "fields" : {
      "skuName" : { }
    }
  }
}{
  "from" : 0,
  "size" : 100,
  "query" : {
    "bool" : {
      "filter" : {
        "terms" : {
          "catalog3Id" : [ "61" ]
        }
      }
    }
  },
  "highlight" : {
    "pre_tags" : [ "<span style='color:blue'>" ],
    "post_tags" : [ "</span>" ],
    "fields" : {
      "skuName" : { }
    }
  }
}
    *
    * */

}
