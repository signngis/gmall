package com.atguigu.gmall0228.cart.CartService.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0228.bean.CartInfo;
import com.atguigu.gmall0228.cart.Mapper.CartInfoMapper;
import com.atguigu.gmall0228.service.CartService;
import com.atguigu.gmall0228.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;

//    根据userId,获取缓存中的商品列表信息.
/*
* 思路:
* 1. 我们把商品列表信息用key存储在Redis中,key的格式是: "userId:"+userId+":info"
* 2. 现在要从Redis中取出数据,则需要用相同格式的key来获取对应的值
* 3. 根据userId得到key,根据RedisUtil中的方法获取对应的value值.
* 4. 获取出来的value是字符串,把字符串转换为我们所需要的购物车对象,存储到相关的集合中
*
* */
    @Override
    public List<CartInfo> getCartListCacheByUser(String userId) {
        List<CartInfo> cartList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");
        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            cartList.add(cartInfo);
        }
        return cartList;
    }

    /*
    * 此方法是根据userId和skuId来确定一个购物车对象
    * 用处:可以根据返回的对象判断,如果对象存在,则说明此对象
    * 以前添加过,则就只用跟新商品数和商品总价就可以了.
    * 若为空,则直接新增
    * */
    @Override
    public CartInfo ifCartExist(String userId, String skuId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfo1 = cartInfoMapper.selectOne(cartInfo);
        return cartInfo1;
    }


    /*
    * 添加修改购物车:
    * 1. 根据传入的商品信息的主键来判断
    * 2. 如果该主键为空则是新增,主键自增,如主键为空,则说明还没有这一条数据.
    * 3. 如果不为空,则是修改.有主键,则说明该数据已存在.就可以根据userId和skuId定位到该条数据,实现更新.
    * 更新选用有选择的更新.
    *
    * */
    @Override
    public void addCart(CartInfo cartInfo) {
        if(StringUtils.isBlank(cartInfo.getId())){
//            添加
            cartInfoMapper.insert(cartInfo);
        }else {
//            修改
            Example e = new Example(CartInfo.class);
            e.createCriteria().andEqualTo("userId",cartInfo.getUserId()).andEqualTo("skuId",cartInfo.getSkuId());
            cartInfoMapper.updateByExampleSelective(cartInfo,e);
        }
    }


/*
* 作用:同步Redis
* 思路:
* 1. 先根据userId  从数据库查出商品信息
* 2. 新建一个map对象,用来接收数据库的信息
* 3. 调用RedisUtil工具类中的方法把数据同步到Redis上(Redis中的数据是一字符串的格式存储的,所以需要把
* 数据库对象转换为字符串)
* 4. 注:用以固定的格式作为Redis的key,后面用来去除Redis中的数据.
*
* */
    @Override
    public void cartCache(String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> select = cartInfoMapper.select(cartInfo);
        Map<String, String> stringStringHashMap = new HashMap<>();
        for (CartInfo info : select) {
            stringStringHashMap.put(info.getSkuId(), JSON.toJSONString(info));
        }
        Jedis jedis = redisUtil.getJedis();
        jedis.del("user:"+userId+":cart");
        jedis.hmset("user:"+userId+":cart",stringStringHashMap);
        jedis.close();
    }

    @Override
    public void updateCartChecked(CartInfo cartInfo) {
//     根据userId和skuId更新商品的选中状态.
        Example e = new Example(CartInfo.class);
        e.createCriteria().andEqualTo("userId",cartInfo.getUserId()).andEqualTo("skuId",cartInfo.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfo,e);
    }

//     合并购物车
    /*
    * 简介:在登录的时候合并购物车
    * 流程:
    * 1. 用户登录的成功的时候,同时进行合并购物车的操作.
    * 2. 调用合并购物车的方法,需要传递userId,同时还需要cookie中的信息.
    * 3. 根据用户id查询购物个的数据库,查出购物车中已存的商品信息
    * 4. 判断购物车是否为空,如果是新车,则直接把缓存中的商品信息直接插入到数据库中即可.
    * 5. 购物车不为空,则需要判断缓存中的商品信息是否与购物车中的有同样的.若有相同的,则修改数量与总价
    * 不同的则直接插入数据库.
    * 6. 合并完成后,删除cookie中的商品信息,同时把数据库中的数据同步到Redis
    *
    * */
    @Override
    public void mergeCart(String userId, List<CartInfo> cartListCookie) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartListDb = cartInfoMapper.select(cartInfo);

        if(cartListCookie!=null){
            for (CartInfo cartInfoCookie : cartListCookie) {
//            判断是否为新车
                boolean b = ifNewCart(cartListDb,cartInfoCookie);
                if(b){
//                新车,把缓存的商品信息添加到数据库
                    cartInfoCookie.setUserId(userId);
//                根据userId插入缓存中的数据到数据库
                    cartInfoMapper.insertSelective(cartInfoCookie);
                    cartListDb.add(cartInfoCookie);
                }else{
//                老车,更新数据库
                    for (CartInfo cartInfoDb : cartListDb) {
                        if(cartInfoCookie.getSkuId().equals(cartInfoDb.getSkuId())){
                            cartInfoDb.setSkuNum(cartInfoCookie.getSkuNum()+cartInfoDb.getSkuNum());
                            cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                        }
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);
                    }
                }
            }
        }

//        同步到Redis
        cartCache(userId);
    }

    // 根据userId获取选中的商品到订单页面
    @Override
    public List<CartInfo> getCartListCheckedCacheByUser(String userId) {
        List<CartInfo> cartList = new ArrayList<>();
//        从Redis中获取购物车中的商品
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");
        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            if(cartInfo.getIsChecked().equals("1")){
                cartList.add(cartInfo);
            }

        }
        return cartList;
    }

    @Override
    public void cleanCart(List<String> cartIds, String userId) {
//       根据被选中的购物车的id删除购物车
        String join = StringUtils.join(cartIds, ",");
        cartInfoMapper.deleteCheckedCart(join,userId);
//        刷新购物车缓存
        cartCache(userId);
    }


    //    判断是否为新车
    private boolean ifNewCart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfos) {
//            如果新加的商品在购物车中已存在,则为旧车,B为false.
            if(info.getSkuId().equals(cartInfo.getSkuId())){
                b= false;
                break;
            }
        }
        return  b;
    }
}
