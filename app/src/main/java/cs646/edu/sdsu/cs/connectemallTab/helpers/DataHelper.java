package cs646.edu.sdsu.cs.connectemallTab.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Pkp on 04/05/17.
 */

public class DataHelper {

    private static final String DATABASE_NAME = "connectemall";
    private static final String DATABASE_TABLE = "users";

    private static final int DATABASE_VERSION = 1;

    private final Context helperContext;
    private static SQLiteDatabase db;
    private DBManager manageDB;


    public DataHelper(Context ctx){
        helperContext = ctx;
    }

    public DataHelper openConnetion() throws SQLException{
        manageDB = new DBManager(helperContext);
        db = manageDB.getWritableDatabase();
        return this;
    }

    public void closeConnection(){
        manageDB.close();
    }

    public long insert(ContentValues cvItems){
       return db.insertWithOnConflict(DATABASE_TABLE, null, cvItems, SQLiteDatabase.CONFLICT_IGNORE );
    }

    public Cursor select(String Query, String [] params){
        return db.rawQuery(Query, params);
    }
    private static class DBManager extends SQLiteOpenHelper{


       public DBManager(Context ctx){
           super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
       }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" +Constants.KEY_ID + " INTEGER PRIMARY KEY , "+ Constants.KEY_NICKNAME
                    + " TEXT UNIQUE, " + Constants.KEY_COUNTRY + " TEXT NOT NULL, " + Constants.KEY_STATE
                    +  " TEXT NOT NULL, "+Constants.KEY_CITY + " TEXT, "+ Constants.KEY_YEAR + " INTEGER, "
                    +  Constants.KEY_LATITUDE +" REAL NOT NULL, "+Constants.KEY_LONGITUDE + " REAL NOT NULL, "+ Constants.KEY_TIMESTAMP + " TEXT);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE);
            onCreate(db);

        }
    }
}
