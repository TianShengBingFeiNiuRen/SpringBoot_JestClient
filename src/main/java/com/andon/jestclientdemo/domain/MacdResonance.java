package com.andon.jestclientdemo.domain;

import java.io.Serializable;

/**
 * @author Andon
 * @date 2019/1/21
 *
 * MACD共振
 */
public class MacdResonance implements Serializable {

    private String time;
    private String domain;
    private String pair;
    private String macdTimeType;
    private String macdType;
    private String highDimensionalTime;
    private String lowDimenSionalTime;
    private String base;
    private String quote;
    private String remark;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getMacdTimeType() {
        return macdTimeType;
    }

    public void setMacdTimeType(String macdTimeType) {
        this.macdTimeType = macdTimeType;
    }

    public String getMacdType() {
        return macdType;
    }

    public void setMacdType(String macdType) {
        this.macdType = macdType;
    }

    public String getHighDimensionalTime() {
        return highDimensionalTime;
    }

    public void setHighDimensionalTime(String highDimensionalTime) {
        this.highDimensionalTime = highDimensionalTime;
    }

    public String getLowDimenSionalTime() {
        return lowDimenSionalTime;
    }

    public void setLowDimenSionalTime(String lowDimenSionalTime) {
        this.lowDimenSionalTime = lowDimenSionalTime;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPk(String timeId) {
        return domain + "_" + pair + "_" + macdTimeType + "_" + timeId + "_" + macdType;
    }

    @Override
    public String toString() {
        return "MacdResonance{" +
                "time='" + time + '\'' +
                ", domain='" + domain + '\'' +
                ", pair='" + pair + '\'' +
                ", macdTimeType='" + macdTimeType + '\'' +
                ", macdType='" + macdType + '\'' +
                ", highDimensionalTime='" + highDimensionalTime + '\'' +
                ", lowDimenSionalTime='" + lowDimenSionalTime + '\'' +
                ", base='" + base + '\'' +
                ", quote='" + quote + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
