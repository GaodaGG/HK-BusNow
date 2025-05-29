package com.gg.busStation.data.database;

public class SQLConstants {
    private SQLConstants() {
    }

    public static final String routeDBName = "routes";
    public static final String featureDBName = "features";
    public static final String companyDBName = "company";
    public static final String stopDBName = "stops";
    public static final String historyDBName = "history";
    public static final String fareDBName = "fares";
    public static final String settingsDBName = "settings";

    public static final String createRouteDBCommand = "CREATE TABLE IF NOT EXISTS " + SQLConstants.routeDBName + " (" +
            "routeId INTEGER NOT NULL," +
            "routeSeq INTEGER CHECK (routeSeq IN (1, 2))," + // 1 == outbound, 2 == inbound
            "stopSeq INTEGER," +
            "stopPickDrop INTEGER," +
            "stopId INTEGER," +
            "FOREIGN KEY (routeId) REFERENCES " + featureDBName + "(routeId)" +
            "FOREIGN KEY (stopId) REFERENCES " + stopDBName + "(id)" +
            "PRIMARY KEY (routeId, routeSeq, stopSeq)" +
            ");";

    public static final String createFeatureDBCommand = "CREATE TABLE IF NOT EXISTS " + featureDBName + " (" +
            "routeId INTEGER PRIMARY KEY," +
            "routeNameC TEXT ," +
            "routeNameS TEXT ," +
            "routeNameE TEXT ," +
            "routeType INTEGER," +
            "serviceMode CHAR(2)," +
            "specialType INTEGER," +
            "companyCode TEXT ," +
            "journeyTime INTEGER," +
            "locStartNameC TEXT ," +
            "locStartNameS TEXT ," +
            "locStartNameE TEXT ," +
            "locEndNameC TEXT ," +
            "locEndNameS TEXT ," +
            "locEndNameE TEXT ," +
            "fullFare REAL," +
            "FOREIGN KEY (companyCode) REFERENCES " + companyDBName + "(code)" +
            ");";

    public static final String createCompanyDBCommand = "CREATE TABLE IF NOT EXISTS " + companyDBName + " (" +
            "code TEXT PRIMARY KEY," +
            "nameC TEXT," +
            "nameS TEXT," +
            "nameE TEXT" +
            ");";

    public static final String createStopDBCommand = "CREATE TABLE IF NOT EXISTS " + stopDBName + " (" +
            "id INTEGER PRIMARY KEY," +
            "nameC TEXT," +
            "nameS TEXT," +
            "nameE TEXT," +
            "lat REAL NOT NULL," +
            "long REAL NOT NULL" +
            ");";

    public static final String createHistoryDBCommand = "CREATE TABLE IF NOT EXISTS " + historyDBName + " (" +
            "routeId INTEGER," +
            "routeSeq INTEGER NOT NULL," +
            "pinnedIndex INTEGER, " +
            "timestamp INTEGER, " +
            "FOREIGN KEY (routeId) REFERENCES " + routeDBName + "(routeId)" +
            "PRIMARY KEY (routeId, routeSeq)" +
            ");";

    public static final String createFareDBCommand = "CREATE TABLE IF NOT EXISTS " + fareDBName + " (" +
            "routeId INTEGER NOT NULL, " +
            "routeSeq INTEGER CHECK (routeSeq IN (1, 2)), " +
            "fare TEXT, " +
            "FOREIGN KEY (routeId) REFERENCES " + featureDBName + "(routeId), " +
            "PRIMARY KEY (routeId, routeSeq)" +
            ");";

    public static final String createSettingsDBCommand = "CREATE TABLE IF NOT EXISTS " + settingsDBName + " (" +
            "key TEXT PRIMARY KEY," +
            "value TEXT" +
            ");";


    @lombok.Getter
    @lombok.Setter
    public static class TableInfo {
        private String name;
        private String createCommand;

        public TableInfo(String name, String createCommand) {
            this.name = name;
            this.createCommand = createCommand;
        }
    }
}
