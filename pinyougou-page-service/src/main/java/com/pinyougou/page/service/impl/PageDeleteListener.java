package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


/**
 * author: DahongZhou
 * Date:
 */
@Component
public class PageDeleteListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("接收到消息：" + goodsIds);
            boolean b = itemPageService.deleteItemHtml(goodsIds);
            System.out.println("网页文件删除结果" + b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
