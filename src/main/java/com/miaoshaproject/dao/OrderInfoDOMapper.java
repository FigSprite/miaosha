package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.OrderInfoDO;

public interface OrderInfoDOMapper {
    int deleteByPrimaryKey(String id);

    int insert(OrderInfoDO record);

    int insertSelective(OrderInfoDO record);

    OrderInfoDO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderInfoDO record);

    int updateByPrimaryKey(OrderInfoDO record);
}