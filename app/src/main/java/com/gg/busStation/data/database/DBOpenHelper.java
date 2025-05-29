package com.gg.busStation.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class DBOpenHelper extends SQLiteOpenHelper {
    private final List<SQLConstants.TableInfo> tables = Arrays.asList(
            new SQLConstants.TableInfo(SQLConstants.featureDBName, SQLConstants.createFeatureDBCommand),
            new SQLConstants.TableInfo(SQLConstants.stopDBName, SQLConstants.createStopDBCommand),
            new SQLConstants.TableInfo(SQLConstants.companyDBName, SQLConstants.createCompanyDBCommand),
            new SQLConstants.TableInfo(SQLConstants.fareDBName, SQLConstants.createFareDBCommand),
            new SQLConstants.TableInfo(SQLConstants.historyDBName, SQLConstants.createHistoryDBCommand),
            new SQLConstants.TableInfo(SQLConstants.routeDBName, SQLConstants.createRouteDBCommand),
            new SQLConstants.TableInfo(SQLConstants.settingsDBName, SQLConstants.createSettingsDBCommand)
    );

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static boolean isTableNotExist(SQLiteDatabase db, String tableName) {
        String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";

        Cursor cursor = db.rawQuery(checkTableSQL, new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return !exists;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        tables.forEach(tableInfo -> {
            if (isTableNotExist(db, tableInfo.getName())) {
                db.execSQL(tableInfo.getCreateCommand());
            }
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
