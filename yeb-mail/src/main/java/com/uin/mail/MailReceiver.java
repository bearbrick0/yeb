package com.uin.mail;


import com.rabbitmq.client.Channel;
import com.uin.server.pojo.Employee;
import com.uin.server.pojo.MailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;

/**
 * \* Created with IntelliJ IDEA.
 * \* @author wanglufei
 * \* Date: 2021年08月18日 14:31
 * \* Description:  邮件发给谁  谁发的 消息接受者
 * \
 */
@Component
public class MailReceiver {

    public static final Logger logger = LoggerFactory.getLogger(MailReceiver.class);

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private MailProperties mailProperties;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = MailConstants.MAIL_QUEUE_NAME)// 监听 队列
    /**
     * Message
     */
    public void handler(Message message, Channel channel) {// Channel 信道
        // 获取员工类   getPayload消息负载
        Employee employee = (Employee) message.getPayload();
        System.out.println("MailReceiver: employee=" + employee);

        MessageHeaders headers = message.getHeaders();
        // 消息序号; tag 手动确认的时候返回
        long tag = (long) headers.get(AmqpHeaders.DELIVERY_TAG);
        System.out.println("tag=" + tag);
        String msgId = (String) headers.get("spring_returned-message_correlation");

        System.out.println("msgId=" + msgId);
        /**
         * 使用redis存储hash的数据结构存储<key,value>
         *     <msgId,ok>
         */
        HashOperations hash = redisTemplate.opsForHash();
        try {
            /**
             * 看 redis hashOperations 里面包不包含 对应的 msgId
             */
            if (hash.entries("mail_log").containsKey(msgId)) {
                /**
                 * redis中包含key，说明消息已经被消费
                 */
                logger.error("邮件已经发送过了======>{}" + msgId);
                /**
                 * 手动确认消息
                 *  tag：消息的序号
                 *  multiple: 是否确认多条，false 确认1条
                 */
                channel.basicAck(tag, false);
                return;
            }
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg);
            /**
             * 发件人
             */
            helper.setFrom(mailProperties.getUsername());
            /**
             * 收件人
             */
            helper.setTo(employee.getEmail());
            /**
             * 主题
             */
            helper.setSubject("入职邮件");
            /**
             * 发送日期
             */
            helper.setSentDate(new Date());
            /**
             * 邮件内容
             */
            Context context = new Context();
            context.setVariable("name", employee.getName());
            context.setVariable("posName", employee.getPosition().getName());
            context.setVariable("joblevelName", employee.getJoblevel().getName());
            context.setVariable("departmentName", employee.getDepartment().getName());
            /**
             * 解析前端页面
             */
            String mail = templateEngine.process("main", context);
            helper.setText(mail, true);
            /**
             * 发送邮件
             */
            javaMailSender.send(msg);
            logger.info("邮件发送成功");
            /**
             * 将消息id存入redis
             */
            hash.put("mail_log", msgId, "ok");
            System.out.println("MailReceiver:redis--->msgId=" + msgId);
            /**
             * 手动确认消息
             */
            channel.basicAck(tag, false);
        } catch (MessagingException | IOException e) {
            try {
                /**
                 * 手动确认
                 *  tag 消息的序号
                 *  multiple 取消批量
                 *  requeue 需要重新入队
                 */
                channel.basicNack(tag, false, true);
            } catch (IOException ioException) {
                logger.error("消息确认失败=====>{}", ioException.getMessage());
            }
            logger.error("MailReceiver + 邮件发送失败========{}", e.getMessage());
        }

    }
}
