package se.matzlarsson.skuff.database;

import java.io.Reader;
import java.util.Date;

import se.matzlarsson.skuff.database.data.Result;
import se.matzlarsson.skuff.database.data.ResultDeserializer;
import se.matzlarsson.skuff.ui.Refreshable;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataSyncer extends AsyncTask<String, Void, Result>{

	private static DataSyncer instance = null;
	private static final String SERVER_URL = "http://skuff.host-ed.me/fetchdata.php";
	
	private ActionBarActivity activity = null;
	private boolean working = false;
	
	private DataSyncer(ActionBarActivity activity){
		this.activity = activity;
	}
	
	public static DataSyncer getInstance(ActionBarActivity activity){
		if(instance == null || instance.isCancelled() || !instance.isWorking()){
			instance = new DataSyncer(activity);
		}
		
		return instance;
	}
	
	@Override
	protected Result doInBackground(String... params) {
		if(!this.isWorking()){
			setWorking(true);
			Result result = fetchData();
			saveToDb(result);
			return result;
		}
		return null;
	}
	
	private Result fetchData(){
		try{
			Reader reader = IOUtil.getReaderFromHttp(SERVER_URL+getPreviousFetch());
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
			gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
			Gson gson = gsonBuilder.create();
			Result result = gson.fromJson(reader, Result.class);
			reader.close();
			return result;
		} catch (Exception ex) {
			Log.e("SKUFF", "Failed to parse JSON due to: " + ex);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Result result){
		if(result != null){
			loadedData(result);
			if(activity != null){
				Fragment frag = activity.getSupportFragmentManager().findFragmentByTag("currentFragment");
				if(frag instanceof Refreshable){
					((Refreshable)frag).refresh();
				}
			}
		}else{
			failedLoadingData();
		}
		
		setWorking(false);
	}
	
	public boolean isWorking(){
		return this.working;
	}
	
	public void setWorking(boolean working){
		this.working = working;
	}
	
	
	public String getPreviousFetch(){
		DatabaseHelper db = DatabaseHelper.getInstance();
		Cursor c = db.selectQuery("SELECT time FROM "+DatabaseFactory.TABLE_UPDATES+" WHERE _id=? LIMIT 1", new String[]{"1"});
		if(c.getCount()>0){
			c.moveToFirst();
			String time = c.getString(c.getColumnIndex("time"));
			long timestamp = DateUtil.stringToDate(time).getTime()/1000;
			return "?prevFetch="+timestamp;
		}else{
			return "";
		}
	}
	
	public void saveToDb(Result result){
		DatabaseHelper db = DatabaseHelper.getInstance();
		result.saveToDb(db);
		DBTable table = DatabaseFactory.getTable(DatabaseFactory.TABLE_UPDATES);
		db.insertOrUpdateQuery(table, new String[]{"1", DateUtil.dateToString(new Date())});
	}
	
	public void loadedData(Result result){
		Toast.makeText(this.activity, "Grabbed data ("+result.getUpdatesInfo()+")", Toast.LENGTH_SHORT).show();
	}
	
	public void failedLoadingData(){
		Toast.makeText(this.activity, "Failed to sync data", Toast.LENGTH_SHORT).show();
	}

}
