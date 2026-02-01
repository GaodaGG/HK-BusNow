package com.gg.busStation.data.bus;

import java.util.Locale;

public record Stop(int id, String nameE, String nameC, String nameS, double lat, double lon) {
    public String getName(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return nameE;
        }

        if (new Locale("zh_rHK").getLanguage().equals(language)) {
            return nameC;
        }

        return nameS;
    }
}
