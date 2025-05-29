package com.gg.busStation.data.layout;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.gg.busStation.ui.layout.ETAView;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode
public class StopItemData {
    private final ObservableField<String> stopNumber;
    private final ObservableField<String> headline;
    private final ObservableField<String> context;

    private String co;
    private int routeId;
    private int stopSeq;
    private int bound;

    private ETAView[] etas;

    public ObservableBoolean isOpen = new ObservableBoolean(false);

    public StopItemData(String stopNumber, String headline, String context, int bound, String co, int routeId, int stopSeq) {
        this.stopNumber = new ObservableField<>(stopNumber);
        this.headline = new ObservableField<>(headline);
        this.context = new ObservableField<>(context);
        this.bound = bound;
        this.routeId = routeId;
        this.stopSeq = stopSeq;
        this.co = co;
    }

    public String getStopNumber() {
        return stopNumber.get();
    }

    public String getHeadline() {
        return headline.get();
    }

    public String getContext() {
        return context.get();
    }

    public void setStopNumber(String stopNumber) {
        this.stopNumber.set(stopNumber);
    }

    public void setHeadline(String headline) {
        this.headline.set(headline);
    }

    public void setContext(String context) {
        this.context.set(context);
    }
}
