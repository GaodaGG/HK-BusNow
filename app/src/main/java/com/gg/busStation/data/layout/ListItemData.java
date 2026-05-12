package com.gg.busStation.data.layout;

import android.os.Parcel;
import android.os.Parcelable;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode
public class ListItemData implements Parcelable {
    private String stopNumber;
    private String headline;
    private String context;
    private String tips;

    private int routeId;
    private String co;
    private int routeSeq;
    private String service_type;

    public ListItemData(int routeId, String co, String stopNumber, String headline, String context, int routeSeq, String service_type, String tips) {
        this.stopNumber = stopNumber;
        this.headline = headline;
        this.context = context;
        this.tips = tips;
        this.routeId = routeId;
        this.co = co;
        this.routeSeq = routeSeq;
        this.service_type = service_type;
    }

    protected ListItemData(Parcel in) {
        routeId = in.readInt();
        stopNumber = in.readString();
        headline = in.readString();
        context = in.readString();
        tips = in.readString();
        co = in.readString();
        routeSeq = in.readInt();
        service_type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(routeId);
        dest.writeString(stopNumber);
        dest.writeString(headline);
        dest.writeString(context);
        dest.writeString(tips);
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
