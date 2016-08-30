package homework5.person.charge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChargeDB {

    public static final String TABLE_NAME = "Charge";

    public static final String KEY_ID = "_ID";
    public static final String Column_Date = "Date";
    public static final String Column_Type = "Type";
    public static final String Column_Item = "Item";
    public static final String Column_Cost = "Cost";
    public static final String Column_PicturePath = "PicturePath";

    private SQLiteDatabase db;

    public ChargeDB(Context context){
        db = DBHelper.getDatabase(context);
    }

    public void close(){
        db.close();
    }

    public Bill insert(Bill item){
        ContentValues cv = new ContentValues();

        cv.put(Column_Date, item.getDate());
        cv.put(Column_Type, item.getType());
        cv.put(Column_Item, item.getItem());
        cv.put(Column_Cost, item.getCost());
        cv.put(Column_PicturePath, item.getPicturePath());

        long id = db.insert(TABLE_NAME, null, cv);

        item.setId(id);

        return item;
    }

    public ArrayList<Bill> getAll() {
        ArrayList<Bill> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public boolean delete(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    public ArrayList<Integer> getAllCost(){
        ArrayList<Integer> allCost = new ArrayList<>();
        String[] column = {Column_Cost};
        Cursor cursor = db.query(TABLE_NAME, column, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            allCost.add(cursor.getInt(0));
        }

        cursor.close();
        return allCost;
    }

    public Bill getRecord(Cursor cursor) {
        Bill result = new Bill();
        result.setId(cursor.getLong(0));
        result.setDate(cursor.getString(1));
        result.setType(cursor.getString(2));
        result.setItem(cursor.getString(3));
        result.setCost(cursor.getInt(4));
        result.setPicturePath(cursor.getString(5));
        return result;
    }
}