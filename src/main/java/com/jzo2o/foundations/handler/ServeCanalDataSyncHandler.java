package com.jzo2o.foundations.handler;

import com.jzo2o.canal.listeners.AbstractCanalRabbitMqMsgListener;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.foundations.constants.IndexConstants;
import com.jzo2o.foundations.model.domain.ServeSync;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ServeCanalDataSyncHandler extends AbstractCanalRabbitMqMsgListener<ServeSync> {

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    /**
     * RabbitMQ消息监听器，用于处理来自指定队列的消息
     * 该监听器绑定到名为"anal-mq-jzo2o-foundations"的队列，
     * 从"exchange.canal-jzo2o"交换机接收路由键为"canal-mq-jzo2o-foundations"的消息
     *
     * @param message 接收到的RabbitMQ消息对象，包含需要处理的数据
     * @throws Exception 当消息解析过程中发生错误时抛出异常
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name="canal-mq-jzo2o-foundations"),arguments={@Argument(name="x-single-active-consumer", value = "true", type = "java.lang.Boolean") },
            exchange = @Exchange(name = "exchange.canal-jzo2o",type = ExchangeTypes.TOPIC),
            key = "canal-mq-jzo2o-foundations"),
            concurrency = "1"
    )
    public void onMessage(Message message) throws Exception {
        // 解析并处理接收到的消息
        parseMsg(message);
    }


    /**
     * 批量保存服务同步数据到ES
     * @param data 待保存的服务同步数据列表
     */
    @Override
    public void batchSave(List<ServeSync> data) {
        // 执行批量插入操作
        Boolean b = elasticSearchTemplate.opsForDoc().batchInsert(IndexConstants.SERVE, data);
        if( !b){
            // 插入失败时等待1秒后抛出异常
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("同步数据到ES失败");
        }
    }


        /**
     * 批量删除指定ID的数据
     *
     * @param ids 需要删除的数据ID列表
     */
    @Override
    public void batchDelete(List<Long> ids) {
        // 执行批量删除操作
        Boolean b = elasticSearchTemplate.opsForDoc().batchDelete(IndexConstants.SERVE, ids);
        if( !b){
            // 删除失败时，等待1秒后抛出异常
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("同步数据到ES失败");
        }
    }

}
