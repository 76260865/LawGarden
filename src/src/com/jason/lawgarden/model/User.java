package com.jason.lawgarden.model;

import java.util.Date;

public class User {
    private int id;
    private String userName;
    private int serviceType;
    private Date purchaseDate;
    private Date overdueDate;
    private String aboutUs;
    private String token;
    private boolean isRememberPwd;

    public boolean isRememberPwd() {
        return isRememberPwd;
    }

    public void setRememberPwd(boolean isRememberPwd) {
        this.isRememberPwd = isRememberPwd;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getOverdueDate() {
        return overdueDate;
    }

    public void setOverdueDate(Date overdueDate) {
        this.overdueDate = overdueDate;
    }

    public String getAboutUs() {
        return aboutUs;
    }

    public void setAboutUs(String aboutUs) {
        this.aboutUs = aboutUs;
    }

}
