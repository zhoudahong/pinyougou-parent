package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 5000)
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);

        //缓存处理
        saveToRedis();
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
    private RedisTemplate redisTemplate;

    //将品牌列表和规格列表放入缓存
    private void saveToRedis() {
        //获取模板数据
        List<TbTypeTemplate> typeTemplateList = findAll();
        //循环模板
        for (TbTypeTemplate typeTemplate : typeTemplateList) {
            //存储品牌列表
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
            System.out.println("brandList"+brandList);
            //存储规格列表
            List<Map> specList = findSpecList(typeTemplate.getId());//根据模板ID查询规格列表
            System.out.println("specList"+specList);
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }
        System.out.println("缓存品牌列表");
    }

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;


    @Override
    public List<Map> findSpecList(long id) {
        //查询模板
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        System.out.println(typeTemplate);
        String specIds = typeTemplate.getSpecIds();
        System.out.println(specIds);
        List<Map> idList = JSON.parseArray(specIds, Map.class);
        System.out.println(idList);
        for (Map map : idList) {
            //查询规格选项列表
            TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
            criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
            List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
            map.put("option", options);

        }
        return idList;
    }
}