package com.jason.lawgarden.model;

import java.util.Date;

public class PurchaseSubject {
    private int id;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Date purchaseDate;
    private Date ourdueDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getOurdueDate() {
        return ourdueDate;
    }

    public void setOurdueDate(Date ourdueDate) {
        this.ourdueDate = ourdueDate;
    }
}
