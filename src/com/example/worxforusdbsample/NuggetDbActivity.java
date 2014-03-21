package com.example.worxforusdbsample;

import java.util.ArrayList;

import com.worxforus.db.TableManager;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NuggetDbActivity extends ActionBarActivity {
    NuggetTable nuggetTable;
    static final int NUM_ITEMS_TO_CREATE = 5;
    static final int NUM_THREADS_TO_RUN = 10;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		nuggetTable = new NuggetTable(this);
		wipeTable(nuggetTable);
	}
	
	public void singleThreadOldStyle(View v) {
		ClassicAction action = new ClassicAction();
		action.execute(this, null, null);
		Toast toast = Toast.makeText(this, "Running old style db access - single thread", Toast.LENGTH_SHORT);
		toast.show();
	}
	public void multiThreadOldStyle(View v) {
		for (int i = 0; i < NUM_THREADS_TO_RUN; i++) {
			ClassicAction action = new ClassicAction();
			action.execute(this, null, null);
		}
		Toast toast = Toast.makeText(this, "Running old style db access - multi thread", Toast.LENGTH_SHORT);
		toast.show();
	}

	public void singleThreadWorxForUs(View v) {
		WorxForUsAction action = new WorxForUsAction();
		action.execute(this, null, null);
		Toast toast = Toast.makeText(this, "Running WorxForUs db access - single thread", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void multiThreadWorxForUs(View v) {
		for (int i = 0; i < NUM_THREADS_TO_RUN; i++) {
			WorxForUsAction action = new WorxForUsAction();
			action.execute(this, null, null);
		}
		Toast toast = Toast.makeText(this, "Running WorxForUs db access - multi thread", Toast.LENGTH_SHORT);
		toast.show();
	}

	//=============---------> Database Helpers <---------===============\\
	
	/**
	 * Start with a fresh database each time
	 */
	public void wipeTable(NuggetTable table) {
		TableManager.acquireConnection(this, NuggetTable.DATABASE_NAME, table);
		table.wipeTable();
		TableManager.releaseConnection(table);
	}
	
	public void wipeTableClassic(NuggetTable table) {
		table.openDb();
		table.wipeTable();
		table.closeDb();
	}
	
	/**
	 * Add data to the database, note that the operation is enclosed within the acquire/release block
	 */
	public void addRandomDataWorxForUs(NuggetTable table) {
		TableManager.acquireConnection(this, NuggetTable.DATABASE_NAME, table);
		addRandomDataHelper(table);
		TableManager.releaseConnection(nuggetTable);
	}

	/**
	 * The classic method of getting data out of a database.
	 */
	public void addRandomDataClassic(NuggetTable table) {
		nuggetTable.openDb();
		addRandomDataHelper(table);
		nuggetTable.closeDb();
	}

	public void addRandomDataHelper(NuggetTable table) {
		ArrayList<Nugget> list = new ArrayList<Nugget>();
		for (int i = 0; i < NUM_ITEMS_TO_CREATE; i++) {
			Nugget nugget = new Nugget();
			nugget.setType((int)Math.round(Math.random()*3));
			list.add(nugget);
		}
		//it is much faster to do db inserts/updates as one transaction
		table.insertArrayList(list);
	}
	
	/**
	 * Get items from the database, note that the operation is enclosed within the acquire/release block
	 */
	public ArrayList<Nugget> getItemsWorxForUs(NuggetTable table) {
		TableManager.acquireConnection(this, NuggetTable.DATABASE_NAME,table);
		ArrayList<Nugget> list = nuggetTable.getAllEntries();
		TableManager.releaseConnection(table);
		return list;
	}
	
	public ArrayList<Nugget> getItemsClassic(NuggetTable table) {
		nuggetTable.openDb();
		ArrayList<Nugget> list = table.getAllEntries();
		nuggetTable.closeDb();
		return list;
	}
	
	/**
	 * This function copies the data from the database into the view on the activity.
	 * Note: this must be run from the UI thread.
	 */
	public void updateDisplay(ArrayList<Nugget> list) {
		TextView textView = (TextView) this.findViewById(R.id.textDisplay);
		String listText = "";
		for (Nugget nugget : list) {
			listText += nugget.getDescription()+"\n";
		}
		textView.setText(listText);
	}
	
	private class ClassicAction extends AsyncTask<Context, Void, Void> {
		@Override
		protected Void doInBackground(Context... params) {
			wipeTableClassic(nuggetTable);
			addRandomDataWorxForUs(nuggetTable);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			updateDisplay(getItemsClassic(nuggetTable));
		}
	}
	
	private class WorxForUsAction extends AsyncTask<Context, Void, Void> {
		@Override
		protected Void doInBackground(Context... params) {
			wipeTable(nuggetTable);
			addRandomDataWorxForUs(nuggetTable);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			updateDisplay(getItemsWorxForUs(nuggetTable));
		}
	}
}
