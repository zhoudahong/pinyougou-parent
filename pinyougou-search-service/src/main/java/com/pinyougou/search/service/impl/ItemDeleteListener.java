package com.pinyougou.search.service.impl;

/**
 * author: DahongZhou
 * Date:
 * <p>
 * 监听：用于删除索引库中记录
 *
 * @author Administrator
 */
/**
 * 监听：用于删除索引库中记录
 * @author Administrator
 *
 */

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements javax.jms.MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听接收到消息..." + goodsIds);
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            System.out.println("成功删除索引库中的记录");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}