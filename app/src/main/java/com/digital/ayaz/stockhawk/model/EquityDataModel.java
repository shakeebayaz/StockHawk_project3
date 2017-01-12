package com.digital.ayaz.stockhawk.model;

/**
 * Created by Shakeeb on 12/01/17.
 */
public class EquityDataModel {
    public String name;
    public String symbal;
    public String price;
    public String change;
    public String percentage;
    public boolean isPositive;
    public boolean isPercentage;

    public boolean isPercentage() {
        return isPercentage;
    }

    public void setPercentage(boolean percentage) {
        isPercentage = percentage;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbal() {
        return symbal;
    }

    public void setSymbal(String symbal) {
        this.symbal = symbal;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
}
