package com.projects.mocks.mocks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by Cristian on 11/19/2016.
 */

public class DBAdapter
{
    //Symbols table
    private static final String SYMBOLS_TABLE = "Symbols";
    private static final String SYMBOLS_ID = "ID";
    private static final String SYMBOLS_NAME = "Name";

    //Portfolio table
    private static final String PORTFOLIO_TABLE = "Portfolio";
    private static final String PORTFOLIO_ID = "ID";
    private static final String PORTFOLIO_SYMBOL = "Symbol";
    private static final String PORTFOLIO_QTY = "QTY";

    //database finals
    private static final String DATABASE_NAME = "mocksdb";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "DBAdapter";

    //create statements
    private static final String DATABASE_CREATE_TABLE_SYMBOLS = "CREATE TABLE \"Symbols\" ( `ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `Name` TEXT NOT NULL UNIQUE )";
    private static final String DATABASE_CREATE_TABLE_PORTFOLIO = "CREATE TABLE `Portfolio` ( `ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `Symbol` TEXT NOT NULL UNIQUE, `QTY` INTEGER NOT NULL )";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try{
                db.execSQL(DATABASE_CREATE_TABLE_SYMBOLS);
                db.execSQL(DATABASE_CREATE_TABLE_PORTFOLIO);
            }
            catch (SQLException e)
            {
                Log.d("DATABASE CREATE ERROR", e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS Symbols");
            db.execSQL("DROP TABLE IF EXISTS Portfolio");
        }
    }

    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close(){DBHelper.close();}

    //inserts for the tables.
    public long insertSymbol(String name){
        ContentValues initialValues = new ContentValues();
        initialValues.put(SYMBOLS_NAME, name);
        return db.insert(SYMBOLS_TABLE, null, initialValues);
    }

    public long insertPortfolio(String symbol, int qty){
        ContentValues contentValues = new ContentValues();
        contentValues.put(PORTFOLIO_SYMBOL, symbol);
        contentValues.put(PORTFOLIO_QTY, qty);
        return db.insert(PORTFOLIO_TABLE, null, contentValues);
    }

    //deletes for portfolio (let me know if symbols needs a delete)
    public boolean deletePortfolioRow(int id){
        return db.delete(PORTFOLIO_TABLE, PORTFOLIO_ID + "=" + id, null) > 0;
    }

    public boolean deletePortfolioRow(String name){
        return db.delete(PORTFOLIO_TABLE, PORTFOLIO_SYMBOL + "='" + name + "'", null) > 0;
    }

    public void deleteAllPortfolio(){
        db.execSQL("delete from " + PORTFOLIO_TABLE);
    }

    public Cursor getPortfolioSymbol(String name){
            return db.query(PORTFOLIO_TABLE,new String[]{PORTFOLIO_QTY}," Symbol = '" + name +"'",null ,null, null,null);
    }
    public int updatePortfolioSymbol(String name,int newQTY)
    {
        ContentValues args = new ContentValues();
        args.put(PORTFOLIO_QTY, newQTY);
        return db.update(PORTFOLIO_TABLE,args,"Symbol = '"+name+"'",null);
    }

    //retrieve functions for the tables

    //db.query(Table, column(s), whereClause, whereArgs, groupBy, having, orderBy, limit);
    public Cursor getAllPortfolio()
    {
        return db.query(PORTFOLIO_TABLE, new String[] {PORTFOLIO_ID, PORTFOLIO_QTY, PORTFOLIO_SYMBOL}, null, null, null, null, null);
    }

    //shouldnt be used to retrieve all 20,000 rows. here just in case.
    public Cursor getAllSymbols()
    {
        return db.query(SYMBOLS_TABLE, new String[] {SYMBOLS_NAME}, null, null, null, null, null);
    }

    public Cursor searchForSymbol(String pattern)
    {
        String whereClause = "Name Like\"" +  pattern + "%\"";
        return db.query(SYMBOLS_TABLE, new String[] {SYMBOLS_NAME}, whereClause, null, null, null, null, null);
    }


    public Cursor getFiftySymbols(int startIndex)
    {
         String whereClause = SYMBOLS_ID + " >= " + startIndex;
        return db.query(SYMBOLS_TABLE, new String[]{SYMBOLS_NAME}, whereClause, null, null, null, null, "50");
    }
}
