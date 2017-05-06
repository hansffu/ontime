package hansffu.ontime.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import hansffu.ontime.model.Stop;

/**
 * Created by hansffu on 5/1/17.
 */

public class FavoriteService extends SQLiteOpenHelper {

    private static final String FAVORITES_TABLE_NAME = "FAVORITES_TABLE_NAME";
    private static final String DATABASE_NAME = "db_favorites";
    private static final int DATABASE_VERSION = 1;
    private static final String STOP_ID = "STOP_ID";
    private static final String STOP_NAME = "STOP_NAME";
    private static final String FAVORITES_TABLE_CREATE = "CREATE TABLE " + FAVORITES_TABLE_NAME + " " +
            "(" + STOP_ID + " INT PRIMARY_KEY," +
            "STOP_NAME TEXT)";

    public FavoriteService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @NonNull
    public List<Stop> getFavorites() {
        List<Stop> favorites = new LinkedList<>();
        try (Cursor cursor = getReadableDatabase().query(FAVORITES_TABLE_NAME, new String[]{STOP_ID, STOP_NAME},
                null, null, null, null, null)) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                favorites.add(new Stop(cursor.getString(1), cursor.getLong(0)));
            }
        }
        return favorites;
    }

    public boolean toggleFavorite(Stop stop) {
        boolean wasFavorite = isFavorite(stop);
        if (wasFavorite) removeFavorite(stop);
        else addFavorite(stop);
        return !wasFavorite;
    }

    public boolean isFavorite(Stop stop) {
        try (Cursor query = getReadableDatabase().query(FAVORITES_TABLE_NAME, new String[]{STOP_ID},
                STOP_ID + " = ?", new String[]{String.valueOf(stop.getId())},
                null, null, null)) {
            if (query.getCount() > 0) return true;
        }
        return false;
    }

    private void addFavorite(Stop stop) {
        ContentValues newFavorite = new ContentValues();
        newFavorite.put(STOP_ID, stop.getId());
        newFavorite.put(STOP_NAME, stop.getName());
        try(SQLiteDatabase writableDatabase = getWritableDatabase()) {
            writableDatabase.insert(FAVORITES_TABLE_NAME, null, newFavorite);
        }
    }

    private void removeFavorite(Stop stop) {
        try(SQLiteDatabase writableDatabase = getWritableDatabase()) {
            writableDatabase.delete(FAVORITES_TABLE_NAME, STOP_ID + " = ?",
                    new String[]{String.valueOf(stop.getId())});
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FAVORITES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
