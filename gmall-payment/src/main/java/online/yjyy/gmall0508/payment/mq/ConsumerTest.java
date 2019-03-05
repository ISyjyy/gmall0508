package online.yjyy.gmall0508.payment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;

public class ConsumerTest {

     // psvm
    public static void main(String[] args) throws JMSException {
        // 创建工厂 默认的用户名，密码：admin
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://192.168.67.205:61616");
        // 创建一个连接
        Connection connection = activeMQConnectionFactory.createConnection();
        // 打开连接
        connection.start();
        // 创建session
        // boolean ： 是否开启事务 false ，true。
        // int : 给的一个枚举
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 创建队列
        Queue queue = session.createQueue("order");
        // 创建消费者
        MessageConsumer consumer = session.createConsumer(queue);
        // 消费信息 -- 消息监听器
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                // 做一个判断  ActiveMQTextMessage
//                if (message instanceof TextMessage){
//                    try {
//                        String text = ((TextMessage) message).getText();
//                        System.out.println(text);
//                    } catch (JMSException e) {
//                        e.printStackTrace();
//                    }
//                }
                if (message instanceof MapMessage){
                    try {

                        String result = ((MapMessage) message).getString("result");
                        String orderId = ((MapMessage) message).getString("orderId");
                        System.out.println(result+"\t"+orderId);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        consumer.close();
        session.close();
        connection.close();


    }
}
