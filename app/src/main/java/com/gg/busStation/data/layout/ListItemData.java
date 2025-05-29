package com.gg.busStation.data.layout;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.ObservableField;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode
public class ListItemData implements Parcelable {
    private final ObservableField<String> stopNumber;
    private final ObservableField<String> headline;
    private final ObservableField<String> context;
    private final ObservableField<String> tips;

    private int routeId;
    private String co;
    private int routeSeq;
    private String service_type;

    public ListItemData(int routeId, String co, String stopNumber, String headline, String context, int routeSeq, String service_type, String tips) {
        this.stopNumber = new ObservableField<>(stopNumber);
        this.headline = new ObservableField<>(headline);
        this.context = new ObservableField<>(context);
        this.tips = new ObservableField<>(tips);
        this.routeId = routeId;
        this.co = co;
        this.routeSeq = routeSeq;
        this.service_type = service_type;
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

    public void setTips(String tips) {
        this.tips.set(tips);
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

    public String getTips() {
        return tips.get();
    }

    protected ListItemData(Parcel in) {
        routeId = in.readInt();
        stopNumber = new ObservableField<>(in.readString());
        headline = new ObservableField<>(in.readString());
        context = new ObservableField<>(in.readString());
        tips = new ObservableField<>(in.readString());
        co = in.readString();
        routeSeq = in.readInt();
        service_type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(routeId);
        dest.writeString(stopNumber.get());
        dest.writeString(headline.get());
        dest.writeString(context.get());
        dest.writeString(tips.get());
        dest.writeString(co);
        dest.writeInt(routeSeq);
        dest.writeString(service_type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListItemData> CREATOR = new Creator<>() {
        @Override
        public ListItemData createFromParcel(Parcel in) {
            return new ListItemData(in);
        }

        @Override
        public ListItemData[] newArray(int size) {
            return new ListItemData[size];
        }
    };
}
