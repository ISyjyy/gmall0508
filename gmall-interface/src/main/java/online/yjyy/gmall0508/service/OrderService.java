package online.yjyy.gmall0508.service;

import online.yjyy.gmall0508.bean.OrderInfo;
import online.yjyy.gmall0508.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    // 保存订单方法的参数参数 orderInfo ,orderDetail
    String  saveOrder(OrderInfo orderInfo);

    // 获取流水号
    String getTradeNo(String userId);

    // 校验流水号
    boolean checkTradeCode(String userId, String tradeCodeNo);

    // 删除流水号
    void delTradeCode(String userId);

    // 验库存接口
    boolean checkStock(String skuId, Integer skuNum);
    // 根据订单Id查订单信息
    OrderInfo getOrderInfo(String orderId);
    // 发送通知给库存
    void sendOrderStatus(String orderId);

//更新订单状态
    void updateOrderStatus(String orderId, ProcessStatus paid);
    // 发送通知给库存
    // 查询过期订单
    List<OrderInfo> getExpiredOrderList();
    // 处理过期的订单
    void execExpiredOrder(OrderInfo orderInfo);
    // 将每个orderInfo 转换成Map
    Map initWareOrder(OrderInfo orderInfo);
    // 获取子订单集合
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);

    //查询所有的订单状态
    List<OrderInfo> queryOrderList(OrderInfo orderInfo);

    List queryOrderMap(Long userId);

    int delOrderInfoById(String orderId);
}
