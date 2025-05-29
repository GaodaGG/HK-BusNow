package com.gg.busStation.data.bus;

import java.util.Locale;

public record CompanyData(String code, String nameC, String nameS, String nameE) {
    public String getName(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return nameE;
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return nameC;
        }

        return nameS;
    }
}
