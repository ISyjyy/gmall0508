package online.yjyy.gmall0508.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.OrderDetail;
import online.yjyy.gmall0508.bean.OrderInfo;
import online.yjyy.gmall0508.bean.enums.ProcessStatus;
import online.yjyy.gmall0508.config.ActiveMQUtil;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.order.mapper.OrderDetailMapper;
import online.yjyy.gmall0508.order.mapper.OrderInfoMapper;
import online.yjyy.gmall0508.service.OrderService;
import online.yjyy.gmall0508.service.PaymentService;
import online.yjyy.gmall0508.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        // 创建时间，以及过期时间 2018-9-27
        orderInfo.setCreateTime(new Date());
        // 获取日历类
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        //  2018-9-28
        orderInfo.setExpireTime(calendar.getTime());
        // 设置第三方交易编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        // orderInfo 表结构
        orderInfoMapper.insertSelective(orderInfo);
        // 订单详情 orderDetail
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 设置orderId
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        String orderId = orderInfo.getId();
        // 返回orderId
        return orderId;
    }
    // 生成一个流水号：
    public String getTradeNo(String userId){
        // 生成号
        String tradeCode = UUID.randomUUID().toString();
        // 放入redis
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        //
        jedis.setex(tradeNoKey,10*60,tradeCode);

        jedis.close();
        return tradeCode;
    }

    // 校验：页面提交到后台的流水号，跟redis 中的流水号进行校验
    public boolean checkTradeCode(String userId,String tradeCodeNo){
        // 取得redis 中的数据
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode  = jedis.get(tradeNoKey);
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else {
            return false;
        }
    }
    // 删除流水号
    public void delTradeCode(String userId){
        // 放入redis
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";

        jedis.del(tradeNoKey);

        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        // 调用 库存系统接口
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return true;
        }else {
            return false;
        }
    }
    @Override
    public OrderInfo getOrderInfo(String orderId) {
//        OrderInfo orderInfo = new OrderInfo();
//        orderInfo.setId(orderId);
//        orderInfoMapper.selectOne(orderInfo);
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        // 根据orderId 查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        // 添加到orderInfo 中
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }


    @Override
    public void updateOrderStatus(String orderId, ProcessStatus paid) {
        // 创建一个OrderInfo
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        // orderInfo.setOrderStatus(OrderStatus.PAID);
        orderInfo.setProcessStatus(paid);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        // 修改状态
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        // 创建工厂
        Connection connection = activeMQUtil.getConnection();
        // 符合减库存的json 字符串
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            // 创建提供者
            MessageProducer producer = session.createProducer(order_result_queue);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(orderJson);
            // 发送消息
            producer.send(activeMQTextMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private String initWareOrder(String orderId) {
        // 根据orderId 查询对应的订单信息
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }
//减少库存json
    public   Map initWareOrder(OrderInfo orderInfo) {
        // 创建map
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试减库存。");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        // 仓库Id 给拆单预留的。
        map.put("wareId",orderInfo.getWareId());
        // 少一个orderDetail。
        // details:[{skuId:101,skuNum:1,skuName:’小米手64G’},{skuId:201,skuNum:1,skuName:’索尼耳机’}]
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        List detailList = new ArrayList();
        // 遍历该集合
        for (OrderDetail orderDetail : orderDetailList) {
            // 存储一个map 集合
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
        return map;
    }

    //拆单
    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList  = new ArrayList<>();
        // 先通过orderId 查询到原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        // wareSkuMap [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
        // 循环当前的map集合
        for (Map map : mapList) {
            String wareId = (String) map.get("wareId");
            List<String> skuIds = (List<String>) map.get("skuIds");
            // 创建新的子订单对象
            OrderInfo subOrderInfo  = new OrderInfo();
            // 将原始订单的属性拷贝给新的子订单
            try {
                BeanUtils.copyProperties(subOrderInfo,orderInfoOrigin);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            // 防止子订单主键重复
            subOrderInfo.setId(null);
            // 设置父订单Id
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
            // 赋值仓库Id
            subOrderInfo.setWareId(wareId);

            // 声明一个子订单集合
            List<OrderDetail> subOrderDetailList = new ArrayList<>();
            // 获取子订单明细
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            for (OrderDetail orderDetail : orderDetailList) {
                // 循环skuIds
                for (String skuId : skuIds) {
                    if (skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null); //主键自增
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }
            // 给新的子订单赋值订单明细
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            // 总价
            subOrderInfo.sumTotalAmount();
            // 保存新的子订单
            saveOrder(subOrderInfo);
            // 将新的子订单添加到集合中
            subOrderInfoList.add(subOrderInfo);
        }
        // 将原始订单变为拆分
        updateOrderStatus(orderId,ProcessStatus.SPLIT);

        return subOrderInfoList;
    }

    @Override
    public List<OrderInfo> getExpiredOrderList() {
        // 过期时间《当前时间
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("processStatus",ProcessStatus.UNPAID).andLessThan("expireTime",new Date());
        List<OrderInfo> orderInfos = orderInfoMapper.selectByExample(example);
        return orderInfos;
    }

    @Override
    @Async
    public void execExpiredOrder(OrderInfo orderInfo) {
        //  最重要的更新状态
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        //  处理一下paymentInfo 的状态也需要关闭。
        paymentService.closePayment(orderInfo.getId());
    }

}
