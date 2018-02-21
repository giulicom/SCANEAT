package com.app.project.scaneatapp.db_lists;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.app.project.scaneatapp.MyApplication;


public class MyListsDAO_impl implements MyList_DAO {
    private SQLiteDatabase database;
    private MyList_SQLiteHelper dbHelper;
    private String[] allColumns = {MyList_SQLiteHelper.COLUMN_ID,
            MyList_SQLiteHelper.COLUMN_NAME};

    @Override
    public void open() {
        if(dbHelper == null) dbHelper =
                new MyList_SQLiteHelper(MyApplication.getAppContext());
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public void close() {dbHelper.close();}

    // from Object to database
    private ContentValues myListToValues(MyList list) {
        ContentValues values = new ContentValues();
        values.put(MyList_SQLiteHelper.COLUMN_NAME, list.getName());

        return values;
    }

    // from database to Object
    private MyList cursorToMyList(Cursor cursor) {
        int id = cursor.getInt(0);
        String name = cursor.getString(1);

        return new MyList(id,name);
    }

    @Override
    public MyList insertList(MyList list) {
        long insertId = database.insert(MyList_SQLiteHelper.TABLE_LISTS, null, myListToValues(list));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(MyList_SQLiteHelper.TABLE_LISTS, allColumns,
                MyList_SQLiteHelper.COLUMN_ID + " = ?",
                new String[]{"" + insertId}, null, null, null);
        cursor.moveToFirst();
        MyList l = cursorToMyList(cursor);
        cursor.close();
        return l;
    }

    // EDIT NAME OF AN EXISTING LIST
    @Override
    public void editList(MyList list) {
        int id_list = list.getId();

        String whereClause = MyList_SQLiteHelper.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{ ""+id_list};
        // query
        database.update(MyList_SQLiteHelper.TABLE_LISTS, myListToValues(list), whereClause, whereArgs);
        Log.d("editList", list.getId() + " " + list.getName());
    }

    @Override
    public List<MyList> getAllListsExceptOne(int idList) {
        String whereClause = MyList_SQLiteHelper.COLUMN_ID + " != ?";
        String[] whereArgs = new String[]{ ""+idList};

        List<MyList> lists = new ArrayList<MyList>();
        Cursor cursor = database.query(MyList_SQLiteHelper.TABLE_LISTS,
                allColumns, whereClause, whereArgs, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            MyList list = cursorToMyList(cursor);
            lists.add(list);
            cursor.moveToNext();
        }
        cursor.close();
        return lists;

    }

    @Override
    public void deleteList(MyList list) {
        int id = list.getId();

        // preferred version compared to the previous one
        database.delete(MyList_SQLiteHelper.TABLE_LISTS,
                MyList_SQLiteHelper.COLUMN_ID + " = ?",
                new String[]{"" + id});
    }

    @Override
    public List<MyList> getAllLists() {
        List<MyList> lists = new ArrayList<MyList>();
        Cursor cursor = database.query(MyList_SQLiteHelper.TABLE_LISTS,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            MyList list = cursorToMyList(cursor);
            Log.d("whileee",list.toString());
            lists.add(list);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return lists;
    }
}
