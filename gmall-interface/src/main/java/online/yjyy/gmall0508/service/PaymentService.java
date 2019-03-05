package online.yjyy.gmall0508.service;


import online.yjyy.gmall0508.bean.PaymentInfo;

public interface PaymentService {
    void  savyPaymentInfo(PaymentInfo paymentInfo);
    // 根据outtradeNo 查询PaymentInfo
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);
    // 更新paymentInfo
    void updatePaymentInfo(PaymentInfo paymentInfo);
    // 根据out_trade_no更新
    void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfo);
    //发送成功通知
    void sendPaymentResult(PaymentInfo paymentInfo,String result);
    // 查询支付宝的支付状态
    boolean checkPayment(PaymentInfo paymentInfoQuery);
    // 定义延迟队列接口
    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);
    // 关闭paymentInfo的状态
    void closePayment(String orderId);

}
