package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnmBusinessError;
import com.miaoshaproject.service.IItemService;
import com.miaoshaproject.service.IPromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service("iItemService")
public class ItemServiceImpl implements IItemService {
    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private IPromoService iPromoService;

    /*
    *1.检验入参
    * 2.model-->do
    * 3.写入数据库
    * 4.返回创建对象
     */


    //创建商品
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);



        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockFromItemModel(itemModel);


        itemStockDOMapper.insertSelective(itemStockDO);

        return this.getItemById(itemModel.getId());
    }
    //商品列表浏览
    public List<ItemModel> listItem(){
        List<ItemDO> itemDOList = itemDOMapper.selectItemDOList();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByPrimaryKey(itemDO.getId());
            ItemModel itemModel = this.convertItemModelFromItemDOAndItemStockItem(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }
    //商品详情浏览
    public ItemModel getItemById(Integer id){
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO==null){
            return null;
        }


        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);

        ItemModel itemModel = this.convertItemModelFromItemDOAndItemStockItem(itemDO,itemStockDO);

        PromoModel promoModel = iPromoService.getPromoByItemId(id);

        if(promoModel!=null&&promoModel.getStatus()!=3){
            itemModel.setPromoModel(promoModel);
        }


        return itemModel;
    }

    //减库存
    @Transactional
    public boolean decreaseStock(Integer itemId,Integer amount){
        int row = itemStockDOMapper.decreaseStock(amount,itemId);
        if(row>0){
            return true;
        }
        return false;
    }

    //加销量
    @Transactional
    public void increaseSales(Integer itemId,Integer amount){
         itemDOMapper.increaseSales(itemId,amount);
    }


    /*
    *模型轉換
     */
    //ItemModel-->ItemDO
    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){

        if(itemModel==null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);

        return itemDO;
    }
    //ItemModel-->ItemStockDO
    private ItemStockDO convertItemStockFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    //ItemDO、ItemStockDO-->ItemModel
    private ItemModel convertItemModelFromItemDOAndItemStockItem(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
