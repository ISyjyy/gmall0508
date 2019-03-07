package online.yjyy.gmall0508.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.enums.ProcessStatus;
import online.yjyy.gmall0508.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;
    // 使用了activeMQ工具类，工具类中已经有了一个消息监听器工厂。
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void  consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        // 从消息提供者中取得消息
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        // result = success 支付成功
        if ("success".equals(result)){
            // 修改订单状态
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            // 发送一个通知告诉库存
            orderService.sendOrderStatus(orderId);
            // 变成待状态
            orderService.updateOrderStatus(orderId, ProcessStatus.WAITING_DELEVER);
        }
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void  consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 从消息提供者中取得消息
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        // result = success 支付成功
        if ("DEDUCTED".equals(status)){
            // 以减少库存
            System.out.println("--- 消费减库存！");
            orderService.updateOrderStatus(orderId, ProcessStatus.DELEVERED);
        }
    }
}
