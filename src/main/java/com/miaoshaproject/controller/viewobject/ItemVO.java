package com.miaoshaproject.controller.viewobject;

import java.math.BigDecimal;

public class ItemVO {
    private Integer id;

    //商品名
    private String title;

    //商品价格
    private BigDecimal price;

    //商品库存
    private Integer stock;

    //商品描述
    private String description;

    //商品销量
    private Integer sales;

    //商品描述图片的url
    private String imgUrl;
}
