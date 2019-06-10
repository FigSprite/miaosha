package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnmBusinessError;
import com.miaoshaproject.service.IUserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("IUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private ValidatorImpl validator;


    //登入检验
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {
        //通过用户手机获取用户登入信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null){
            throw new BusinessException(EnmBusinessError.USER_NOT_EXIST);
        }

        //对比用户信息内加密的密码是否和传输进来的密码匹配
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectPasswordByUserId(userDO.getId());

        UserModel userModel = this.convertFromDataObject(userDO,userPasswordDO);

        if(StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            return userModel;
        }else {
            throw new BusinessException(EnmBusinessError.USER_LOGIN_FAIL);
        }

    }


    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectPasswordByUserId(id);

        UserModel userModel = this.convertFromDataObject(userDO, userPasswordDO);

        return userModel;
    }





    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //参数校验
        if (StringUtils.isEmpty(userModel.getName()) ||
                userModel.getGender() == null ||
                userModel.getAge() == null ||
                StringUtils.isEmpty(userModel.getTelphone())) {
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //Model->DO
        UserDO userDO = convertFromUserModel(userModel);

        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已重复");
        }
        userModel.setId(userDO.getId());
        System.out.println(userModel);


        UserPasswordDO userPasswordDO = convertPasswordFromUserModel(userModel);
        System.out.println(userPasswordDO);
        userPasswordDOMapper.insertSelective(userPasswordDO);


        //insert如果数据库原先有默认值，而我传入的参数字段有null，会被替换掉
        return;
    }

    //由UserModel组装UserPassWordDO
    private UserPasswordDO convertPasswordFromUserModel(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            return null;
        }

        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }


    //由UserModel组装UserDO
    private UserDO convertFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);

        return userDO;
    }

    //由UserDO组装UserModel
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);

        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
