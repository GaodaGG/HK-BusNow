package com.gg.busStation.data.layout;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.gg.busStation.ui.layout.ETAView;

import java.util.Arrays;
import java.util.Objects;

public class StopItemData{
    private final ObservableField<String> stopNumber;
    private final ObservableField<String> headline;
    private final ObservableField<String> context;

    private String co;
    private String routeId;
    private String stopId;
    private String bound;
    private String service_type;

    private ETAView[] etas;

    public ObservableBoolean isOpen = new ObservableBoolean(false);

    public StopItemData(String stopNumber, String headline, String context, String bound, String service_type, String co, String routeId, String stopId){
        this.stopNumber = new ObservableField<>(stopNumber);
        this.headline = new ObservableField<>(headline);
        this.context = new ObservableField<>(context);
        this.bound = bound;
        this.service_type = service_type;
        this.routeId = routeId;
        this.stopId = stopId;
        this.co = co;
    }

    public String getStopNumber() {
        return stopNumber.get();
    }

    public void setStopNumber(String stopNumber) {
        this.stopNumber.set(stopNumber);
    }

    public String getHeadline() {
        return headline.get();
    }

    public void setHeadline(String headline) {
        this.headline.set(headline);
    }

    public String getContext() {
        return context.get();
    }

    public void setContext(String context) {
        this.context.set(context);
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

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public ETAView[] getEtas() {
        return etas;
    }

    public void setEtas(ETAView[] etas) {
        this.etas = etas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopItemData that = (StopItemData) o;
        return Objects.equals(stopNumber, that.stopNumber) && Objects.equals(headline, that.headline) && Objects.equals(context, that.context) && Objects.equals(co, that.co) && Objects.equals(routeId, that.routeId) && Objects.equals(stopId, that.stopId) && Objects.equals(bound, that.bound) && Objects.equals(service_type, that.service_type) && Objects.deepEquals(etas, that.etas) && Objects.equals(isOpen, that.isOpen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopNumber, headline, context, co, routeId, stopId, bound, service_type, Arrays.hashCode(etas), isOpen);
    }
}
