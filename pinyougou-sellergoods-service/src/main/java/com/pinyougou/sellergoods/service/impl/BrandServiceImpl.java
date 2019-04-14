package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;


    @Override
    public List<TbBrand> findAll() {

        return brandMapper.selectByExample(null);
    }

    //返回全部列表
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        //调用MyBatis分页插件PageHelper
        PageHelper.startPage(pageNum, pageSize);
        //通过条件进行查询
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }


    //增加品牌
    @Override
    public void addBrand(TbBrand brand) {
        brandMapper.insert(brand);
    }

    //修改品牌
    @Override
    public void updateBrand(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    //根据id查找品牌
    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void deleteBrand(Long[] ids) {
        for(Long id:ids){
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbBrand tbBrand, int pageNum, int pageRow) {
        PageHelper.startPage(pageNum,pageRow);

        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if(tbBrand!=null) {
            if (tbBrand.getName()!= null && tbBrand.getName().length() > 0) {
                criteria.andNameLike("%" + tbBrand.getName() + "%");

            }
            if (tbBrand.getFirstChar() != null && tbBrand.getFirstChar().length() > 0) {
                criteria.andFirstCharLike("%" + tbBrand.getFirstChar() + "%");
            }}
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);

        return new PageResult(page.getTotal(),page.getResult());
    }



    /**
     * 列表数据
     */
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
