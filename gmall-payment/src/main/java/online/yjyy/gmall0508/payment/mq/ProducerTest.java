package online.yjyy.gmall0508.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;

import javax.jms.*;

public class ProducerTest {

    // 发送消息 Producer
    public static void main(String[] args) throws JMSException {

        // 创建消息队列工厂 -- mybatis --
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.80.100:61616");
        // 创建一个连接
        Connection connection = activeMQConnectionFactory.createConnection();
        // 打开连接
        connection.start();
        // 创建session
        // boolean ： 是否开启事务 false ，true。
        // int : 给的一个枚举
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        // 创建队列
        Queue queue = session.createQueue("order");
        // 创建一个消息提供者Producer
        MessageProducer producer = session.createProducer(queue);
        // 创建消息对象
//        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
//        activeMQTextMessage.setText("饿了么？");
        ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
        mapMessage.setString("orderId","10001");
        mapMessage.setString("result","success");
        // 发送消息
        producer.send(mapMessage);
        // 做关闭操作
//        session.commit();
        producer.close();
        session.close();
        connection.close();



    }
}
