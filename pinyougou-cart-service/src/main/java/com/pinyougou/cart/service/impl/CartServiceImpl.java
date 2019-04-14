package com.pinyougou.cart.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 *
 * @author Administrator
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {//用户在浏览商品时，商品状态可能发生改变
            throw new RuntimeException("商品状态无效");
        }

        //2.获取商家ID
        String sellerId = item.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {

            //4.1 新建购物车对象 ，
            cart = new Cart();
            cart.setSellerId(sellerId);//商家ID
            cart.setSellerName(item.getSeller());//商家名称

            //createOrderItem()封装，创建新的购物车明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderItemList = new ArrayList();//创建购物车明细列表
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2将购物车对象添加到购物车列表
            cartList.add(cart);

        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 判断购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);

            if (orderItem == null) {
                //5.1. 如果没有，新增购物车明细，并添加到购物车列表
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);//添加数量
                orderItem.setTotalFee(new Double(orderItem.getPrice().doubleValue() * orderItem.getNum()));//更改金额
                //如果数量操作后小于等于0，则移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }


    /**
     * 根据商家ID查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 在购物车明细列表中，根据商品明细ID查询
     *
     * @param orderItemList 购物车明细列表
     * @param itemId        商品id
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            //Long是对象，两者比较的是地址
            //long是具体值
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;//循环完成没有具体的商品明细，直接返回null
    }

    /**
     * 创建订单明细
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new Double(item.getPrice().doubleValue() * num));
        return orderItem;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    //从redis中查询购物车数据
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据.....用户名：" + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList();
        }
        return cartList;
    }

    //保存购物车数据到redis
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据.....用户名：" + username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    //合并cookie和redis的购物车数据
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

}