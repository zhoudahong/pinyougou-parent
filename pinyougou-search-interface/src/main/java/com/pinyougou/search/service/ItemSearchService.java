package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * author: DahongZhou
 * Date:
 */
public interface ItemSearchService {
    /**
     * 搜索
     *
     * @param
     * @return
     */
    public Map<String, Object> search(Map searchMap);


    //更新到索引库
    public void importList(List list);

    /**
     * 删除数据
     * @param ids
     */
    public void deleteByGoodsIds(List goodsIdList);
}