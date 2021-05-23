package gaman.aryal.scanner.dabtabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.util.Date;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "SCANNER.db";
    public static final String TABLE_NAME = "MY_TABLE";
    public static final String ID = "ID";
    public static final String TITLE = "Title";
    public static final String DATA = "Data";
    public static final String TYPE = "Type";
    public static final String DATE = "Date";
    public static final String TIME = "Time";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                TITLE + " TEXT," +
                DATA + " TEXT," +
                TYPE + " TEXT," +
                DATE + " TEXT," +
                TIME + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean save(String title, String data, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        contentValues.put(DATA, data);
        contentValues.put(TYPE, type);
        contentValues.put(DATE, DateFormat.getDateInstance()
                .format(new Date()));
        contentValues.put(TIME, DateFormat.getTimeInstance()
                .format(new Date()));

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor extractAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery("select * from " + TABLE_NAME, null);
    }

    public boolean delete(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id}) != -1;
    }
}
