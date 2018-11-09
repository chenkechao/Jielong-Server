package com.jielong.core.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserInfo {
    private Integer id;

    private Integer userId;

    private String name;

    private String nickName;

    private String avatarUrl;

    private String phoneNumber;

    private String email;

    private String deliveryAddress;

    private String qrcodeUrl;
    
    //用户状态
    private Integer state;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="UTC-7")
    private Date createdAt;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="UTC-7")
    private Date updatedAt;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl == null ? null : avatarUrl.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress == null ? null : deliveryAddress.trim();
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

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

    public String getQrcodeUrl() {
        return qrcodeUrl;
    }

    public void setQrcodeUrl(String qrcodeUrl) {
        this.qrcodeUrl = qrcodeUrl;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", qrcodeUrl='" + qrcodeUrl + '\'' +
                ", state=" + state +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}