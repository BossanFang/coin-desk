package com.sunway.app.model;

import java.util.Map;

public class CoindeskResponse {
    private Time time;
    private Map<String, CurrencyInfo> bpi;

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Map<String, CurrencyInfo> getBpi() {
        return bpi;
    }

    public void setBpi(Map<String, CurrencyInfo> bpi) {
        this.bpi = bpi;
    }
}
