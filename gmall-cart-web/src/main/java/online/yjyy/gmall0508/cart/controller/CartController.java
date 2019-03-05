package online.yjyy.gmall0508.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.CartInfo;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.config.LoginRequire;
import online.yjyy.gmall0508.service.CartService;
import online.yjyy.gmall0508.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ManageService manageService;


    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        // 要获取skuNum，skuId
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        // 购物车分为登录，未登录 -- 看是否有userId即可
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            // 已经登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            // 未登录 数据放入cookie 中。
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        // 需要保存skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response){
        // 根据userId 判断
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            // 从redis --- mysql
            // 先判断cookie是否有购物车
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            List<CartInfo> cartList = null;
            if (cartListCK!=null && cartListCK.size()>0){
                // 有：合并，将cookie购物车删除
                cartList = cartService.mergeToCartList(cartListCK,userId);
                // 删除
                cartCookieHandler.deleteCartCookie(request,response);
            } else {
                // 没有：直接显示redis - mysql。
                cartList = cartService.getCartList(userId);
            }
            request.setAttribute("cartList",cartList);
        }else { 
            // cookie 中查询
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }

        return "cartList";
    }
    /**
     * 获取选中的状态
     */
    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        // 获取用户Id ，来判断用户是否登录
        String userId = (String) request.getAttribute("userId");

        if (userId!=null){
            // 登录了，记录当前的商品状态
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            // 操作cookie
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        // 被选中的商品进行结算 【redis = cookie】
        String userId = (String) request.getAttribute("userId");
        // 选中的合并
        List<CartInfo> cookieHandlerCartList  = cartCookieHandler.getCartList(request);
        if (cookieHandlerCartList!=null && cookieHandlerCartList.size()>0){
            // 符合什么条件进行合并 "skuId 相等, isChecked=1"
            cartService.mergeToCartList(cookieHandlerCartList,userId);
            // 合并被选中的商品，然后删除！
            cartCookieHandler.deleteCartCookie(request,response);
        }
        // 重定向到订单控制器
        return "redirect://order.gmall.com/trade";
    }
}
