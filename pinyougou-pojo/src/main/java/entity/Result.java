package entity;

import java.io.Serializable;

/********
 * 结果信息
 *
 * date2018/9/21 16:19

 ******/


public class Result implements Serializable{

    private boolean success;

    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result(boolean success, String message) {

        this.success = success;
        this.message = message;
    }
}
