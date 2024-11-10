package com.luoyi.implatform.rabbitmq.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq订阅模式
 * rabbitmq配置类，用于读取配置文件
 */
@Configuration
public class RabbitMQConfig {
    /**
     * @Value ":" 后面的为默认值
     */
    @Value("${mail.exchange:mail-exchange}")
    private String mailExchange;

    @Value("${mail.queue.verifyCode:mail-queue-verifyCode}")
    private String mailQueueVerifyCode;

    /**
     * route:路由键
     */
    @Value("${mail.route.verifyCode:mail-route-verifyCode}")
    private String mailRouteVerifyCode;

    @Value("${mail.queue.feedback:mail-queue-feedback}")
    private String mailQueueFeedback;

    @Value("${mail.route.feedback:mail-route-feedback}")
    private String mailRouteFeedback;


    @Bean
    DirectExchange mailExchange(){
        return new DirectExchange(mailExchange,true,false);
    }

    /**
     * 验证码消息队列
     * @return
     */
    @Bean
    Queue mailQueueVerifyCode(){
        return new Queue(mailQueueVerifyCode,true);
    }

    /**
     * 验证码消息队列与交换机进行绑定
     * @return
     */
    @Bean
    Binding mailQueueVerifyCodeBinding(){
        return BindingBuilder.bind(mailQueueVerifyCode()).to(mailExchange()).with(mailRouteVerifyCode);
    }

    /**
     * 反馈消息队列
     * @return
     */
    @Bean
    Queue mailQueueFeedback(){
        return new Queue(mailQueueFeedback,true);
    }

    /**
     * 反馈消息队列和交换机进行绑定
     * @return
     */
    @Bean
    Binding mailQueueFeedbackBinding(){
        return BindingBuilder.bind(mailQueueFeedback()).to(mailExchange()).with(mailRouteFeedback);
    }

}
