package com.gg.busStation.function;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gg.busStation.data.bus.CloudFeature;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FeatureDAO;
import com.gg.busStation.function.database.dao.RouteDAO;
import com.gg.busStation.function.feature.CompanyManager;
import com.gg.busStation.function.feature.FareManager;
import com.gg.busStation.function.feature.FeatureManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BusDataManager {
    private BusDataManager() {
    }

    public static void initData(Context context, OnDataInitListener onDataInitListener, boolean updateNow) {
        //判断是否需要更新数据
        SettingsManager settingsManager = SettingsManager.getInstance(context);
        if (!updateNow) {
            long lastUpdateTime = settingsManager.getLastUpdateTime();
            long updateDataCycleTime = settingsManager.getUpdateDataCycleTime();

            if (settingsManager.isInit() && System.currentTimeMillis() <= lastUpdateTime + updateDataCycleTime) {
                return;
            }
        }

        onDataInitListener.start();

        initData(context, onDataInitListener);

        onDataInitListener.finish(true);
    }

    private static void initData(Context context, OnDataInitListener listener) {
        DataBaseHelper dataBaseHelper = DataBaseHelper.getInstance(context);

        CompanyManager companyManager = new CompanyManager(dataBaseHelper.getDatabase());
        FeatureManager featureManager = new FeatureManager(dataBaseHelper.getDatabase());
        FareManager fareManager = new FareManager(dataBaseHelper.getDatabase());

        dataBaseHelper.executeTransaction(db -> {
            int max = 3;
            try {
                companyManager.saveCompanys();

                listener.progress(0, max, "正在从网络获取巴士数据");
                List<CloudFeature> features = featureManager.fetchAllFeatures();

                listener.progress(1, max, "正在导入巴士数据");
                featureManager.saveFeatures(features);

                listener.progress(2, max, "正在从网络获取车费数据");
                fareManager.saveFares();

                listener.progress(max, max, "Done!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static long getMinutesRemaining(Date targetDate) {
        Date now = new Date();
        long currentTimeMillis = now.getTime();
        long targetTimeMillis = targetDate.getTime();
        long timeDifferenceMillis = targetTimeMillis - currentTimeMillis;

        long minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long remainderMillis = timeDifferenceMillis % TimeUnit.MINUTES.toMillis(1);

        // 四舍五入
        if (remainderMillis >= 1000 * 30) {
            minutesRemaining++;
        }

        return minutesRemaining;
    }

    public static List<ListItemData> featuresToListItemData(List<Feature> features, RouteDAO routeDAO) {
        String language = Locale.getDefault().getLanguage();

        List<ListItemData> data = new ArrayList<>();
        for (Feature feature : features) {
            routeDAO.getRouteSeq(feature.getRouteId()).stream().forEach(integer -> {
                ListItemData listItemData = createListItemData(feature, integer, language);
                data.add(listItemData);
            });
        }

        return data;
    }

    @NonNull
    private static ListItemData createListItemData(Feature feature, int bound, String language) {
        StringBuilder headline = new StringBuilder();
        headline.append(bound == FeatureManager.outbound ? feature.getStartName(language) : feature.getEndName(language))
                .append(headline.length() > 20 ? " ->\n" : " -> ")
                .append(bound == FeatureManager.outbound ? feature.getEndName(language) : feature.getStartName(language));

        String companyCode = getCompanyCode(feature);
        CompanyManager.CompanyEnum companyEnum = CompanyManager.CompanyEnum.valueOf(companyCode);

        return new ListItemData(feature.getRouteId(),
                feature.getCompanyCode(),
                feature.getRouteName(language),
                headline.toString(),
                "",
                bound,
                feature.getServiceMode(),
                companyEnum.getName(language));
    }

    private static String getCompanyCode(Feature feature) {
        String companyCode = feature.getCompanyCode();
        if (companyCode.equals(CompanyManager.CompanyEnum.KMB_CTB.getCode())) {
            companyCode = CompanyManager.CompanyEnum.KMB_CTB.name();
        } else if (companyCode.equals(CompanyManager.CompanyEnum.LWB_CTB.getCode())) {
            companyCode = CompanyManager.CompanyEnum.LWB_CTB.name();
        } else if (companyCode.equals(CompanyManager.CompanyEnum.KMB_NWFB.getCode())) {
            companyCode = CompanyManager.CompanyEnum.KMB_NWFB.name();
        }
        return companyCode;
    }

    public static List<ListItemData> routesToListItemData(List<Route> routes, FeatureDAO featureDAO) {
        String language = Locale.getDefault().getLanguage();

        List<ListItemData> data = new ArrayList<>();
        for (Route route : routes) {
            Feature feature = featureDAO.getFeature(route.id());
            ListItemData listItemData = createListItemData(feature, route.routeSeq(), language);
            data.add(listItemData);
        }

        return data;
    }


    public interface OnDataInitListener {
        void start();

        void progress(int now, int max, String tip);

        void finish(boolean status);
    }
}
