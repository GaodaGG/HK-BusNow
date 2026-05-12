package com.gg.busStation.data.layout;

import androidx.databinding.ObservableBoolean;

import com.gg.busStation.ui.layout.ETAView;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode
public class StopItemData {
    private String stopNumber;
    private String headline;
    private String context;

    private String co;
    private int routeId;
    private int stopSeq;
    private int bound;

    private ETAView[] etas;

    public ObservableBoolean isOpen = new ObservableBoolean(false);

    public StopItemData(String stopNumber, String headline, String context, int bound, String co, int routeId, int stopSeq) {
        this.stopNumber = stopNumber;
        this.headline = headline;
        this.context = context;
        this.bound = bound;
        this.routeId = routeId;
        this.stopSeq = stopSeq;
        this.co = co;
    }
}
