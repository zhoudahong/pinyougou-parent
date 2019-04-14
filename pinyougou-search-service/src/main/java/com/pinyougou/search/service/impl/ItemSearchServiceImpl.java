package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: DahongZhou
 * Date:
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //对搜索关键字的空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));

       /* Query query = new SimpleQuery();
        //添加查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", page.getContent());*/

        //1,查询列表
        map.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //3.查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)) {//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {//如果没有分类名称，按照第一个查询
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }


        return map;
    }

    private Map searchList(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        //初始化高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        //为查询对象设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //1.1根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按分类筛选
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3按品牌筛选
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5按价格过滤
        if (!"".equals(searchMap.get("price"))) {
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("_");
            if (!price[0].equals("0")) {//如果最低价格不等于0
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery();
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {//如果最高价格不等于*
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery();
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页
        Integer pageNo = (Integer) searchMap.get("pageNo");//获取当前页码
        if (pageNo == null) {
            pageNo = 1;//默认为第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//获取每页大小
        if (pageSize == null) {
            pageSize = 20;//默认每页显示20条记录
        }
        query.setOffset((pageNo - 1) * pageSize);//起始索引
        query.setRows(pageSize);//每页记录数

        //1.7按价格排序
        String sortValue = (String) searchMap.get("sort");//升序ASC，降序DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue != null && !sortValue.equals("")) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }


        //*************************获取高亮结果集*********************
        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合（每条记录的高亮入口）
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表
            List<HighlightEntry.Highlight> highlightsList = entry.getHighlights();
            for (HighlightEntry.Highlight h : highlightsList) {
                List<String> sns = h.getSnipplets();
                //System.out.println(sns);
            }
            if (highlightsList.size() > 0 && highlightsList.get(0).getSnipplets().size() > 0) {
                TbItem item = entry.getEntity();
                //highlightsList.get(0).getSnipplets() 是配置文件中复制域的multiValued="true"
                item.setTitle(highlightsList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());//每页记录数
        map.put("totalPages", page.getTotalPages());//总页数
        map.put("total", page.getTotalElements());//总记录数
        return map;
    }


    private List searchCategoryList(Map searchMap) {
        List list = new ArrayList();

        Query query = new SimpleQuery("*:*");
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//相当于sql语句中的group by
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            //将分组的结果添加到返回的集合中
            list.add(entry.getGroupValue());//数据库中的商品分类的字段值
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据商品分类名称查询品牌和规格列表
     *
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //1，根据商品分类名称获取模板ID
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId != null) {
            //2，根据模板ID获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            //3，根据模板ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }

    //更新到索引库
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }


    //商品删除同步索引数据
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID" + goodsIdList);
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
