package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.CompanyData;

public class CompanyDAOImpl implements CompanyDAO{
    private final SQLiteDatabase db;

    public CompanyDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public boolean exists(String code) {
        String sql = "SELECT * FROM company WHERE code = ?";
        String[] args = {code};
        try (Cursor cursor = db.rawQuery(sql, args)) {
            return cursor.getCount() > 0;
        }
    }

    @Override
    public void insert(CompanyData company) {
        ContentValues values = companyToConvert(company);
        if (exists(company.code())) {
            db.update("company", values, "code = ?", new String[]{company.code()});
            return;
        }

        db.insert("company", null, values);
    }

    private static ContentValues companyToConvert(CompanyData company){
        ContentValues values = new ContentValues();
        values.put("code", company.code());
        values.put("nameC", company.nameC());
        values.put("nameS", company.nameS());
        values.put("nameE", company.nameE());
        return values;
    }
}
