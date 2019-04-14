package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
//import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }


    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }


    @Autowired
    private Destination queueSolrDeleteDestination;//用户在索引库中删除记录
    @Autowired
    private Destination topicPageDeleteDestination;//用于删除静态网页的消息
    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
            //从索引库中删除
            //itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

            //通过消息中间件实现在商品删除时也同时移除索引库记录
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            //执行商品删除后，同时删除每个服务器上的商品详细页
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 更新状态
     *
     * @param ids
     * @param status
     */
//    @Reference
//    private ItemSearchService itemSearchService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination queueSolrDestination;//用于导入solr索引库的消息目标（点对点方式）

    @Autowired
    private Destination topicPageDestination;//用于生产商品详情页的消息目标（发布订阅方式）

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            //按照SPU ID查询 SKU列表(状态为1)
            if (status.equals("1")) {//审核通过
                List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);

                //调用搜索接口实现数据批量导入
                if (itemList.size() > 0) {
                    //itemSearchService.importList(itemList);

                    //运用消息中间件activeMQ实现运营商后台与搜索服务的零耦合。
                    // 运营商执行商品审核后，向activeMQ发送消息（SKU列表），搜索服务从activeMQ接收到消息并导入到solr索引库。
                    String jsonString = JSON.toJSONString(itemList);//转换为json传输
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(jsonString);
                        }
                    });
                } else {
                    System.out.println("没有明细数据");
                }
                //静态页生成
                for (final Long goodsId : ids) {
                    //itemPageService.genItemHtml(goodsId);
                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(goodsId + "");
                        }
                    });
                }
            }
            return new Result(true, "修改状态成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改状态失败");
        }
    }

//    @Reference(timeout = 5000)
//    private ItemPageService itemPageService;

    /**
     * 生成静态页（测试）
     *
     * @param goodsId
     */
    @RequestMapping("/genHtml")
    public void genHtml(Long goodsId) {

        //itemPageService.genItemHtml(goodsId);
    }
}
