package com.example.worxforusdbsample;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

//Result is a convenience class to capture errors and pass objects back to the caller
import com.worxforus.Result;
import com.worxforus.db.TableInterface;

public class NuggetTable extends TableInterface<Nugget> {
	public static final String DATABASE_NAME = "sample_db"; //Instead of a text string, this should be a static constant for your app
	public static final String TABLE_NAME = "nugget_table";
	public static final int TABLE_VERSION = 1;
	// 1 - Initial version

	static int i = 0; // counter for field index
	public static final String NUGGET_ID = "nugget_id"; // int
	public static final int NUGGET_ID_COL = i++;
	public static final String NUGGET_TYPE = "nugget_type"; // String
	public static final int NUGGET_TYPE_COL = i++;

	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " 
			+ NUGGET_ID + " 	INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ NUGGET_TYPE + "   TEXT" 
			+ ")";

	private SQLiteDatabase db;
	private NuggetDbHelper dbHelper;

	public NuggetTable(Context _context) {
		dbHelper = new NuggetDbHelper(_context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public Result openDb() {
		Result r = new Result();
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLException e) {
			Log.e(this.getClass().getName(), r.error);
			throw(new RuntimeException(e));
		}
		return r;
	}

	@Override
	public void closeDb() {
		if (db != null)
			db.close();
	}

	@Override
	public void createTable() {
		dbHelper.onCreate(db);
	}

	@Override
	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		invalidateTable();
	}

	public void wipeTable() {
		synchronized (TABLE_NAME) {
			db.delete(TABLE_NAME, null, null);
		}
	}
	
	@Override
	public void updateTable(int last_version) {
		dbHelper.onUpgrade(db, last_version, TABLE_VERSION);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public int getTableCodeVersion() {
		return TABLE_VERSION;
	}

	/**
	 * For ease of use, not efficiency, I combined insert and update as a single statement.  Note that if the item exists,
	 * that two operations are performed, a delete and insert.
	 */
	@Override
	public Result insertOrUpdate(Nugget t) {
		synchronized (TABLE_NAME) {
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(t);
				r.last_insert_id = (int) db.replace(TABLE_NAME, null, cv);
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			return r;
		}
	}
	
	public Result insert(Nugget t) {
		synchronized (TABLE_NAME) {
			Result r = new Result();
			try {
				ContentValues vals = new ContentValues();
				if (t.getId() > 0)
					vals.put(NUGGET_ID, t.getId());
		    	vals.put(NUGGET_TYPE, t.getType());
				r.last_insert_id = (int) db.insert(TABLE_NAME, null, vals);
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			return r;
		}
	}

	@Override
	public Result insertOrUpdateArrayList(ArrayList<Nugget> t) {
		return null; //not implemented in this sample
	}

	public Result insertArrayList(ArrayList<Nugget> list) {
		Result r = new Result();
		db.beginTransaction();
		for (Nugget item : list) {
			try {
				insert(item);
			} catch(SQLException e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return r;
	}
	
	@Override
	public ArrayList<Nugget> getUploadItems() {
		return null; //not implemented in this sample
	}

	public ArrayList<Nugget> getAllEntries() {
		ArrayList<Nugget> al = new ArrayList<Nugget>();
		Cursor list = getAllEntriesCursor();
		if (list.moveToFirst()){
			do {
				al.add(getFromCursor(list));
			} while(list.moveToNext());
		}
		list.close();
		return al;
	}
	
	protected Cursor getAllEntriesCursor() {
		return db.query(TABLE_NAME, null, null, null, null, null, NUGGET_ID);
	}
	
	// ================------------> helpers <-----------==============\\
 
	/** returns a ContentValues object for database insertion
     * @return
     */
    public ContentValues getContentValues(Nugget item) {
    	ContentValues vals = new ContentValues();
    	//prepare info for db insert/update
    	vals.put(NUGGET_ID, item.getId());
    	vals.put(NUGGET_TYPE, item.getType());
		return vals;
    }
    
	/**
	 * Get the data for the item currently pointed at by the database
	 * @param record
	 * @return
	 */
	public Nugget getFromCursor(Cursor record) {
		Nugget c= new Nugget();
    	c.setId(record.getInt(NUGGET_ID_COL));
		c.setType(record.getString(NUGGET_TYPE_COL));
		return c;
	}
	
    // ================------------> db helper class <-----------==============\\
	private static class NuggetDbHelper extends SQLiteOpenHelper {
		public NuggetDbHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// called when the version of the existing db is less than the current
			Log.w(this.getClass().getName(), "Upgrading table from " + oldVersion + " to " + newVersion);
//			if (oldVersion < 1) { //EXAMPLE: if old version was V1, just add field
//				// create new table
//				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
//				onCreate(db);
//				Log.d(this.getClass().getName(), "Creating new "+ DATABASE_TABLE + " Table");
//			}
//			if (oldVersion < 2) {
//				//EXAMPLE: add field and change the index
//				db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD COLUMN "+NEW_COLUMN+" "+NEW_COLUMN_TYPE);
//				db.execSQL("DROP INDEX IF EXISTS "+INDEX_1_NAME); //remove old index
//				db.execSQL(INDEX_1); //add a new index
//				Log.d(this.getClass().getName(), "Adding new field and new index to "	+ DATABASE_TABLE + " Table");
//			}
		}
	}
	
}
