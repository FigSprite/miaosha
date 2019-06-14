package com.miaoshaproject.service;

public interface ICacheService {
    //存
    void setCommonCache(String key ,Object value);

    //取
    Object getFromCommonCache(String key);
}
