package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

public interface IPromoService {
    PromoModel getPromoByItemId(Integer itemId);
}
