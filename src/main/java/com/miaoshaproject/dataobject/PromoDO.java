package com.miaoshaproject.dataobject;

import java.math.BigDecimal;
import java.util.Date;

public class PromoDO {
    private Integer id;

    private String promoName;

    private Date startDate;

    private Integer itemId;

    private BigDecimal promoItemPrice;

    private Date endDate;

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
        this.promoName = promoName == null ? null : promoName.trim();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "PromoDO{" +
                "id=" + id +
                ", promoName='" + promoName + '\'' +
                ", startDate=" + startDate +
                ", itemId=" + itemId +
                ", promoItemPrice=" + promoItemPrice +
                ", endDate=" + endDate +
                '}';
    }
}