package online.yjyy.gmall0508.order.Controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.*;
import online.yjyy.gmall0508.bean.enums.OrderStatus;
import online.yjyy.gmall0508.bean.enums.ProcessStatus;
import online.yjyy.gmall0508.config.LoginRequire;
import online.yjyy.gmall0508.service.CartService;
import online.yjyy.gmall0508.service.ManageService;
import online.yjyy.gmall0508.service.OrderService;
import online.yjyy.gmall0508.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Reference
    private ManageService manageService;


    public OrderController() {
    }

    @RequestMapping("trade")
    @LoginRequire(autoRedirect = true)

    public String tarde(HttpServletRequest request, Model model){
        // 获取用户Id
        String userId = (String) request.getAttribute("userId");
        // 送货清单地址
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);

        // 送货清单：数据来源于 cartService
        List<CartInfo> cartCheckedList =  cartService.getCartCheckedList(userId);

        // 创建一个OrderDetail 集合
        ArrayList<OrderDetail> orderDetailList = new ArrayList();
        // 应该给orderDetail 赋值
        for (CartInfo cartInfo : cartCheckedList) {
            // 属性拷贝
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }


        // 引出订单：
        OrderInfo orderInfo = new OrderInfo();
        // 将订单明细给主订单
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();


        // 保存一个totalAmount 表示订单总价 [订单中所有的商品明细]
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        // 保存orderDetail集合
        request.setAttribute("orderDetailList",orderDetailList);

        request.setAttribute("userAddressList",userAddressList);

        // 获取流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    /**
     * 下订单，支付
     * @return
     */
    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = true)
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        // 将订单数据 添加到数据库中 orderInfo ,oderDetail
        // 取得userId
        String userId = (String) request.getAttribute("userId");

        // 校验 获取流水号
        String tradeNo = request.getParameter("tradeNo");
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if (!result){
            // 验证失败！
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }

        // 校验库存 ： 每个订单都需要校验
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // skuInfo.price ===orderDetail.getorderPrice   验价格
            // 获取skuInfo.price
                     /*  SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
                      if (skuInfo!=null){
                          if (!skuInfo.getPrice().equals(orderDetail.getOrderPrice())){
                                request.setAttribute("errMsg","价格有变动，请重新下单!");
                                // 重新加载redis中购物车数据
                              cartService.loadCartCache(userId);
                                return "tradeFail";
                            }
                        }*/
            // 调用验库存
            boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            // 校验失败
            if (!flag){
                request.setAttribute("errMsg","库存不足，请重新下单!");
                return "tradeFail";
            }
        }
        // 验价： 购物车的价格是否正确！ 购物车商品的价格，跟skuInfo.price 是否一致！ true ，过！ false 不过！
        orderInfo.setUserId(userId);


        // 订单状态，进程状态赋值
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        // 计算一下总金额
        orderInfo.sumTotalAmount();
        String orderId = orderService.saveOrder(orderInfo);
        // 如果校验成功则删除redis
        orderService.delTradeCode(userId);
        // mysql --- 伪删除！
        // 支付的时候，需要根据orderId
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(String orderId,String wareSkuMap,HttpServletRequest request){
        // 声明一个集合接收 【调用服务层的方法得到子订单List<OrderInfo>】
        List<OrderInfo> subOrderInfoList  =  orderService.splitOrder(orderId,wareSkuMap);
        List<Map> wareMapList=new ArrayList<>();
        // 遍历子订单集合返回字符串
        for (OrderInfo orderInfo : subOrderInfoList) {
            // orderInfo 主订单
            Map map = orderService.initWareOrder(orderInfo);
            // 将map 集合添加到一个list集合中
            wareMapList.add(map);
        }
        return JSON.toJSONString(wareMapList);
    }



}
