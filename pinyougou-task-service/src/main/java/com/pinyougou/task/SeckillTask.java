package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * author: DahongZhou
 * Date:
 */
@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟更新秒杀商品
    public void refreshSeckillGoods() {
        System.out.println("执行了任务调度" + new Date());
        //查询所有的秒杀商品键集合
        List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());

        //查询正在秒杀的商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//剩余库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间

        if (ids.size() > 0) {
            criteria.andIdNotIn(ids);//排除缓存中已经有的商品
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //装入缓存
        for (TbSeckillGoods seckill : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckill.getId(), seckill);
            System.out.println("增量更新秒杀商品ID：" + seckill.getId());
        }
        System.out.println("将" + seckillGoodsList.size() + "条商品装入缓存");
    }

    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        //查询缓存中的数据，扫描每条记录，如果当前时间超过了截止时间，移除此记录
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        System.out.println("开始清除秒杀商品的方法" + new Date());
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            if (seckillGoods.getEndTime().getTime() < new Date().getTime()) {
                //同步到数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                //清除缓存
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                System.out.println("秒杀商品：" + seckillGoods.getId() + "已过期");
            }
        }
        System.out.println("完成了清除秒杀商品的方法...end");
    }
}