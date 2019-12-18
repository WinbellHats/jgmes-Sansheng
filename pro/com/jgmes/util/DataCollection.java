package com.jgmes.util;

public class DataCollection {
    //设备名称
    private String deviceName;
    //取数ID
    private String fetchID;
    //设别中文描述
    private String deviceNameDes;
    //数据值
    private String dataValue;
    //取数时间
    private String fetchDate;
    //设备备注
    private String deviceRemark;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getFetchID() {
        return fetchID;
    }

    public void setFetchID(String fetchID) {
        this.fetchID = fetchID;
    }

    public String getDeviceNameDes() {
        return deviceNameDes;
    }

    public void setDeviceNameDes(String deviceNameDes) {
        this.deviceNameDes = deviceNameDes;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public String getFetchDate() {
        return fetchDate;
    }

    public void setFetchDate(String fetchDate) {
        this.fetchDate = fetchDate;
    }

    public String getDeviceRemark() {
        return deviceRemark;
    }

    public void setDeviceRemark(String deviceRemark) {
        this.deviceRemark = deviceRemark;
    }
}
