package online.yjyy.gmall0508.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.OrderInfo;
import online.yjyy.gmall0508.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

//     // 每分钟的第五秒执行一次
//    @Scheduled(cron = "5 * * * * ?")
//    public void  test1(){
//        System.out.println("ThreadTest1 ====== "+ Thread.currentThread());
//    }
//    // 每隔五秒执行一次的方法
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void  test2(){
//        System.out.println("ThreadTest2 ====== "+ Thread.currentThread());
//    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void  checkOrder(){
        // 扫描一次过期订单，处理过期的订单 ？
        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();
        // 循环处理
        for (OrderInfo orderInfo : expiredOrderList) {
            // orderInfo 过期订单对象
            orderService.execExpiredOrder(orderInfo);
        }
    }
}
