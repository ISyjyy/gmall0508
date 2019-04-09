package online.yjyy.gmall0508.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import online.yjyy.gmall0508.bean.OrderInfo;
import online.yjyy.gmall0508.bean.PaymentInfo;
import online.yjyy.gmall0508.bean.enums.PaymentStatus;
import online.yjyy.gmall0508.payment.config.AlipayConfig;
import online.yjyy.gmall0508.service.OrderService;
import online.yjyy.gmall0508.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("index")
    public String index( HttpServletRequest request){
        // 获取orderId
        String orderId = request.getParameter("orderId");
        // 根据订单orderId 查询订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 存储orderId
        request.setAttribute("orderId",orderId);
        HttpSession session=request.getSession();
        session.setAttribute("orderId",orderId);
        // 存储总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }

    @RequestMapping(value = "alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment( HttpServletRequest request,HttpServletResponse response){

        String orderId = request.getParameter("orderId");
        // 保存交易记录 mysql -- payment_info
        // 创建paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        // 数据来源：订单信息
        // 调用根据订单Id查询订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        // 测试信息 ： 订单明细的名称
        paymentInfo.setSubject("有钱随便买！");
        paymentService.savyPaymentInfo(paymentInfo);

        // 生成一个二维码
        // sdk 中已经有AlipayClient 工具类！
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        //
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 制作一个map
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("subject",paymentInfo.getSubject());
        map.put("total_amount",paymentInfo.getTotalAmount());
        // 将map 转换为字符串
        String jsonMap = JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonMap);
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        return form;
    }

    // 测试同步回调
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(HttpServletRequest request){
        HttpSession session=request.getSession();
        String orderId = (String) session.getAttribute("orderId");
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        // select * from paymentInfo where orderId = ?
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        // 主动 查询支付结果 PaymentInfo 对象 中才会有out_trade_no
        boolean flag = paymentService.checkPayment(paymentInfo);
        return "redirect:"+AlipayConfig.return_order_url;
    }

    // 异步回调 -- 业务逻辑。 支付宝，通过外网 -- [能访问internet]
    @RequestMapping("callback/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        // 将异步回调通知的参数封装到一个paramMap集合中

        boolean signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type); //调用SDK验证签名
        if(signVerified){
            //    TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //   trade_status == TRADE_SUCCESS TRADE_FINISHED
            //   如果该订单未支付，且不能关闭才是成功！ paymentInfo 表中记录支付信息 【out_trade_no】
            String trade_status = paramMap.get("trade_status");
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){

                String out_trade_no = paramMap.get("out_trade_no");
                // 根据out_trade_no 查询paymentInfo 的支付状态  select * from paymentInfo where out_trade_no = ?
                PaymentInfo paymentInfoQuery  = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);

                // 获取paymentInfo的支付状态
                if (paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED || paymentInfo.getPaymentStatus()==PaymentStatus.PAID){
                    return "fail";
                }
                // 做更新 PaymentStatus.PAID
                // update paymentInfo set payment_status = PaymentStatus.PAID where id = paymentInfo.getId();
                // // update paymentInfo set payment_status = PaymentStatus.PAID where out_trade_no = out_trade_no;
                // 创建一个更新的对象
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(paramMap.toString());

                paymentService.updatePaymentInfo(paymentInfo);
                // paymentService.updatePaymentInfoByOutTradeNo(out_trade_no,paymentInfo);
                paymentService.sendPaymentResult(paymentInfo,"success");
            /* //查看是否支付成功
                HttpSession session=request.getSession();
                String orderId = (String) session.getAttribute("orderId");
               // PaymentInfo paymentInfoQuery = new PaymentInfo();
                paymentInfoQuery.setOrderId(orderId);
                // select * from paymentInfo where orderId = ?
                PaymentInfo paymentInfo2 = paymentService.getPaymentInfo(paymentInfoQuery);
                // 主动 查询支付结果 PaymentInfo 对象 中才会有out_trade_no
                boolean flag = paymentService.checkPayment(paymentInfo2);*/
                return "success";
            }else {
                return "fail";
            }

        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "fail";
        }
    }

    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){
        // 手动发送通知
        paymentService.sendPaymentResult(paymentInfo,result);
        return "success";
    }
    // 主动查询支付结果
    // queryPaymentResult?orderId =104
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        // 通过orderId 查询paymentInfo对象
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        // select * from paymentInfo where orderId = ?
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        // 主动 查询支付结果 PaymentInfo 对象 中才会有out_trade_no
        boolean flag = paymentService.checkPayment(paymentInfo);
        return ""+flag;
    }


}
