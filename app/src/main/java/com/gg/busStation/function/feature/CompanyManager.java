package com.gg.busStation.function.feature;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.CompanyData;
import com.gg.busStation.function.database.dao.CompanyDAO;
import com.gg.busStation.function.database.dao.CompanyDAOImpl;
import com.gg.busStation.function.feature.co.Company;

import lombok.SneakyThrows;

public class CompanyManager {
    private final CompanyDAO companyDAO;
    private static final String PACKAGE = "com.gg.busStation.function.feature.co.";

    public CompanyManager(SQLiteDatabase db) {
        this.companyDAO = new CompanyDAOImpl(db);
    }

    @SneakyThrows
    public static Company getCompanyInstance(String company) {
        if (company.equals(CompanyEnum.KMB_CTB.code)) {
            company = "KMBCTB";
        } else if (company.equals(CompanyEnum.KMB_NWFB.code)) {
            company = "KMBNWFB";
        } else if (company.equals(CompanyEnum.LWB_CTB.code)) {
            company = "LWBCTB";
        }

        String className = PACKAGE + company;

        try {
            return (Company) Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static CompanyEnum getCompanyByCode(String code) {
        for (CompanyEnum companyEnum : CompanyEnum.values()) {
            if (companyEnum.getCode().equals(code)) {
                return companyEnum;
            }
        }
        return CompanyEnum.NLB;
    }

    public void saveCompanys() {
        for (CompanyEnum companyEnum : CompanyEnum.values()) {
            CompanyData company = enumToCompany(companyEnum);
            companyDAO.insert(company);
        }
    }

    private static CompanyData enumToCompany(CompanyEnum companyEnum) {
        return new CompanyData(
                companyEnum.getCode(),
                companyEnum.getNameTC(),
                companyEnum.getNameSC(),
                companyEnum.getNameEn()
        );
    }

    @lombok.Getter
    public enum CompanyEnum {
        CTB("CTB", "城巴", "城巴", "City Bus"),
        KMB("KMB", "九巴", "九巴", "Kowloon Motor Bus"),
        NWFB("NWFB", "新巴", "新巴", "New World First Bus"),
        LWB("LWB", "龍運巴士", "龙运巴士", "Long Win Bus"),
        NLB("NLB", "新大嶼山巴士", "新大屿山巴士", "New Lantao Bus"),
        KMB_CTB("KMB+CTB", "九巴/城巴", "九巴/城巴", "Joint Operation of KMB & CTB"),
        KMB_NWFB("KMB+NWFB", "九巴/新巴", "九巴/新巴", "Joint Operation of KMB & NWFB"),
        LWB_CTB("LWB+CTB", "龍運/城巴", "龙运/城巴", "Joint Operation of LWB & CTB"),
        PI("PI", "馬灣巴士", "马湾巴士", "Ma Wan Bus"),
        DB("DB", "愉景灣巴士", "愉景湾巴士", "Discovery Bay Bus"),
        XB("XB", "落馬洲/皇崗過境巴士", "落马洲/皇岗过境巴士", "LMC Cross Boundary Coach"),
        LRTFeeder("LRTFeeder", "港鐵巴士", "港铁巴士", "Mass Transit Railway Feeder Bus"),
        GMB("GMB", "專線小巴", "专线小巴", "Green Minibus"),
        FERRY("FERRY", "渡輪", "渡轮", "Ferry"),
        PTRAM("PTRAM", "山頂纜車", "山顶缆车", "Peak Tram"),
        TRAM("TRAM", "電車", "电车", "Tram");

        private final String code;
        private final String nameTC;  // 繁體中文
        private final String nameSC;  // 簡體中文
        private final String nameEn;  // 英文

        CompanyEnum(String code, String nameTC, String nameSC, String nameEn) {
            this.code = code;
            this.nameTC = nameTC;
            this.nameSC = nameSC;
            this.nameEn = nameEn;
        }

        public String getName(String language) {
            return switch (language) {
                case "en" -> nameEn;
                case "zh_HK" -> nameTC;
                default -> nameSC;
            };
        }
    }
}
