package com.gg.busStation.function.feature;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.Route;
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

    public FareManager(SQLiteDatabase db) {
        this.fareDAO = new FareDAOImpl(db);
    }

    @SneakyThrows
    public void saveFares() {
        InputStream dataStream = HttpClientHelper.getDataStream(dataJsonUrl);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream), 65536)) {
            String line;

            // 跳过表头行
            reader.readLine();

            int lastRouteId = -1;
            int lastBound = -1;
            String lastFare = "";
            int startPickStop = -1;
            int lastPickStop = -1;
            StringBuilder routeFare = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                // Header: fare_id(0), price(1), currency(2), payment(3), transfers(4), agency_id(5)
                int c1 = line.indexOf(',');
                int c2 = line.indexOf(',', c1 + 1);
                // 中间列 (currency, payment, transfers) 不需要，直接跳过查找
                // 从 price 后面开始找第 3, 4, 5 个逗号
                // 快速跳至第5列 (Company)
                int c3 = line.indexOf(',', c2 + 1);
                int c4 = line.indexOf(',', c3 + 1);
                int c5 = line.indexOf(',', c4 + 1);

                if (c5 != -1) {
                    // agency_id 可能在行尾，没有下一个逗号
                    int c6 = line.indexOf(',', c5 + 1);
                    int endOfAgency = (c6 == -1) ? line.length() : c6;

                    // TODO 数据库中无专线小巴数据
                    if (isGMB(line, c5 + 1, endOfAgency)) {
                        continue;
                    }
                }

                // 解析 fare_id (第1列，索引0)
                // 格式: routeId-routeSeq-stopId (例如: 101-1-1)
                // 必须在第一个逗号 c1 之前
                int d1 = line.indexOf('-');
                int d2 = line.indexOf('-', d1 + 1);
                if (d1 == -1 || d2 == -1 || d1 > c1) continue;

                int routeId = parseFastInt(line, 0, d1);
                int routeSeq = parseFastInt(line, d1 + 1, d2);
                int currentPickStop = parseFastInt(line, d2 + 1, c1);

                // 获取 Price (第2列，索引1)
                int fareStart = c1 + 1;
                int fareEnd = c2;

                if (lastRouteId != routeId || lastBound != routeSeq) {
                    // 保存上一条完整的路线数据
                    if (lastRouteId != -1) {
                        routeFare.append(startPickStop).append("-").append(lastPickStop)
                                .append(",").append(lastFare);

                        fareDAO.insert(lastRouteId, lastBound, routeFare.toString());
                    }

                    // 重置状态
                    routeFare.setLength(0);
                    lastRouteId = routeId;
                    lastBound = routeSeq;
                    startPickStop = currentPickStop;
                    lastPickStop = currentPickStop;

                    // 只有在需要时才创建 Price 字符串
                    lastFare = line.substring(fareStart, fareEnd);
                }

                // 判断站点是否连续，或者票价是否变动
                boolean fareChanged = !regionEquals(line, fareStart, fareEnd, lastFare);

                if (lastPickStop != currentPickStop && fareChanged) {
                    routeFare.append(startPickStop).append("-").append(lastPickStop)
                            .append(",").append(lastFare).append(";");

                    startPickStop = currentPickStop;
                    lastFare = line.substring(fareStart, fareEnd); // 仅在变动时生成新对象
                }

                lastPickStop = currentPickStop;
            }

            // 处理最后一行遗留数据
            if (lastRouteId != -1) {
                routeFare.append(startPickStop).append("-").append(lastPickStop)
                        .append(",").append(lastFare);
                fareDAO.insert(lastRouteId, lastBound, routeFare.toString());
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

    /**
     * 快速解析字符串中的整数
     */
    private int parseFastInt(String s, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                result = result * 10 + (c - '0');
            }
        }
        return result;
    }

    /**
     * 比较字符串的某一部分是否与目标字符串相等
     */
    private boolean regionEquals(String line, int start, int end, String target) {
        int len = end - start;
        if (len != target.length()) return false;
        for (int i = 0; i < len; i++) {
            if (line.charAt(start + i) != target.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查指定范围内的字符是否为 "GMB"
     */
    private boolean isGMB(String line, int start, int end) {
        if (end - start != 3) return false;
        return line.charAt(start) == 'G' &&
                line.charAt(start + 1) == 'M' &&
                line.charAt(start + 2) == 'B';
    }
}
