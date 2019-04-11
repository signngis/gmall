package com.atguigu.gmall0228.list.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0228.bean.*;
import com.atguigu.gmall0228.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListController {

    @Reference
    ListService listService;
// 跳转至首页
    @RequestMapping("/index")
    public String index(){
        return "index";
    }
//  跳转至list页面
    @RequestMapping("/list.html")
    public String List(SkuLsParam skuLsParam,ModelMap map){
        String catalog3Id = skuLsParam.getCatalog3Id();
//        根据条件查询出spu下的sku
        List<SkuLsInfo> search = listService.search(skuLsParam);
//        根据检索结果获得所有去重的valued,去重使用set集合,set集合特性
        String [] valueIds = getValueIds(search);
//        添加","
        String idJoin = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> baseAttrInfos = listService.getAttrListByValueIds(idJoin);
//        制作面包屑
        List<Crumb> crumbs = new ArrayList<>();// 暂时不清楚为什么放在这里
//        根据被选中的id删除该Id属性值所属于的属性
//        现获取页面所选中的属性值的id,这操作是为了方便做面包屑.面包屑中显示的信息,也就是地址栏携带的信息,也就是
//        用户选中的信息.
        String[] requestValueIds = skuLsParam.getValueId();
//        判断requestValueIds是否存在
        if(requestValueIds !=null&& requestValueIds.length>0){
            Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
//            此处用iterator()方法遍历的作用是,为了方便直接删除选中的属性值所在的哪一行的属性信息
            while(iterator.hasNext()){//iterator.hasNext()是否有下一个值
//                取出下一个值,iterator浮标放在第一个参数的上面,所以是取出下一个值
                BaseAttrInfo baseAttrInfo = iterator.next();
//                取出平台属性值
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//                遍历attrValueList集合,取出单个的平台属性值
                for (BaseAttrValue baseAttrValue : attrValueList) {
//                    获取属性列表中的平台属性值id
                    String valueId = baseAttrValue.getId();
//                    制作面包屑列表
                    for (String requestValueId : requestValueIds) {
//                      判断平台属性的id是否和请求栏中的属性ID是否相同
                        if(valueId.equals(requestValueId)){
//                            1.相同则表示该条属性已经被用户选择,则需要删除这一行的属性
//                            2. 同时也说明这个选中的属性值应该作为面包屑在面包屑里列表显示
//                            制作面包屑后再删除这行属性

//                  根据查询的形式拼接字符串,作为请求栏的地址
                            String urlParam = getUrlParam(skuLsParam,requestValueId);
                            Crumb crumb = new Crumb();
                            String valueName = baseAttrValue.getValueName();
                            crumb.setUrlParam(urlParam);
                            crumb.setValueName(valueName);
                            crumbs.add(crumb);
//                            删除已选中的属性值的所在的属性行
                            iterator.remove();;
                        }

                    }
                }

            }
        }
        // 当前的查询的关键字
        if (StringUtils.isNotBlank(skuLsParam.getKeyword())) {
            map.put("keyword", skuLsParam.getKeyword());
        }

        String urlParam = getUrlParam(skuLsParam);
        map.put("urlParam", urlParam);
        map.put("attrList", baseAttrInfos);
        map.put("skuLsInfoList", search);
        map.put("attrValueSelectedList", crumbs);
        return "list";
    }


//  根据参数拼接请求字符串
    private String getUrlParam(SkuLsParam skuLsParam, String ...requestValueIds) {

        String crumbValurId ="";
//        requestValueIds不为空,则取出requestValueIds中的值
        if(requestValueIds!=null&&requestValueIds.length>0){
            crumbValurId = requestValueIds[0];
        }

        String urlParam = "";
//        获取关键字
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueId = skuLsParam.getValueId();

//        如果根据三级分类id查询,则后面的查询一定携带三级分类的id
        if(StringUtils.isNotBlank(catalog3Id)){
            urlParam = urlParam + "catalog3Id=" +catalog3Id;
        }

//        如果根据关键字查询,则不会携带三级分类的Id
        if(StringUtils.isNotBlank(keyword)){
//            其实这一步不用判断,因为如果根据关键字查询,则不会涉及到三级分类的查询
            if(StringUtils.isNotBlank(catalog3Id)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam +"keyword=" + keyword;
        }

//      删除面包屑
//        1. 判断属性值Id是否存在
        if(valueId!=null&&valueId.length>0){
            for (int i = 0; i < valueId.length; i++) {
//       2. 拼接URL的时候排除掉请求列表中已有的属性值ID ,就相当于删除了
//                如果是&&,添加第一次,是成立的.但是第二次的时候,这个条件就永远不成立了,则参数不能拼接上去.则只能有一个面包屑
//                但是如果是|| ,则满足一个条件就可以进入方法,则可以据拼接,同时还会排除已有的.
                if (StringUtils.isNotBlank(crumbValurId) ||!valueId[i].equals(crumbValurId)) {
                    urlParam = urlParam + "&" + "valueId=" + valueId[i];
                }
            }
        }

        return urlParam;
    }

    //    去重方法的实现,去重的效果就是每个销售值属性都只展示一份,即不同sku中可能存在
//    重叠的属性信息,在此我们只需拿到一个即可.所以把重复的删除.在此我们使用
//    set集合.set集合天然的具有数据的唯一性.
    private String[] getValueIds(List<SkuLsInfo> search){
//        创建set集合来存放ValueIds
        Set<String> set = new HashSet<>();
//        遍历取出search中的SkuLsInfo
        for (SkuLsInfo skuLsInfo : search) {
//        取出属性值信息集合
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
//            遍历集合,把属性值id放入到set里面
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                set.add(skuLsAttrValue.getValueId());
            }
        }
//        把set集合转换成String[]
        String [] valueIds = new String[set.size()];
        Iterator<String> iterator = set.iterator();
        int i = 0;
        while(iterator.hasNext()){
            String next = iterator.next();
            valueIds[i] = next;
            i++;
        }
        return valueIds;


    }

}
