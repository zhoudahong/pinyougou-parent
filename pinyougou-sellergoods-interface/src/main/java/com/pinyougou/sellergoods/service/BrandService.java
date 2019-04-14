package com.pinyougou.sellergoods.service;


import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌服务层接口
 */

public interface BrandService {
    //返回全部列表
    public List<TbBrand> findAll();


    //返回分页列表
    public PageResult findPage(int pageNum, int pageSize);

    //增加品牌
    public void addBrand(TbBrand brand);

    //更新品牌
    public void updateBrand(TbBrand brand);

    //根据id查找品牌
    public TbBrand findOne(Long id);

    //批量删除选中的ids(集合)的记录
    public void deleteBrand(Long[] ids);


    PageResult findPage(TbBrand tbBrand, int pageNum, int pageRow);


    /**
     * 品牌下拉框数据
     */
    List<Map> selectOptionList();
}
