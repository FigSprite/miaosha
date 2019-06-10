package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderInfoDOMapper;
import com.miaoshaproject.dao.SequenceInfoDOMapper;
import com.miaoshaproject.dataobject.OrderInfoDO;
import com.miaoshaproject.dataobject.SequenceInfoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnmBusinessError;
import com.miaoshaproject.service.IItemService;
import com.miaoshaproject.service.IOrderService;
import com.miaoshaproject.service.IUserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IItemService iItemService;

    @Autowired
    private OrderInfoDOMapper orderInfoDOMapper;

    @Autowired
    private SequenceInfoDOMapper sequenceInfoDOMapper;


    /*
    *1.校验下单状态，下单商品是否存在
    * 2.落单减库存，//支付减库存
    * 3.订单入库
    * 4.返回前端
     */
    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount,Integer promoId) throws BusinessException {
        ItemModel itemModel = iItemService.getItemById(itemId);
        if(itemModel==null){
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }

        UserModel userModel = iUserService.getUserById(userId);
        if(userModel==null){
            throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息错误");
        }

        if(amount<=0||amount>99){
            throw new BusinessException((EnmBusinessError.PARAMETER_VALIDATION_ERROR),"数量信息错误");
        }

        if(promoId!=null){
            if(promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息错误");
            }else if(itemModel.getPromoModel().getStatus()!=2){
                throw new BusinessException(EnmBusinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始或者已结束");
            }
        }




        boolean result = iItemService.decreaseStock(itemId,amount);
        if (!result){
            throw new BusinessException(EnmBusinessError.STOCK_NOT_ENOUGH);
        }

        iItemService.increaseSales(itemId,amount);

        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(new BigDecimal(amount.toString()).multiply(orderModel.getItemPrice()));


        //主键没有设计成自增
        //生成交易流水号
        String no = generateOrderNo();
        orderModel.setId(no);
        OrderInfoDO orderInfoDO = this.convertOrderInfoDOFromOrderModel(orderModel);
        orderInfoDOMapper.insertSelective(orderInfoDO);

        //返回前端
        return orderModel;

    }

    /*
    模型转换
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo(){
        //订单号有16位
        StringBuilder sb = new StringBuilder();
        //前8位为时间信息
        LocalDateTime now = LocalDateTime.now();
        sb.append(now.format(DateTimeFormatter.ISO_DATE).replace("-",""));

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceInfoDO sequenceInfoDO =  sequenceInfoDOMapper.getSequenceByName("order_info");
        sequence = sequenceInfoDO.getCurrentValue();
        sequenceInfoDO.setCurrentValue(sequenceInfoDO.getCurrentValue()+sequenceInfoDO.getStep());
        sequenceInfoDOMapper.updateByPrimaryKeySelective(sequenceInfoDO);
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6-sequenceStr.length(); i++) {
            sb.append(0);
        }
        sb.append(sequenceStr);

        //最后2位为分库分表位,暂时写死
        sb.append("00");
        return sb.toString();
    }

    private OrderInfoDO convertOrderInfoDOFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        BeanUtils.copyProperties(orderModel,orderInfoDO);
        return orderInfoDO;
    }
}
