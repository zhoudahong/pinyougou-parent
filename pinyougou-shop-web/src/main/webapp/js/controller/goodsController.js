//商品控制层（商家后台）
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {


    $controller('baseController', {$scope: $scope});//继承

    //定义一个组合实体类
    // $scope.entity = {tbGoods: {}, tbGoodsDesc: {itemImages: [], specificationItems: []}};
    // //一级下拉选择框
    // $scope.selectCatalog1 = function () {
    //     itemCatService.findPId(0).success(
    //         function (data) {
    //             $scope.category1List = data;
    //         }
    //     );
    // }
    // //二级下拉选择框
    // $scope.$watch('entity.tbGoods.category1Id', function (newvalue, oldvalue) {
    //     itemCatService.findPId(newvalue).success(
    //         function (data) {
    //             $scope.category2List = data;
    //         });
    // });
    // //三级下拉选择框
    // $scope.$watch('entity.tbGoods.category2Id', function (newvalue, oldvalue) {
    //     itemCatService.findPId(newvalue).success(
    //         function (data) {
    //             $scope.category3List = data;
    //         });
    // });
    // //模板id
    // $scope.$watch('entity.tbGoods.category3Id', function (newvalue, oldvalue) {
    //     itemCatService.findOne(newvalue).success(
    //         function (data) {
    //             //$scope.itemcatentity=data;
    //             $scope.entity.tbGoods.typeTemplateId = data.typeId;
    //         });
    // });

    // //品牌下拉框//模板 ID 选择后 更新品牌列表
    // $scope.$watch('entity.tbGoods.typeTemplateId', function (newvalue, oldvalue) {
    //
    //     typeTemplateService.findOne(newvalue).success(
    //         function (data) {
    //
    //             $scope.typeTemplate = data;
    //             $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
    //
    //             if ($location.search()['id'] == null) {
    //                 $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
    //
    //             }
    //         });
    //     //增加规格
    //     typeTemplateService.findspecs(newvalue).success(
    //         function (data) {
    //             $scope.specificationEntity = data;
    //         });
    // });


    //增加规格名称,会将勾选的后的规格实体保存到specificationItems数组集合中
    // $scope.updateSpecAttribute = function ($event, text, options) {
    //     var specitemsList = $scope.searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems, 'attributeName', text);
    //     if (specitemsList != null) {
    //         if ($event.target.checked) {
    //             specitemsList.attributeValue.push(options);
    //         } else {
    //             specitemsList.attributeValue.splice(specitemsList.attributeValue.indexOf(options), 1);
    //             if (specitemsList.attributeValue.length == 0) {
    //                 $scope.entity.tbGoodsDesc.specificationItems.splice($scope.entity.tbGoodsDesc.specificationItems.indexOf(specitemsList), 1)
    //             }
    //         }
    //     } else {
    //         if ($event.target.checked) {
    //             $scope.entity.tbGoodsDesc.specificationItems.push({"attributeName": text, "attributeValue": [options]})
    //         }
    //     }
    // }
    $scope.updateSpecAttribute = function ($event, name, value) {
        var specitemsList = $scope.searchObjectByKey(
            $scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (specitemsList != null) {
            if ($event.target.checked) {
                specitemsList.attributeValue.push(value);
            } else {//取消勾选				object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
                //如果选项都取消了，将此条记录移除
                if (specitemsList.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(specitemsList), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }
    }

    //创建SKU列表
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];//初始
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }
    //添加列值
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];//新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < conlumnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    //克隆方法只是给$scope.entity.tbGoodsDesc.specificationItems里specificationItems的spec属性进行赋值,会产生多个对象
    addColumn = function (itemsList, attributeName, attributeValueList) {
        //   alert("测试点");
        var newList = [];//这是一个容器,用于存放更新后的数组集合(这里存放的是实体对象{spec:{...},'price':0,'num':98999,'isEnable':'0','isDefault':'0'})
        for (var i = 0; i < itemsList.length; i++) {
            oldRow = itemsList[i];
            for (var j = 0; j < attributeValueList.length; j++) {
                newRow = JSON.parse(JSON.stringify(oldRow))//深克隆,先将数组集合转换成字符串,然后再转回来一个内容完全独立的对象
                newRow.spec[attributeName] = attributeValueList[j];//给实体对象的spec属性的某个属性进行赋值,注意这里的spec是itemList集合里对象的一个属性
                // alert("测试点");
                newList.push(newRow);
            }
        }
        return newList;
    }


    //曾加图片
    $scope.addImageEntity = function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity)
    }
    $scope.deleteimage = function (id) {
        $scope.entity.tbGoodsDesc.itemImages.splice(id, 1);
    }


    /**
     * 上传图片
     */
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {//如果上传成功，取出url
                    $scope.image_entity.url = response.message;//设置文件地址
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传发生错误");
        });
    };

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }


    //查询实体
    $scope.findOne = function () {
        // alert("测试点1")
        //通过服务location的search方法获得当前对象的id值
        var id = $location.search()['id'];//获取参数值
        //alert(id);
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;

                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表
                //将商品图片的字符串格式转成集合格式
                $scope.entity.goodsDesc.itemImages =
                    JSON.parse($scope.entity.goodsDesc.itemImages);

                //如果没有ID，则加载模板中的扩展数据
                if ($location.search()['id'] == null) {
                    //显示扩展属性
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                }
                //将商品规格的字符串格式转成集合格式
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                //将SKU商品列表中的规格中规格字符串格式转成集合格式
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec =
                        JSON.parse( $scope.entity.itemList[i].spec);
                }


            }
        );
    }


    //保存
    $scope.save=function(){
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction=editor.html();
        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                   // location.href="goods.html";//跳转到商品列表页
                    alert('保存成功');
                    $scope.entity={};
                    editor.html("");
                }else{
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    // <option value="">全部</option>
    // <option value="0">未审核</option>
    // <option value="1">已审核</option>
    // <option value="2">审核未通过</option>
    // <option value="3">关闭</option>
    //商品状态
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];//根据响应获得的状态值显示不同的结果


    $scope.itemCatList = [];//商品分类列表
    //加载商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {//遍历响应结果,将集合中的itemCat表的id和goodsName一一对应分别放入到数组集合中
                    //因为我们需要根据分类ID得到分类名称，所以我们将返回的分页结果以数组形式再次封装。
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        );
    }

    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    //上下架
    $scope.updateMarket = function (marketable) {
        goodsService.updateMarke($scope.selectIds, marketable).success(
            function (data) {
                if (data.success) {
                    $scope.reloadList();//刷新列表
                } else {
                    alert(data.message);
                }
            });
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};//定义页面实体结构
    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }


    //列表中移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //读取一级分类
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        );
    }
    //读取二级分类
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        //根据选择的值，查询二级分类
        if (newValue == null) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        );
    });
    //读取三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        );
    });
    //三级分类选择后  读取模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId; //更新模板ID
            }
        );
    });


    //模板ID选择后  更新模板对象
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//获取类型模板
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
            }
        );
        //查询规格列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        );
    });
});
