package com.xiaochen.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaochen.rabbitmq.entity.Order;
import com.xiaochen.rabbitmq.entity.Packaged;
import jdk.internal.cmm.SystemResourcePressureImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqSpringApplicationTests {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testAdmin() throws Exception{
        rabbitAdmin.declareExchange(new DirectExchange("test.direct",false,false));

        rabbitAdmin.declareExchange(new TopicExchange("test.topic",false,false));

        rabbitAdmin.declareExchange(new FanoutExchange("test.fanout",false,false));

        rabbitAdmin.declareQueue(new Queue("test.direct.queue",false));

        rabbitAdmin.declareQueue(new Queue("test.topic.queue",false));

        rabbitAdmin.declareQueue(new Queue("test.fanout.queue",false));

        rabbitAdmin.declareBinding(
                BindingBuilder
                        .bind(new Queue("test.queue",false))
                        .to(new TopicExchange("test.exchange",false,false))
                        .with("2131"));

        rabbitAdmin.declareBinding(new Binding("test.direct.queue",Binding.DestinationType.QUEUE,"test.direct","direct",new HashMap<>()));
        rabbitAdmin.declareBinding(new Binding("test.topic.queue",Binding.DestinationType.QUEUE,"test.topic","topic",new HashMap<>()));
        rabbitAdmin.declareBinding(new Binding("test.fanout.queue",Binding.DestinationType.QUEUE,"test.fanout","fanout",new HashMap<>()));




    }
    @Test
    public void  testSendMessage() throws Exception{
        MessageProperties messageProperties=new MessageProperties();
        messageProperties.getHeaders().put("desc","信息描述。。。");
        messageProperties.getHeaders().put("type","自定义消息类型。。。");
        messageProperties.setContentType("text/plain");
        Message message =new Message("Hello RabbirMQ!!!".getBytes(),messageProperties);
        rabbitTemplate.convertAndSend("topic001", "spring.amqp", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                System.out.println("===========添加额外的设置===========");
                messageProperties.getHeaders().put("desc","额外修改的信息描述。。。");
                messageProperties.getHeaders().put("attr","额外新加的属性。。。");
                return message;
            }
        });
    }
    @Test
    public void testSendJsonMessage() throws Exception{
        Order order =new Order();
        order.setId("001");
        order.setName("消息订单");
        order.setContent("描述信息");
        ObjectMapper objectMapper=new ObjectMapper();
        String json=objectMapper.writeValueAsString(order);
        System.out.println("order 4 json : " + json);
        MessageProperties messageProperties=new MessageProperties();
        //这里一定要修改contentType 为application/json
        messageProperties.setContentType("application/json");
        Message message=new Message(json.getBytes(),messageProperties);
        rabbitTemplate.send("topic001","spring.order",message);
    }
    @Test
    public void testSendJavaMessage() throws Exception{
        Order order =new Order();
        order.setId("001");
        order.setName("消息订单");
        order.setContent("描述信息");
        ObjectMapper objectMapper=new ObjectMapper();
        String json = objectMapper.writeValueAsString(order);
        System.out.println("order 4 json : " + json);

        MessageProperties messageProperties=new MessageProperties();
        //这里一定要修改contentType 为application/json
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__","com.xiaochen.rabbitmq.entity.Order");
        Message message=new Message(json.getBytes(),messageProperties);
        rabbitTemplate.send("topic001","spring.order",message);
    }
    @Test
    public void testSendMappingMessage() throws Exception{

        ObjectMapper objectMapper=new ObjectMapper();

        Order order =new Order();
        order.setId("001");
        order.setName("消息订单");
        order.setContent("描述信息");
        String json1 = objectMapper.writeValueAsString(order);
        System.out.println("order 4 json : " + json1);

        MessageProperties messageProperties = new MessageProperties();
        //这里一定要修改contentType 为application/json
        messageProperties.setContentType("application/json");
        messageProperties.getHeaders().put("__TypeId__","order");
        Message message=new Message(json1.getBytes(),messageProperties);
        rabbitTemplate.send("topic001","spring.order",message);

        Packaged packaged =new Packaged();
        packaged.setId("002");
        packaged.setName("消息订单");
        packaged.setDescription("包裹描述信息");
        String json2=objectMapper.writeValueAsString(packaged);
        System.out.println("order 5 json : " + packaged);

        MessageProperties messageProperties2 = new MessageProperties();
        //这里一定要修改contentType 为application/json
        messageProperties2.setContentType("application/json");
        messageProperties2.getHeaders().put("__TypeId__","packaged");
        Message message2 =new Message(json2.getBytes(),messageProperties2);
        rabbitTemplate.send("topic001","spring.packaged",message2);
    }

    @Test
    public void testSendExtConverterMessage() throws Exception{
//        byte[] body = Files.readAllBytes(Paths.get("d:/002_books", "picture.png"));
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setContentType("image/png");
//        messageProperties.getHeaders().put("extName", "png");
//        Message message = new Message(body, messageProperties);
//        rabbitTemplate.send("", "image_queue", message);

        byte[] body = Files.readAllBytes(Paths.get("d:/002_books", "Java企业设计模式.pdf"));
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/pdf");
        Message message = new Message(body, messageProperties);
        rabbitTemplate.send("", "pdf_queue", message);
    }

}
