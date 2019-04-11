package com.atguigu.gmall0228.service;

import com.atguigu.gmall0228.bean.CartInfo;

import java.util.List;

public interface CartService {
    List<CartInfo> getCartListCacheByUser(String userId);

    CartInfo ifCartExist(String userId, String skuId);

    void addCart(CartInfo cartInfo);

    void cartCache(String userId);

    void updateCartChecked(CartInfo cartInfo);

    void mergeCart(String id, List<CartInfo> cartList);

    List<CartInfo> getCartListCheckedCacheByUser(String userId);

    void cleanCart(List<String> cartIds, String userId);
}
