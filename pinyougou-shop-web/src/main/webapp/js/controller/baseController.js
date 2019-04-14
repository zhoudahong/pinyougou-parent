app.controller("baseController", function ($scope) { //控制层


    ////从集合中按照 key 查询对象
    $scope.searchObjectByKey = function (list, key, value) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == value) {
                return list[i];
            }
        }
        return null;
    }


    //分页控件配置 totalItems总页数 itemsPerPage页面的记录数 perPageOptions:页面的下拉选项 onChange 页面触发事件,自动更改
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };
    <!--分页方法-->
    $scope.reloadList = function () {
        // $scope.findPage($scope.paginationConf.totalItems,$scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }


    $scope.selectIds = [];//选中的ID集合

    //更新复选
    $scope.updateSele = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id); //如果是被选中,则增加到数组
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);
        }
    }

    //json字符串转json函数
    // $scope.jsonToString=function (jsonString,key) {
    // $scope.jsonToString=function(jsonString,key){
    //     var json=JSON.parse(jsonString);
    //     var json=JSON.parse(jsonString);
    //     var  value="";
    //////// var value="";
    //     for(var i=0;i<jsonString.length;i++){
    //         if(i>0){
    //             value+=",";
    //         }
    //         value+=json[i][key];
    //     }
    //     return value;
    //
    // }
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        for (var i = 0; i < json.length; i++) {//根据获得的json的进行遍历
            if (i > 0) {
                value += ","
            }
            value += json[i][key];
        }
        return value;
    }

    //从集合中按照key查询对象
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }
});