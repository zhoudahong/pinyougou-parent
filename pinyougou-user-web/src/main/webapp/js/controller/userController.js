//控制层
app.controller('userController', function ($scope, $controller, userService) {
    //比较两次输入的密码是否一致
    $scope.register = function () {
        if ($scope.password != $scope.entity.password) {
            alert("两次输入密码不一致，请重新输入");
            $scope.password = "";
            return;
        }

        //用户注册
        userService.add($scope.entity, $scope.smsCode).success(
            function (response) {
                alert(response.message);
            }
        );
    }

    //发送验证码
    $scope.sendCode = function () {
        if ($scope.entity.phone == null || $scope.entity.phone == "") {
            alert("请输入手机号");
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);
            }
        );
    }

});	
