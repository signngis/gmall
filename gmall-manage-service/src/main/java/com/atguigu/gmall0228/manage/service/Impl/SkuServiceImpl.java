package com.atguigu.gmall0228.manage.service.Impl;


import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0228.bean.*;
import com.atguigu.gmall0228.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall0228.manage.mapper.SkuImageMapper;
import com.atguigu.gmall0228.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall0228.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall0228.service.SkuService;
import com.atguigu.gmall0228.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public void saveSku(SkuInfo skuInfo) {
//        提交skuInfo信息
        skuInfoMapper.insertSelective(skuInfo);
//        提交图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage);
        }
//        提交属性值
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }
//        提交销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
    }

/*    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfoMapper.select(skuInfo);
        return skuInfo;
    }*/

    @Override
    public String getSpuIdBySkuId(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo info = skuInfoMapper.selectOne(skuInfo);
        String spuId = info.getSpuId();
        return spuId;
    }

//    根据spuId,查询出spu下的所有sku信息
    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfos = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuInfos;
    }

    @Override
    public List<SkuInfo> getSkuInfoByCatalog3Id(String catalog3Id) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);
        for (SkuInfo skuInfo1 : select) {
                SkuAttrValue skuAttrValue = new SkuAttrValue();
                skuAttrValue.setSkuId(skuInfo1.getId());
                List<SkuAttrValue> select1 = skuAttrValueMapper.select(skuAttrValue);
                skuInfo1.setSkuAttrValueList(select1);
        }
        return select;
    }

//    检验价格和库存
    @Override
    public boolean checkSkuPrice(CartInfo cartInfo) {
        BigDecimal skuPrice = cartInfo.getSkuPrice();
/*        if(skuPrice.equals(0)){
            return false;
        }*/
        if(cartInfo == null || cartInfo.getSkuPrice() == null){
            return false;
        }
        String skuId = cartInfo.getSkuId();
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo = skuInfoMapper.selectByPrimaryKey(skuInfo);
        if(skuInfo == null){
            return false;
        }
        if(cartInfo.getSkuPrice().compareTo(skuInfo.getPrice()) != 0){
            return false;
        }
        return true;
    }


    // 此方法是通用的访问数据库的方法.
    public SkuInfo getSkuInfoFromDb(String skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
//      SkuInfo基本信息的查询
        SkuInfo info = skuInfoMapper.selectOne(skuInfo);
//        封装图片信息
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        info.setSkuImageList(skuImages);
        return info;
    }
    /*正常步骤:Redis中没有数据,先从数据库查询,然后同步到Redis中
     * 实现:先判断Redis中是否存在所需的数据,如果有直接从Redis中
     * 取,如果没有先从数据库查询,然后同步到Redis中.
     *
     * 如果Redis被击穿,不能正常的通过Redis来获取数据,则用户会直接
     * 访问MySQL,这样可能导致数据库崩溃.为此我们可以在配置一台Redis
     * 服务器,用来在缓存Redis被击穿的情况下,对数据库保护.新配的Redis
     * 的作用是,对于高并发的相同的访问,需要去Redis中取获取分布式锁,根据
     * 分布式锁才能够访问数据库.这样就把高并发变成了排队的形式来
     * 访问,大大的降低了数据库的压力.
     *
     * 实现:先判断Redis缓存是否还存在,如果存在就按照正常套路获取信息,
     * 若缓存Redis失效,则跳转到分布式锁分支,按照排队的方式去访问MySQL.
     * 分布式锁实现的两种情况:
     * 1. 数据信息存在,则按正常的流程进行
     * 2. 如没有数据,则结束访问.后续访问不再执行
     *
     * */
//  Redis访问的实现代码
    @Override
    public SkuInfo getSkuInfo(String skuId){
//      用于存放从Redis中取得的key的值,用于判断Redis是否失效
        String skuInfoJson ="";
        SkuInfo skuInfo = new SkuInfo();
//        Redis中的key的格式
        String key = "sku:"+skuId+":info";
//        访问缓存数据
        Jedis jedis = redisUtil.getJedis();
//        判断jedis有无数据
        if(jedis!=null){ // jedis存在,获得key
            skuInfoJson = jedis.get(key);
        }
//        redis失效
//        jedis不存在,或者没有key,都说明Redis已经失效
        if(jedis==null||StringUtils.isBlank(skuInfoJson)){
//            不是empty的情况,先进数据库查询,查询结果有值,则返回.没有值则把锁置空.返回null
            if(!"empty".equals(skuInfoJson)) {
                // 申请分布式锁访问数据库
                SkuInfo skuInfoFromDb = null;
                System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "开始申请分布式锁");
                //  设置分布式锁
                String ok = jedis.set("sku:" + skuId + ":lock", "1", "nx", "px", 10000);
//                判断,如果具有分布式锁信息,开始访问数据库
                if ("OK".equals(ok)) {
                    System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "申请分布式锁成功，开始访问数据库");
//                    访问数据库.获取信息
                    skuInfoFromDb = getSkuInfoFromDb(skuId);
//                  判断数据库是否有该信息
                    if (skuInfoFromDb == null) {
                        System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "申请分布式锁成功，访问数据库为空，将缓存中商品对象置为空值");
//                        数据库信息为空,则把锁置为empty
                        jedis.setex(key, 60*30,"empty");
//                      关闭jedis
                        if (jedis != null) {
                            jedis.close();
                        }
//                        返回空值
                        skuInfo = null;
                    }else{
//                        查询结果有值,把信息同步到Redis中,然后关闭Redis
                        System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "申请分布式锁成功，成功访问数据库，将数据返回给前端");

                        if (jedis != null) {
                            // 访问db，将db中的商品信息同步到redis
                            String s = JSON.toJSONString(skuInfoFromDb);
                            jedis.set(key, s);
                            jedis.close();
                        }
//                        返回查询结果
                        skuInfo = skuInfoFromDb;
                    }

                    // 将分布式锁归还
                    System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "归还分布式锁");
//                    del就是删除分布式锁,也就是归还
                    jedis.del("sku:" + skuId + ":lock");
                    return skuInfo;
                }else{
                    // 未获得分布式锁,则线程sleep一段时间,在调用getSkuInfo(skuId),尝试获取分布式锁(自旋);
                    System.err.println("缓存失效，线程" + Thread.currentThread().getName() + "未获得分布式锁，分布式锁被占用，等待3秒，开始自旋。。。");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getSkuInfo(skuId);
                }
            }else{
//                数据库结果为空,则直接结束访问
                System.err.println("线程" + Thread.currentThread().getName() + "数据库中没有数据，直接结束访问");
            }
        }else{
//            Redis还活着,则直接解析Redis中的数据,转化成对象
            skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
        }
        return skuInfo;
    }

}
