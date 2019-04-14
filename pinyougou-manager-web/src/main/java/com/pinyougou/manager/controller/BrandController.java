package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/********
 *
 ******/

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(int page, int size) {
        return brandService.findPage(page, size);
    }

    @RequestMapping("/add")
    public Result addBrand(@RequestBody TbBrand tbBrand) {

        try {
            brandService.addBrand(tbBrand);
            return new Result(true, "增加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败!");
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(long id) {
        return brandService.findOne(id);
    }

    @RequestMapping("/updateBrand")
    public Result updateBrand(@RequestBody TbBrand tbBrand) {
        try {
            brandService.updateBrand(tbBrand);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败!");
        }
    }

    @RequestMapping("/deleteBrand")
    public Result deleteBrand(Long[] ids) {

        try {
            brandService.deleteBrand(ids);
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }


    @RequestMapping("/searchPage")
    public PageResult searchPage(@RequestBody TbBrand tbBrand, int page, int size) {
        return brandService.findPage(tbBrand, page, size);
    }



    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
