package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.IUserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller("/user")
@RequestMapping("/user/")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
//没有办法做到session共享，所以要加上一些参数

public class UserController extends BaseController{
    @Autowired
    private IUserService iUserService;

    //单例支持多个用户并发访问？
    //通过Spring
    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户注册接口
    @RequestMapping(value="register",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telephone")String telephone,@RequestParam(name="otpCode") String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessException, NoSuchAlgorithmException {
        //验证手机号和对应OTP_code相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!StringUtils.equals(inSessionOtpCode,otpCode)){
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码错误");
        }


        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telephone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        iUserService.register(userModel);
        return CommonReturnType.create(null);
    }

    private String EncodeByMd5(String str) throws NoSuchAlgorithmException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes()));
        return newStr;
    }

    //用户获取otp短信接口
    @RequestMapping(value="get_otp",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone")String telephone){
        //需要按照一定的生成规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);  //[0,99999)
        randomInt+=10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联
        //redis,天生KV,天生可以反复替换，永远存最新，天生有保活时间
        //暂时使用HttpSession方式绑定手机号与OTP_code
        httpServletRequest.getSession().setAttribute(telephone,otpCode);

        //将OTP验证码通过短信通道发送给用户= =省略
        //仅仅为了调试
        System.out.println("telephone:"+telephone+"  &otpCode = "+otpCode);
        return CommonReturnType.create(null);
    }



    //获取用户信息接口
    @RequestMapping("get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = iUserService.getUserById(id);

        if(userModel==null){
           throw  new BusinessException(EnmBusinessError.USER_NOT_EXIST);
            //设置空指针，测试exception_handler
            //userModel.setEncrptPassword("1233");
        }

        UserVO userVO = this.convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    //组装UserVO
    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

    //用户登入接口
    @RequestMapping(value = "login",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone")String telephone,@RequestParam(name="password")String password) throws BusinessException, NoSuchAlgorithmException {
        //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telephone) ||
                org.apache.commons.lang3.StringUtils.isEmpty(password)) {
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登入Service
        UserModel userModel = iUserService.validateLogin(telephone,this.EncodeByMd5(password));

        //将登入凭证加入到用户登入成功的session
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        System.out.println("===UserController  login===");
        System.out.println(httpServletRequest.getSession().getAttribute("IS_LOGIN")+"   =======");
        System.out.println(httpServletRequest.getSession().getAttribute("LOGIN_USER")+"   =======");
        System.out.println("======================");
        return CommonReturnType.create(null);
    }

}
