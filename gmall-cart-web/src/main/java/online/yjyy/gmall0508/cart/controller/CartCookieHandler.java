package online.yjyy.gmall0508.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.CartInfo;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.config.CookieUtil;
import online.yjyy.gmall0508.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String COOKIECARTNAME = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManageService manageService;

    /**
     *
     * @param request 请求
     * @param response 响应
     * @param skuId 商品Id
     * @param userId 用户Id
     * @param skuNum 商品数据
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum){

        // 判断cookie 中是否有商品，有则数量+skuNum,没有直接添加
        // 先取得数据
        String cartJson  = CookieUtil.getCookieValue(request, COOKIECARTNAME, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        if (cartJson!=null&& cartJson.length()>0){
            // 取出来是字符串，将字符串转换为对象  // 1,2,3,4,5
            cartInfoList  = JSON.parseArray(cartJson, CartInfo.class);

            boolean ifExist = false;
            // 循环比较
            for (CartInfo cartInfo : cartInfoList) {
                // 购物车中有商品  2=2
                if (cartInfo.getSkuId().equals(skuId)){
                    // 设置数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    // 设置一下价格
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist=true;
                }
            }
            //没有商品
            if (!ifExist){
                // 新增
                SkuInfo skuInfo = manageService.getSkuInfo(skuId);
                CartInfo cartInfo=new CartInfo();

                cartInfo.setSkuId(skuId);
                cartInfo.setCartPrice(skuInfo.getPrice());
                cartInfo.setSkuPrice(skuInfo.getPrice());
                cartInfo.setSkuName(skuInfo.getSkuName());
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

                cartInfo.setUserId(userId);
                cartInfo.setSkuNum(skuNum);
                // 将没有匹配到的数据添加到原来的集合中
                cartInfoList.add(cartInfo);
            }
        }
        String newCartJson  = JSON.toJSONString(cartInfoList);
        // 添加
        CookieUtil.setCookie(request,response,COOKIECARTNAME,newCartJson,COOKIE_CART_MAXAGE,true);
    }

    public List<CartInfo> getCartList(HttpServletRequest request) {
        // 将数据从cookie 中取得
        String cartJson  = CookieUtil.getCookieValue(request, COOKIECARTNAME, true);
        // 将其转换为集合
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;

    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,COOKIECARTNAME);
    }
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        // 先查出所有数据
        List<CartInfo> cartList = getCartList(request);
        for (CartInfo cartInfo : cartList) {
            if (skuId.equals(cartInfo.getSkuId())){
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 将cartList 重新写入cookie
        CookieUtil.setCookie(request,response,COOKIECARTNAME,JSON.toJSONString(cartList),COOKIE_CART_MAXAGE,true);

    }
}
