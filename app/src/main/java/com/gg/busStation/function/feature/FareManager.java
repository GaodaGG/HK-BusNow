package com.gg.busStation.function.feature;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.Route;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FareDAO;
import com.gg.busStation.function.database.dao.FareDAOImpl;
import com.gg.busStation.function.internet.HttpClientHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import lombok.SneakyThrows;

public class FareManager {
    private static final String dataJsonUrl = "https://static.data.gov.hk/td/pt-headway-tc/fare_attributes.txt";
    private final FareDAO fareDAO;
    
    public FareManager(SQLiteDatabase db){
        this.fareDAO = new FareDAOImpl(db);
    }

    @SneakyThrows
    public void saveFares() {
        InputStream dataStream = HttpClientHelper.getDataStream(dataJsonUrl);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream))) {
            String line;

            // 跳过表头行
            reader.readLine();

            String lastRouteId = "";
            String lastBound = "";
            String lastFare = "";
            String startPickStop = "";
            String lastPickStop = "";
            StringBuilder routeFare = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                String[] fare = line.split(",");

//                if (!fare[5].equals("KMB") && !fare[5].equals("CTB") && !fare[5].equals("KMB+CTB")) {
//                    continue;
//                }

                if (fare[5].equals("GMB")) {
                    continue; // TODO 数据库中无专线小巴数据
                }

                String[] fareId = fare[0].split("-");

                String routeId = fareId[0];
                String routeSeq = fareId[1];
                String currentPickStop = fareId[2];
                String fareValue = fare[1];

                if (!lastRouteId.equals(routeId) || !lastBound.equals(routeSeq)) {
                    // 如果为新的路线或方向则保存之前的数据
                    if (!lastRouteId.isEmpty()) {
                        routeFare.append(startPickStop).append("-").append(lastPickStop)
                                .append(",").append(lastFare);

                        fareDAO.insert(Integer.parseInt(lastRouteId), Integer.parseInt(lastBound), routeFare.toString());
                    }

                    // 为新的路线或方向重置
                    routeFare.setLength(0);
                    lastRouteId = routeId;
                    lastBound = routeSeq;
                    startPickStop = currentPickStop;
                    lastPickStop = currentPickStop;
                    lastFare = fareValue;
                }

                // 将fare附加到 StringBuilder
                if (!lastPickStop.equals(currentPickStop) && !fareValue.equals(lastFare)) {
                    routeFare.append(startPickStop).append("-").append(lastPickStop)
                            .append(",").append(lastFare).append(";");
                    startPickStop = currentPickStop;
                    lastFare = fareValue;
                }

                lastPickStop = currentPickStop;

            }
        }
    }

    public String[] getFares(Route route, FareDAO fareDAO, int stopCount) {
        String[] stopFares = new String[stopCount];
        String fare = fareDAO.getFare(route.id(), route.routeSeq());
        if (fare == null) {
            return new String[0];
        }

        String[] fares = fare.split(";");
        for (String s : fares) {
            String[] fareData = s.split(",");
            String[] pickStopRange = fareData[0].split("-");
            int start = Integer.parseInt(pickStopRange[0]);
            int end = Integer.parseInt(pickStopRange[1]);
            if (end > stopCount) {
                Arrays.fill(stopFares, "");
                break;
            }

            for (int i = start; i <= end; i++) {
                if (i - 1 >= stopCount) {
                    stopFares[stopCount - 1] = "";
                    break;
                }
                stopFares[i - 1] = fareData[1] + " HKD";
            }
        }
        return stopFares;
    }
}
