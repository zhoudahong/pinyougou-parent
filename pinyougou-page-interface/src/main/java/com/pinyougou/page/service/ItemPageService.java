package com.pinyougou.page.service;

/**
 * author: DahongZhou
 * Date:
 */
public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详细页
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
