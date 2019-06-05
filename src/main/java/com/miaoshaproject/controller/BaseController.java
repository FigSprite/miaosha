package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED ="application/x-www-form-urlencoded";

    //从UserController迁移过来
    //定义exception_handler解决未被Controller解析的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex){
        Map<String,Object> responseDate = new HashMap<>();
        if(ex instanceof BusinessException){
            BusinessException bEx = (BusinessException) ex;
            responseDate.put("errCode",bEx.getErrCode());
            responseDate.put("errMsg",bEx.getErrMsg());
        }else {
            //如果不是BusinessException,去枚举类中定义未知错误
            //@ResponseBody标签序列化，如果直接这样写return CommonReturnType.create(EnmBusinessError.UNKNOWN_ERROR,"fail");
            //达不到code和msg分离的目的
            ex.printStackTrace();
            responseDate.put("errCode", EnmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseDate.put("errMsg",EnmBusinessError.UNKNOWN_ERROR.getErrMsg());

/*      当前情况：
            {
                "status":"fail",
                    "data":{
                "errCode":2,
                        "errMsg":"未知错误"
            }
            }


            注释情况：

            {
                "status": "fail",
                    "data": "UNKNOWN_ERROR"
            }

*/
        }




        return CommonReturnType.create(responseDate,"fail");
    }
}
