//品牌 服务层
app.service("brandService",function ($http) {

    //读取列表数据绑定到表单中
    this.findAll=function () {
        return $http.get("../brand/findAll.do");
    }

    this.findPage=function (page,size) {
        return $http.get("../brand/findPage.do?page="+page+"&size="+size);
    }

    this.add=function (entity) {
        return $http.post("../brand/add.do",entity);
    }

    this.updateBrand=function (entity) {
        return $http.post("../brand/updateBrand.do",entity);
    }

    this.delet=function (ids) {
        return $http.get("../brand/deleteBrand.do?ids="+ids);
    }

    this.findOne=function (id) {
        return $http.get("../brand/findOne.do?id="+id);
    }

    this.search=function (page,size,searchEntity) {
        return $http.post("../brand/searchPage.do?page="+page+"&size="+size,searchEntity);
    }


    //下拉列表数据
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do');
    }
});