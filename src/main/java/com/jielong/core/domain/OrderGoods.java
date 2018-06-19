package com.jielong.core.domain;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;

public class OrderGoods {
    private Integer id;

    private Integer orderId;

    private Integer goodsId;

    private Integer sum;

    private BigDecimal money;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT-7")
    private Date createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT-7") 
    private Date updatedAt;
    
    private Goods goods;
    
    //成团标志 0为未成团，1为成团
    private Integer groupFlg;
    
    //还差几人
    private Integer joinGroupNum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getCreatedAt() {
    	TimeZone timeZone = TimeZone.getDefault();  
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		simpleDateFormat.setTimeZone(timeZone);  
		if(createdAt!=null) {
		  return simpleDateFormat.format(createdAt);
		}else {
			return null;
		}   
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
    	TimeZone timeZone = TimeZone.getDefault();  
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		simpleDateFormat.setTimeZone(timeZone);  
		if(updatedAt!=null) {
		  return simpleDateFormat.format(updatedAt);
		}else {
			return null;
		}   
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

	public Goods getGoods() {
		return goods;
	}

	public void setGoods(Goods goods) {
		this.goods = goods;
	}

	public Integer getGroupFlg() {
		return groupFlg;
	}

	public void setGroupFlg(Integer groupFlg) {
		this.groupFlg = groupFlg;
	}

	public Integer getJoinGroupNum() {
		return joinGroupNum;
	}

	public void setJoinGroupNum(Integer joinGroupNum) {
		this.joinGroupNum = joinGroupNum;
	}
	
	
    
}