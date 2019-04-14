app.controller('searchController', function ($scope, $location, searchService) {
    $scope.searchMap = {
        'keywords': '',
        'category': '', 'brand': '', 'spec': {}, 'price': '',
        'pageNo': 1, 'pageSize': 40,
        'sort': '', 'sortField': '',
        'totalPages': ''
    };//创建搜索对象

    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
                buildPageLabel();//构建分页栏
            }
        );
    }

    //构建分页栏(totalPages为总页数)
    buildPageLabel = function () {
        $scope.pageLabel = [];//新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;//总页码
        var firstPage = 1;//开始页码
        var lastPage = maxPageNo;//截止页码

        $scope.firstDot = true;//分页开始有“..."
        $scope.lastDot = true;//分页后面有“..."
        if ($scope.resultMap.totalPages > 5) {//如果总页数大于5页,显示部分页码
            if ($scope.searchMap.pageNo <= 3) {//如果当前页小于等于3
                lastPage = 5; //前5页
                $scope.firstDot = false;//开始无“..."
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {//如果当前页大于等于最大页码-2
                firstPage = maxPageNo - 4;//后5页
                $scope.lastDot = false;//后面无“..."
            } else { //显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//开始无“..."
            $scope.lastDot = false;//后面无“..."
        }
        //循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }


    //给搜索对象增加元素
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }

    //移除复合搜索条件
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == 'price') {//如果是分类或品牌
            $scope.searchMap[key] = "";
        } else {//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
    }


    //根据页码查询
    $scope.queryByPage = function (pageNo) {
        //页码验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页是否为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否为最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //排序查询
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();//调用查询
    }


    //判断关键字是否是品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }


    //加载查询关键字
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }


});

