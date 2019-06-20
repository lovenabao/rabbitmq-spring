package com.xiaochen.rabbitmq.config;

import com.xiaochen.rabbitmq.adapter.MessageDelegate;
import com.xiaochen.rabbitmq.convert.ImageMessageConverter;
import com.xiaochen.rabbitmq.convert.PDFMessageConverter;
import com.xiaochen.rabbitmq.convert.TextMessageConverter;
import com.xiaochen.rabbitmq.entity.Order;
import com.xiaochen.rabbitmq.entity.Packaged;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * rabbitmq配置类
 * @author shkstart
 * @create 2019-05-14 16:04
 */
@Configuration
@ComponentScan({"com.xiaochen.rabbitmq.*"})
public class RabbitmqConfig {
    /**
     * 先写connection工厂
     * @return
     */
    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory =new CachingConnectionFactory();
        connectionFactory.setAddresses("120.78.4.81:5672");
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    /**
     *  RabbitAdmin管理  rabbitAdmin.setAutoStartup(true) ：启动rabbitmq
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin rabbitAdmin =new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * 注册交换机
     * @return
     */
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange("topic001",true,false);
    }

    /**
     * 注册队列
     * @return
     */
    @Bean
    public Queue queue(){
        return new Queue("queue001",true);
    }

    /**
     * 进行绑定
     * @return
     */
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with("spring.*");
    }


    @Bean
    public Queue queue_image() {
        return new Queue("image_queue", true); //队列持久
    }

    @Bean
    public Queue queue_pdf() {
        return new Queue("pdf_queue", true); //队列持久
    }

    /**
     * 发送消息的模板
     * @param connectionFactory
     * @return
     */
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
        return rabbitTemplate;
    }

    /**
     * 简单消息监听
     * @param connectionFactory
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer listenerContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer simpleMessageListenerContainer=new SimpleMessageListenerContainer(connectionFactory);
        //都需要监听那几个队列
        simpleMessageListenerContainer.setQueues(queue(),queue_image(),queue_pdf());
        //设置最小有几个消费者
        simpleMessageListenerContainer.setConcurrentConsumers(1);
        //设置最多有几个消费者
        simpleMessageListenerContainer.setMaxConcurrentConsumers(5);
        //是否重回队列 一般设置为false
        simpleMessageListenerContainer.setDefaultRequeueRejected(false);
        //消息的签收模式
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        //消费端的标签策略
        simpleMessageListenerContainer.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String queue) {
                return queue+"_"+ UUID.randomUUID().toString();
            }
        });

       /**
        * //设置消息监听，如果一个消息过来就会到 onMessag
        * simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
               String s =new String(message.getBody());
                System.out.println("==========消费者========" + s);
            }
        });*/

       /**
        * MessageListenerAdapter : 消息监听适配器
        //1. 适配器方式，默认是有自己的方法名字的：handleMessage
        //可以自己指定一个方法的名字：consumeMessage
        //也可以添加一个转换器：从字节数组转化为 string
        MessageListenerAdapter messageListenerAdapter =new MessageListenerAdapter(new MessageDelegate());
        //指定自己方法的名字
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        //转换器
        messageListenerAdapter.setMessageConverter(new TextMessageConverter());
        //加入监听
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
        **/

        /**
         * 2.适配器方式:我们的队列名称和方法名称也可以进行一一的适配
         *   MessageListenerAdapter messageListenerAdapter =new MessageListenerAdapter(new MessageDelegate());
         *   Map<String,String> queueOrTagToMethodName =new HashMap<>();
         *   queueOrTagToMethodName.put("queue001","method2");
         *   messageListenerAdapter.setQueueOrTagToMethodName(queueOrTagToMethodName);
         *   simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
         *
         */

        /**
         * 1.1 支持json格式的转化器
         */
        /*MessageListenerAdapter messageListenerAdapter=new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter=new Jackson2JsonMessageConverter();
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/

        /**
         * 1.2 DefaultJackson2JavaTypeMapper && Jackson2JsonMessageConverter  支持java对象的转化器
         */
        /*MessageListenerAdapter messageListenerAdapter=new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter=new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper javaTypeMapper=new DefaultJackson2JavaTypeMapper();
        jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/

        /**
         * 1.3 DefaultJackson2JavaTypeMapper && Jackson2JsonMessageConverter  支持java对象多映射转换
         */
        /*MessageListenerAdapter messageListenerAdapter=new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter=new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper javaTypeMapper=new DefaultJackson2JavaTypeMapper();
        Map<String,Class<?>> idClassMapping =new HashMap<>();
        idClassMapping.put("order", Order.class);
        idClassMapping.put("packaged", Packaged.class);
        javaTypeMapper.setIdClassMapping(idClassMapping);
        jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);*/


        /**
         * 1.4  多种类型转换
         */
        MessageListenerAdapter messageListenerAdapter =new MessageListenerAdapter(new MessageDelegate());
        messageListenerAdapter.setDefaultListenerMethod("consumeMessage");
        //全局转换器
        ContentTypeDelegatingMessageConverter messageConverter=new ContentTypeDelegatingMessageConverter();
        //字符串类型转换
        TextMessageConverter textMessageConverter=new TextMessageConverter();
        messageConverter.addDelegate("text",textMessageConverter);
        messageConverter.addDelegate("html/text",textMessageConverter);
        messageConverter.addDelegate("xml/text",textMessageConverter);
        messageConverter.addDelegate("text/plain",textMessageConverter);
        //json类型转换
        Jackson2JsonMessageConverter jackson2JsonMessageConverter=new Jackson2JsonMessageConverter();
        messageConverter.addDelegate("json",jackson2JsonMessageConverter);
        messageConverter.addDelegate("application/json",jackson2JsonMessageConverter);
        //图片格式转换
        ImageMessageConverter imageMessageConverter=new ImageMessageConverter();
        messageConverter.addDelegate("image/png",imageMessageConverter);
        messageConverter.addDelegate("image",imageMessageConverter);
        //pdf格式转换
        PDFMessageConverter pdfMessageConverter=new PDFMessageConverter();
        messageConverter.addDelegate("application/pdf",pdfMessageConverter);

        messageListenerAdapter.setMessageConverter(messageConverter);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
        return simpleMessageListenerContainer;
    }

}
