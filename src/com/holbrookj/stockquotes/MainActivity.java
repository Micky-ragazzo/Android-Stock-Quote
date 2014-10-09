package com.holbrookj.stockquotes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	JSONObject jsonObj;
	
	EditText stockSymbol;

	TextView displayStockName;
	TextView displayStockValue;
	
	Button searchButton;
	
	String jsonText = null;
	
	private final static String list = "list";
	private final static String resources = "resources";
	private final static String resource = "resource";
	private final static String fields = "fields";
	private final static String name = "name";
	private final static String price = "price";

	
	String stockName, stockValue;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		stockSymbol = (EditText) findViewById(R.id.input_stock_symbol);
		searchButton = (Button) findViewById(R.id.search_button);
		displayStockName = (TextView) findViewById(R.id.stock_name);
		displayStockValue = (TextView) findViewById(R.id.stock_value);
		
		//displayStockName.setTextSize(unit, size)
		
		searchButton.setOnClickListener(new View.OnClickListener() {
			
			
			@Override
			public void onClick(View v) {
				new JSONParse().execute();
			}
		});
	}
	
	/*
	 * JSONParse:  a method which calls fetchJsonString method and 
	 * creates a new JSONObject from the string returned by 
	 * fetchJsonString.  Then it parses that JSONObject to get the 
	 * name and price of the stock corresponding to the stock symbol
	 * that is input by the user.  Lastly, the values for name and 
	 * price are output to the user via separate TextViews. 
	 * 
	 * Note: This is (and must be) an asynchronous task so that network operations 
	 * are NOT done on the UI thread.
	 */
	private class JSONParse extends AsyncTask<String, String, JSONObject>{
		String input = "";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			input = stockSymbol.getText().toString();
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			System.out.println(input);
			String jsonString = fetchJsonString(input);
			try {
				jsonObj = new JSONObject(jsonString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonObj;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			
			try{
				JSONObject jsonList = result.getJSONObject(list);
				JSONArray jsonResources = jsonList.getJSONArray(resources);
				
				JSONObject jsonRes = jsonResources.getJSONObject(0);
				System.out.println(jsonRes);
				JSONObject jsonResource = jsonRes.getJSONObject(resource);
				JSONObject jsonFields = jsonResource.getJSONObject(fields); 
				stockName = jsonFields.getString(name);
				stockValue = jsonFields.getString(price);
				displayStockName.setText(stockName);
				displayStockValue.setText(stockValue);
				
			}catch(JSONException jsonEx){
				jsonEx.printStackTrace();
			}
			
			super.onPostExecute(result);
		}
	}
	

	/*
	 * fetchJsonString: a method which will retreive the json output 
	 * of the specific yahoo url and then convert it to a string 
	 * representation of the json file.
	 * 
	 *  input:  stock symbol for a company; this is user input.
	 *  
	 *  return:  will return a text representation of the json file 
	 *  corresponding to the stock symbol that the user provided
	 */
	
	private String fetchJsonString(String input){
		String url = "http://finance.yahoo.com/webservice/v1/symbols/" + input 
				+ "/quote?format=json";
		System.out.println(url);
		
		
		InputStream inStream = null;
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity httpEntity = response.getEntity();
			
			inStream = httpEntity.getContent();
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream, "iso-8859-1"));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while((line = buffReader.readLine()) != null){
				stringBuilder.append(line + "\n");
				//Print to check the String Building
				//System.out.println(line);
			}
			
			jsonText =  stringBuilder.toString();
			//print to check String being passed back is correct
			//System.out.println(jsonText);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			try{
				if(inStream != null){
					inStream.close();
				}
			}catch(Exception ex){}
		}
		return jsonText;

	}
}
