package com.jielong.core.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Jielong {
    private Integer id;
    
    private Integer userId;

    private String topic;

    private String description;

    private String addressName;

    private String addressDetail;
    
    private Integer addressLongitude;  //经度
    
    private Integer addressLatitude;   //纬度
   

    private Integer setFinishTime;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    private String introImages;

    private String goodsAddresses;
    
    private String phoneNumber;
    
   

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    
    private Integer status;     //接龙状态 1：进行中 2：活动结束  3：提前终止 4：其他情况

    //商品列表
    private List<Goods> goodsList;
    
    //图片介绍数组
    private List<String> imageList;
    
    //提货地址数组
    private List<UserAddress>  takeGoodsAddressList;
    
    private UserInfo userInfo;
    

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public Integer getSetFinishTime() {
		return setFinishTime;
	}

	public void setSetFinishTime(Integer setFinishTime) {
		this.setFinishTime = setFinishTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public String getIntroImages() {
		return introImages;
	}

	public void setIntroImages(String introImages) {
		this.introImages = introImages;
	}

	public String getGoodsAddresses() {
		return goodsAddresses;
	}

	public void setGoodsAddresses(String goodsAddresses) {
		this.goodsAddresses = goodsAddresses;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<Goods> getGoodsList() {
		return goodsList;
	}

	public void setGoodsList(List<Goods> goodsList) {
		this.goodsList = goodsList;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Integer getAddressLongitude() {
		return addressLongitude;
	}

	public void setAddressLongitude(Integer addressLongitude) {
		this.addressLongitude = addressLongitude;
	}

	public Integer getAddressLatitude() {
		return addressLatitude;
	}

	public void setAddressLatitude(Integer addressLatitude) {
		this.addressLatitude = addressLatitude;
	}

	public List<String> getImageList() {
		return imageList;
	}

	public void setImageList(List<String> imageList) {
		this.imageList = imageList;
	}

	public List<UserAddress> getTakeGoodsAddressList() {
		return takeGoodsAddressList;
	}

	public void setTakeGoodsAddressList(List<UserAddress> takeGoodsAddressList) {
		this.takeGoodsAddressList = takeGoodsAddressList;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	
	
	
    
   
	
    
}