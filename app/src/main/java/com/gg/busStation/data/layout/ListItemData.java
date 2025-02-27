package com.gg.busStation.data.layout;

import androidx.databinding.ObservableField;

import java.util.Objects;

public class ListItemData {
    private ObservableField<String> stopNumber;
    private ObservableField<String> headline;
    private ObservableField<String> context;
    public ObservableField<String> tips;

    private String co;
    private String bound;
    private String service_type;

    public ListItemData(String co, String stopNumber, String headline, String context, String bound, String service_type, String tips) {
        this.stopNumber = new ObservableField<>(stopNumber);
        this.headline = new ObservableField<>(headline);
        this.context = new ObservableField<>(context);
        this.tips = new ObservableField<>(tips);
        this.co = co;
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

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public void setTips(String tips) {
        this.tips = new ObservableField<>(tips);
    }

    public String getTips() {
        return tips.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItemData that = (ListItemData) o;
        return Objects.equals(stopNumber, that.stopNumber) && Objects.equals(headline, that.headline) && Objects.equals(context, that.context) && Objects.equals(bound, that.bound) && Objects.equals(service_type, that.service_type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopNumber, headline, context, bound, service_type);
    }
}
