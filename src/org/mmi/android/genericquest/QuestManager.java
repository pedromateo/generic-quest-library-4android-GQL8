/*
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public License 
 *   as published by the Free Software Foundation; either version 3.0 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *   02111-1307 USA
 *
 *   This file is part of the Generic Quest Library for Android (GQL8)
 *   http://www.catedrasaes.org/wiki/CarimQuestionnaires
 *   Contact: pedromateo@um.es
 *
 */
package org.mmi.android.genericquest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuestManager extends Activity {

	private static final String TAG = "QuestManager";

	public QuestManager(){
	}

	// All Question options:
	// 
	// {
	// "q_value": "value_name",            // (man) the name
	// "q_default": "a_value",             // (opt) the default value
	// "q_type": "one type from below",    // (man) how the value is extracted
	//           - q_type_ignore: the value will be ignored  
	//           - q_type_auto: the value will be extracted somehow later  
	//           - q_type_options: a question displaying several options
	//           - q_type_likert5: a 5-item likert scale based on two antonyms
	//           - q_type_likert7: a 7-item likert scale based on two antonyms
	//           - q_type_integer: an integer question from opt1 to opt2
	// "q_wording": "Question wording???", // (opt) the question wording
	// "q_options": "opt1, opt2, opt3",    // (opt) the question options
	// "q_result": "aValue"                // (opt) the place for the returned value
	// },

	// public final values
	public static final String Q_FILE = "q_file";
	public static final String Q_NUMBER_QUESTIONS = "n_questions";
	public static final String Q_QUESTIONS = "questions";
	public static final String Q_VALUE = "q_value";
	public static final String Q_DEFAULT = "q_default";
	public static final String Q_TYPE = "q_type";
	public static final String Q_TYPE_IGNORE = "q_type_ignore";
	public static final String Q_TYPE_AUTO = "q_type_auto";
	public static final String Q_TYPE_OPTIONS = "q_type_options";
	public static final String Q_TYPE_LIKERT5 = "q_type_likert5";
	public static final String Q_TYPE_LIKERT7 = "q_type_likert7";
	public static final String Q_TYPE_INTEGER = "q_type_integer";
	public static final String Q_WORDING = "q_wording";
	public static final String Q_OPTIONS = "q_options";
	public static final String Q_OPTIONS_SEPARATOR = ",";
	public static final String Q_RESULT = "q_result";
	public static final int Q_REQUEST_CODE = 111;

	// static variables
	static JSONObject jObject = null;
	// local variables
	List<String> _results = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quest_manager); 

		/// do questions

		try {

			///
			/// get questionnaire filepath
			String filepath = "";

			Bundle extras = getIntent().getExtras();
			if (extras == null){
				Log.e(TAG, "ERROR: No data to create the questionnaire.");
				finish();
			}

			filepath = extras.getString(QuestManager.Q_FILE);
			if (filepath == ""){
				Log.e(TAG, "ERROR: Empty filepath to questionnaire.");
				finish();
			}

			///
			/// open questionnaire file
			InputStream is_quest = null;
			try{
				is_quest = getAssets().open(filepath); 
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			if (is_quest == null){
				Log.e(TAG, "ERROR: File cannot be opened. Path: " + filepath);
				finish();
			}

			///
			/// open and parse the JSON file
			String jString = "";
			JSONObject jObject = null;

			try{
				// open and read file
				try {
					byte[] b = new byte[is_quest.available()];
					is_quest.read(b);
					jString = new String(b);
				}
				finally {
					is_quest.close();
				}
				// create json object
				jObject = new JSONObject(jString); 
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			if (jObject == null){
				Log.e(TAG, "ERROR: No JSON object could be parsed.");
				finish();
			}

			///
			/// do the questionnaire based on the data included in the JSON object

			/// get metadata from JSON object
			int n_questions = jObject.getInt(Q_NUMBER_QUESTIONS);
			JSONArray questions = jObject.getJSONArray(Q_QUESTIONS);

			Log.w(TAG, "n_questions: " + n_questions);

			/// for each question element...
			for (int i = n_questions -1; i >= 0; i--){
				JSONObject q = questions.getJSONObject(i);
				// go on only if question type is one of
				// the recognized
				String type = q.getString(Q_TYPE);
				if (type.equals(Q_TYPE_INTEGER) 
						|| type.equals(Q_TYPE_LIKERT5)  
						|| type.equals(Q_TYPE_LIKERT7) 
						|| type.equals(Q_TYPE_OPTIONS)){
					// show an activity
					_doQuestion(i, q.getString(Q_VALUE), 
							q.getString(Q_TYPE),
							q.getString(Q_WORDING),
							q.getString(Q_OPTIONS));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		///
		///
		//button listener
		findViewById(R.id.bAccept).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// dump results to intent and finish the activity
				Intent intent = new Intent();

				intent.putExtra(QuestManager.Q_NUMBER_QUESTIONS, _results.size());
				for (int i = 0; i < _results.size(); i++){
					intent.putExtra(Integer.toString(i), _results.get(i));
				}

				setResult(RESULT_OK, intent); //result
				finish();

			}
		});
		///
		///
	}


	@Override
	protected void onResume() {
		super.onResume();

		/// show results if work is done
		if (_results != null &&
				_results.size() > 0){

			/// do it using textviews

			// fill the GUI
			int i = 0;
			Iterator it = _results.iterator();
			LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
			ScrollView sview = (ScrollView)findViewById(R.id.svResults);
			while (it.hasNext()){
				TextView tv = new TextView(this);
				tv.setText("Q" + i + ": " + it.next());
				tv.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
				layout.addView(tv);
				i++;
			}

			/// do it using a model

			/*
			// create a modified results list
			ArrayList aux_l = new ArrayList<String>();
			int i = 0;
			Iterator it = _results.iterator();
			while (it.hasNext()){
				String original = (String)it.next();
				aux_l.add("Question " + i + ": " + original);
				i++;
			}

			// fill the list...
			ListView lv = (ListView)findViewById(R.id.lvResults);
			ArrayAdapter adapter = new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, aux_l);
			lv.setAdapter(adapter);
			 */


		}
	}


	private void _doQuestion(int q_id, String q_value, String q_type, String q_wording, String q_options){

		// start activity of the corresponding type
		Intent intent = new Intent(this,QuestActivity.class);

		Bundle data = new Bundle();
		data.putString(Q_VALUE,q_value);
		data.putString(Q_TYPE,q_type);
		data.putString(Q_WORDING,q_wording);
		data.putString(Q_OPTIONS,q_options);
		intent.putExtras(data);

		startActivityForResult(intent,q_id);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to

		if (_results == null)
			_results = new ArrayList<String>();

		if (requestCode >= 0) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// get the result value
				String result = data.getStringExtra(Q_RESULT);
				Log.d(TAG,"r_code = " + requestCode + "; result: " + result);
				//_results.add(requestCode,result);
				_results.add(result);
			}
		}
	}

}
