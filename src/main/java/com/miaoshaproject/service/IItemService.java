package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface IItemService {

    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    ItemModel getItemById(Integer id);

    List<ItemModel> listItem();

    boolean decreaseStock(Integer itemId,Integer amount);

    void increaseSales(Integer itemId,Integer amount);
}
