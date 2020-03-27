package com.example.exp6;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {


    // Declaring Database Variables
    private static final String DATABASE_NAME = "bookmark.db";
    private static final String TABLE_NAME = "bookmarks";
    private static final String COL_1 = "headlines";
    private static final String COL_2 = "links";
    private static final String COL_3 = "platform";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final String COL_4 = sdf.format(new Date().getTime());

    // -----------------------

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (headlines TEXT PRIMARY KEY, links TEXT, platform TEXT, date DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    // TO INSERT THE DATA
    public boolean insertData(String headline, String link, String platform) {
        // Create SQLite Db instance
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        headline = headline.replace("'", "`");
        contentValues.put(COL_1, headline);
        contentValues.put(COL_2, link);
        contentValues.put(COL_3, platform);

        // insert the data
        long res = db.insert(TABLE_NAME, null, contentValues);

        // above method returns -1 incase of any error
        return res != -1;

    }

    public Cursor queryData(String queryField) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                + COL_1 + " LIKE '%" + queryField + "%'", null);
        return cursor;
    }

    // Delete all the entries from the database
    public void removeAllEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        Log.i("All entries deleted", "removeAllEntries");
    }


    // Check if the data is already present
    public boolean checkIsDataAlreadyInDBorNot(String headline) {
        SQLiteDatabase sqldb = this.getWritableDatabase();
        headline = headline.replace("'", "`");

        // check if headline contains ✯
        if (headline.contains("✯ ")) {
            headline = headline.substring(2);
        }
//        headline = "✯ " + headline;
        Cursor cursor = sqldb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE headlines = " + "'" + headline + "'", null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // To view all the data in db
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return res;
    }

    // To view all the data in db
    public Cursor getAllDataOrderBy(String order) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        if (order.equals("News"))
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_3 + " = " + "'News'", null);
        else if (order.equals("Programming"))
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                    + COL_3 + " = " + "'Programming'", null);

        return cursor;
    }


    // UPDATE DATA
    public boolean updateData(String id, String fname, String lname, int marks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, fname);
        contentValues.put(COL_3, lname);
        contentValues.put(COL_4, marks);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    // Delete Data
    public boolean deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int isDeleted = db.delete(TABLE_NAME, "headlines = ?", new String[]{id});
        return isDeleted != 0;
    }

}
