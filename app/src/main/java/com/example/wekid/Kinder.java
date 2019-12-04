package com.example.wekid;

public class Kinder {

    private String kinderCode;
    private String name;
    private String address;
    private String phoneNum;

    public Kinder(){};
    public Kinder(String kinderCode, String name, String address, String phoneNum) {
        this.kinderCode = kinderCode;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
    }

    public String getKinderCode() {
        return kinderCode;
    }

    public void setKinderCode(String kinderCode) {
        this.kinderCode = kinderCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        phoneNum = phoneNum;
    }
    @Override
    public String toString() {
        return
                kinderCode + " / " + name + " / " + address + " / " + phoneNum;
    }
}
