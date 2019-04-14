//品牌控制层
app.controller("brandController",function ($scope,$controller,brandService) { //控制层

    $controller("baseController",{$scope:$scope})



    $scope.findAll=function () {
        brandService.findAll().success(
            function(response) {
                $scope.list = response;
            }
        );
    }



    <!--分页设置-->
    $scope.findPage=function (page,size) {
        brandService.findPage(page,size).success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
    <!--新增保存设置-->
    $scope.save=function(){
        // var methodName="add";//方法名称
        var Object=null;
        if($scope.entity.id!=null){//如果有ID就进行更新
            // methodName="updateBrand";//执行修改方法
            Object=brandService.updateBrand($scope.entity);
        }else{
            Object=brandService.add($scope.entity)
        }
        Object.success(
            function(response){
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        );
    }

    <!--查找一个-->
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function (response) {
                // $scope.list=response;//{"firstChar":"L","id":1,"name":"联想"}
                $scope.entity=response;//{"firstChar":"L","id":1,"name":"联想"}
            }
        );
    }


    //批量删除

    $scope.delete=function () {
        if($scope.selectIds==null){return;}
        if(confirm("你确定要删除吗?")){                                           
            brandService.delet($scope.selectIds).success(
                function(response){
                    if(response.success){
                        $scope.reloadList();
                        $scope.selectIds=[];
                    }else{
                        alert(response.message)
                        $scope.selectIds=[];
                    }
                }
            )
        }
    }


    $scope.searchEntity={};
    //条件查询
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {

                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;
            }
        );
    }



});