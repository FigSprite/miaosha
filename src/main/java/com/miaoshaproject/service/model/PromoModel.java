package com.miaoshaproject.service.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

public class PromoModel implements Serializable {
    private Integer id;

    private String promoName;

    //为1还未开始，为2进行中，为3已结束
    private Integer status;

    private DateTime startTime;

    private DateTime endTime;

    private Integer itemId;

    private BigDecimal promoPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PromoModel{" +
                "id=" + id +
                ", promoName='" + promoName + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", itemId=" + itemId +
                ", promoPrice=" + promoPrice +
                '}';
    }
}
