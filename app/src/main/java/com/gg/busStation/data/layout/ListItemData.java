package com.gg.busStation.data.layout;

import androidx.databinding.ObservableField;

public class ListItemData {
    public ObservableField<String> stopNumber;
    public ObservableField<String> headline;
    public ObservableField<String> context;

    private String bound;
    private String service_type;

    public ListItemData(String stopNumber, String headline, String context, String bound, String service_type){
        this.stopNumber = new ObservableField<>(stopNumber);
        this.headline = new ObservableField<>(headline);
        this.context = new ObservableField<>(context);
        this.bound = bound;
        this.service_type = service_type;
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
}
