package online.yjyy.gmall0508.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.PaymentInfo;
import online.yjyy.gmall0508.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    // 引入paymentService
    @Reference
    private PaymentService paymentService;

    // 验证是否支付成功
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void  consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 从消息提供者中取得消息
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");
        // 调用checkPayment();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        System.out.println("开始检查");
        //调用checkPayment 看是否支付成功
        boolean flag = paymentService.checkPayment(paymentInfo);
        // 什么时候停止调用？ true:停止调用，什么时候继续调用false ：继续调用[次数-1];
        if (!flag && checkCount>0){
            System.out.println("再次发送 checkCount="+checkCount);
            // 继续调用
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }

    }
}
