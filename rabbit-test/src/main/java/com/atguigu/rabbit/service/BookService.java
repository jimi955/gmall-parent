package com.atguigu.rabbit.service;

import com.atguigu.rabbit.bean.Book;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * 接收消息的两种方式
 */
@Service
public class BookService {

    @RabbitListener(queues = "atguigu.news")  // 监听队列
    public void receive(Book book,Message message, Channel channel) throws IOException {
        System.out.println("收到消息：" + book);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = "atguigu")
    public void receive02(Message message) {
        System.out.println(message.getBody()); // message内容
        System.out.println(message.getMessageProperties()); // message头信息
    }
}
