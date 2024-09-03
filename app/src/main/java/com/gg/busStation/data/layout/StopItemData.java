package com.gg.busStation.data.layout;

import androidx.databinding.ObservableField;

public class StopItemData{
    public ObservableField<String> stopNumber;
    public ObservableField<String> headline;
    public ObservableField<String> context;

    private String routeId;
    private String stopId;
    private String bound;
    private String service_type;

    public StopItemData(String stopNumber, String headline, String context, String bound, String service_type, String routeId, String stopId){
        this.stopNumber = new ObservableField<>(stopNumber);
        this.headline = new ObservableField<>(headline);
        this.context = new ObservableField<>(context);
        this.bound = bound;
        this.service_type = service_type;
        this.routeId = routeId;
        this.stopId = stopId;
    }

    public String getStopNumber() {
        return stopNumber.get();
    }

    public void setStopNumber(String stopNumber) {
        this.stopNumber = new ObservableField<>(stopNumber);
    }

    public String getHeadline() {
        return headline.get();
    }

    public void setHeadline(String headline) {
        this.headline = new ObservableField<>(headline);
    }

    public String getContext() {
        return context.get();
    }

    public void setContext(String context) {
        this.context = new ObservableField<>(context);
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public String getBound() {
        return bound;
    }

    public void setBound(String bound) {
        this.bound = bound;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
