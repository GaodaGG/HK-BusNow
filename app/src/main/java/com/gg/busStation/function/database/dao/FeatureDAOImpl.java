package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.database.SQLConstants;
import com.gg.busStation.function.feature.CompanyManager;

import java.util.ArrayList;
import java.util.List;

public class FeatureDAOImpl implements FeatureDAO {
    private final SQLiteDatabase db;

    public FeatureDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public boolean exists(int routeId) {
        Cursor cursor = db.query(
                SQLConstants.featureDBName,
                new String[]{"routeId"},
                "routeId = ?",
                new String[]{String.valueOf(routeId)},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @Override
    public void insert(Feature feature) {
        ContentValues values = featureToConvert(feature);
        db.insert(SQLConstants.featureDBName, null, values);
    }

    @Override
    public Feature getFeature(int routeId) {
        Cursor cursor = db.query(SQLConstants.featureDBName, null, "routeId = ?",
                new String[]{String.valueOf(routeId)}, null, null, null
        );

        if (cursor.moveToFirst()) {
            Feature feature = convertToFeature(cursor);

            cursor.close();
            return feature;
        }
        cursor.close();
        return null;
    }

    @Override
    public Feature getFeature(String routeName) {
        Cursor cursor = db.query(SQLConstants.featureDBName, null, "routeNameC = ? OR routeNameS = ? OR routeNameE = ?",
                new String[]{routeName, routeName, routeName}, null, null, null);

        if (cursor.moveToFirst()) {
            Feature feature = convertToFeature(cursor);

            cursor.close();
            return feature;
        }
        cursor.close();
        return null;
    }

    @Override
    public List<Feature> getFeatures(CompanyManager.CompanyEnum company) {
        List<Feature> features = new ArrayList<>();
        Cursor cursor = db.query(SQLConstants.featureDBName, null, "companyCode = ?",
                new String[]{company.getCode()}, null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return features;
        }

        do {
            Feature feature = convertToFeature(cursor);
            features.add(feature);
        } while (cursor.moveToNext());

        return features;
    }

    @Override
    public List<Feature> fuzzySearchFeature(String routeName) {
        if (routeName.isEmpty()) {
            return new ArrayList<>();
        }

        String args = routeName + "%";
        Cursor cursor = db.query(SQLConstants.featureDBName, null,
                "routeNameC LIKE ? OR routeNameS LIKE ? OR routeNameE LIKE ?",
                new String[]{args, args, args},
                null, null, "routeNameE");

        List<Feature> features = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            cursor.close();
            return features;
        }

        do {
            Feature feature = convertToFeature(cursor);
            features.add(feature);
        } while (cursor.moveToNext());

        cursor.close();
        return features;
    }

    @Override
    public List<String> getFeatureNthCharacters(String routeName, int index) {
        Cursor cursor = db.query(SQLConstants.featureDBName,
                new String[]{"SUBSTR(routeNameE, " + index + ", 1) AS C"},
                "routeNameE LIKE ? AND LENGTH(routeNameE) >= " + index + " AND companyCode <> ?",
                new String[]{routeName + "%", CompanyManager.CompanyEnum.XB.getCode()},
                "C", null, "C");

        List<String> characters = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            cursor.close();
            return characters;
        }

        do {
            String character = cursor.getString(cursor.getColumnIndexOrThrow("C"));
            characters.add(character);
        } while (cursor.moveToNext());

        cursor.close();
        return characters;
    }

    @NonNull
    private static Feature convertToFeature(Cursor cursor) {
        return new Feature(
                cursor.getInt(cursor.getColumnIndexOrThrow("routeId")),
                cursor.getString(cursor.getColumnIndexOrThrow("routeNameC")),
                cursor.getString(cursor.getColumnIndexOrThrow("routeNameS")),
                cursor.getString(cursor.getColumnIndexOrThrow("routeNameE")),
                cursor.getInt(cursor.getColumnIndexOrThrow("routeType")),
                cursor.getString(cursor.getColumnIndexOrThrow("serviceMode")),
                cursor.getInt(cursor.getColumnIndexOrThrow("specialType")),
                cursor.getString(cursor.getColumnIndexOrThrow("companyCode")),
                cursor.getInt(cursor.getColumnIndexOrThrow("journeyTime")),
                cursor.getString(cursor.getColumnIndexOrThrow("locStartNameC")),
                cursor.getString(cursor.getColumnIndexOrThrow("locStartNameS")),
                cursor.getString(cursor.getColumnIndexOrThrow("locStartNameE")),
                cursor.getString(cursor.getColumnIndexOrThrow("locEndNameC")),
                cursor.getString(cursor.getColumnIndexOrThrow("locEndNameS")),
                cursor.getString(cursor.getColumnIndexOrThrow("locEndNameE")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("fullFare"))
        );
    }

    private static ContentValues featureToConvert(Feature feature) {
        ContentValues values = new ContentValues();
        values.put("routeId", feature.getRouteId());
        values.put("routeNameC", feature.getRouteNameC());
        values.put("routeNameS", feature.getRouteNameS());
        values.put("routeNameE", feature.getRouteNameE());
        values.put("routeType", feature.getRouteType());
        values.put("serviceMode", feature.getServiceMode());
        values.put("specialType", feature.getSpecialType());
        values.put("companyCode", feature.getCompanyCode());
        values.put("journeyTime", feature.getJourneyTime());
        values.put("locStartNameC", feature.getLocStartNameC());
        values.put("locStartNameS", feature.getLocStartNameS());
        values.put("locStartNameE", feature.getLocStartNameE());
        values.put("locEndNameC", feature.getLocEndNameC());
        values.put("locEndNameS", feature.getLocEndNameS());
        values.put("locEndNameE", feature.getLocEndNameE());
        values.put("fullFare", feature.getFullFare());
        return values;
    }
}
