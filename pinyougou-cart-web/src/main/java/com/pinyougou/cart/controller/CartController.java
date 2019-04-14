package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * author: DahongZhou
 * Date:
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    /**
     * 购物车列表
     *
     * @param
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人" + username);

        String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";//将空字符串格式变为json格式
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        if (username.equals("anonymousUser")) {//如果未登录
            //将cartListString字符串转换成json对象
            System.out.println("从cookie中提取购物车" + cartList_cookie);
            //从cookie中提取购物车
            return cartList_cookie;
        } else {//如果已登录
            //获取redis购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);//从redis中提取
            //得到合并后的购物车
            if (cartList_cookie.size() > 0) {//如果本地存在购物车
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                util.CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
                System.out.println("合并了购物车......");
            }
            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    //@CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {

        //springMVC的版本在4.2或以上版本，可以用注解@CrossOrigin的方式替代以下两句
        //跨域解决方案CORS：设置可以访问的域，如果第二个参数为*，表示所有跨域都可以访问
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//此方法不需要操作cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");//如果需要操作cookie，必须加上此句，且可以访问的域不能为*

        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人" + username);
        try {
            //从cookie中提取购物车
            List<Cart> cartList = findCartList();//获取购物车列表

            //调用服务方法，操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) { //如果是未登录，保存到cookie
                util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            } else {//如果是已登录，保存到redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加到购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加到购物车失败");
        }
    }
}