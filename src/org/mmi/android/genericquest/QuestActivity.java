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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.mmi.android.genericquest.QuestManager;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class QuestActivity extends Activity {

	private static final String TAG = "QuestActivity";

	// variables
	String _q_value = "";
	String _q_type = "";
	String _q_wording = "";
	String[] _q_options;
	String _q_result = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quest); 

		///
		/// 1. get data

		Bundle extras = getIntent().getExtras();
		if (extras == null){
			Log.e(TAG, "ERROR: No data to create the question.");
			finish();
		}

		_q_value = extras.getString(QuestManager.Q_VALUE).trim();
		_q_type = extras.getString(QuestManager.Q_TYPE).trim();
		_q_wording = extras.getString(QuestManager.Q_WORDING).trim();
		String aux = extras.getString(QuestManager.Q_OPTIONS);
		_q_options = aux.split(QuestManager.Q_OPTIONS_SEPARATOR);
		for (int i = 0; i < _q_options.length; i++){
			_q_options[i] = _q_options[i].trim();
		}

		// check options condition
		if (_q_options.length < 2){
			Log.e(TAG, "ERROR: Not enough options to create the question.");
			finish();
		}

		///
		/// 2. hide unnecessary elements from activity and set values

		// hide elements depending on type
		//  - visible	0	Visible on screen; the default value.
		//  - invisible	1	Not displayed, but taken into account during layout (space is left for it).
		//  - gone	    2	Completely hidden, as if the view had not been added. 

		Log.i(TAG,"Configuring questionnaire. Type: " + _q_type);

		// type options
		if (_q_type.equals(QuestManager.Q_TYPE_OPTIONS)){ 
			// hide
			Log.d(TAG,"Type 'options' choosed. Configuring...");
			findViewById(R.id.item_likert5).setVisibility(View.GONE);
			findViewById(R.id.item_likert7).setVisibility(View.GONE);
			findViewById(R.id.item_integer).setVisibility(View.GONE);

			// set
			RadioButton rb;
			rb = (RadioButton)findViewById(R.id.rbOption0);
			rb.setText(_q_options[0]);
			rb.setGravity(Gravity.LEFT);
			rb = (RadioButton)findViewById(R.id.rbOption1);
			rb.setText(_q_options[1]);
			rb.setGravity(Gravity.LEFT);
			// additional options
			RadioGroup rg = (RadioGroup)findViewById(R.id.item_options);
			for (int i = 2; i < _q_options.length; i++){
				rb = new RadioButton(rg.getContext());
				rg.addView(rb);
				rb.setId(i);
				rb.setText(_q_options[i]);
				rb.setGravity(Gravity.LEFT);
				rb.setLayoutParams(
						new RadioGroup.LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT));
			}
		}
		// type likert5
		else if (_q_type.equals(QuestManager.Q_TYPE_LIKERT5)){
			// hide
			Log.d(TAG,"Type 'likert5' choosed. Configuring...");
			findViewById(R.id.item_options).setVisibility(View.GONE);
			findViewById(R.id.item_likert7).setVisibility(View.GONE);
			findViewById(R.id.item_integer).setVisibility(View.GONE);
			// set
			TextView tv;
			tv = (TextView)findViewById(R.id.tvLikert5Left);
			tv.setText(_q_options[0]);
			tv = (TextView)findViewById(R.id.tvLikert5Right);
			tv.setText(_q_options[1]);
		}
		// type likert7
		else if (_q_type.equals(QuestManager.Q_TYPE_LIKERT7)){
			// hide
			Log.d(TAG,"Type 'likert7' choosed. Configuring...");
			findViewById(R.id.item_options).setVisibility(View.GONE);
			findViewById(R.id.item_likert5).setVisibility(View.GONE);
			findViewById(R.id.item_integer).setVisibility(View.GONE);
			// set
			TextView tv;
			tv = (TextView)findViewById(R.id.tvLikert7Left);
			tv.setText(_q_options[0]);
			tv = (TextView)findViewById(R.id.tvLikert7Right);
			tv.setText(_q_options[1]);
		}
		// type integer
		else if (_q_type.equals(QuestManager.Q_TYPE_INTEGER)){
			// hide
			Log.d(TAG,"Type 'integer' choosed. Configuring...");
			findViewById(R.id.item_options).setVisibility(View.GONE);
			findViewById(R.id.item_likert5).setVisibility(View.GONE);
			findViewById(R.id.item_likert7).setVisibility(View.GONE);
			// set
			NumberPicker np;
			np = (NumberPicker)findViewById(R.id.item_integer);
			int min = Integer.parseInt(_q_options[0]);
			int max = Integer.parseInt(_q_options[1]);
			np.setMinValue(min);
			np.setMaxValue(max);
			np.setValue(min);
		}

		// set question value
		TextView tv;
		tv = (TextView)findViewById(R.id.tvWording);
		tv.setText(_q_wording);


		///
		///
		//button listener
		findViewById(R.id.bDone).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// get the result
				// type options
				if (_q_type.equals(QuestManager.Q_TYPE_OPTIONS)){
					// get value from selected button
					RadioGroup rg = (RadioGroup)findViewById(R.id.item_options);
					//Log.e(TAG,"rg has children: " + rg.getChildCount());
					RadioButton rb = (RadioButton)findViewById(rg.getCheckedRadioButtonId());
					if (rb != null)
						_q_result = (String)rb.getText();
				}
				// type likert5
				else if (_q_type.equals(QuestManager.Q_TYPE_LIKERT5)){
					// get value from likert5 scale
					RatingBar rbr = (RatingBar)findViewById(R.id.rbLikert5);
					if (rbr == null)
						Log.e(TAG, "ERROR: rating bar 5 does not exist.");
					else if (rbr.getRating() == 0){
						Context context = getApplicationContext();
						CharSequence text = "Please, select a value before touching 'Done'!!";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
						return;//do not finish the activity
					}
					else{
						Float f = rbr.getRating();
						_q_result = f.toString();
					}
				}
				// type likert7
				else if (_q_type.equals(QuestManager.Q_TYPE_LIKERT7)){
					// get value from likert7 scale
					RatingBar rbr = (RatingBar)findViewById(R.id.rbLikert7);
					if (rbr == null)
						Log.e(TAG, "ERROR: rating bar 7 does not exist.");
					else if (rbr.getRating() == 0){
						Context context = getApplicationContext();
						CharSequence text = "Please, select a value before touching 'Done'!!";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
						return;//do not finish the activity
					}
					else{
						Float f = rbr.getRating();
						_q_result = f.toString();
					}
				}
				// type integer
				else if (_q_type.equals(QuestManager.Q_TYPE_INTEGER)){
					// get value from integer number picker
					NumberPicker np = (NumberPicker)findViewById(R.id.item_integer);
					if (np == null)
						Log.e(TAG, "ERROR: number picker does not exist.");
					else{
						int i = np.getValue();
						_q_result = Integer.toString(i);
					}
				}

				// format result to JSON
				_q_result = "{\"q_value\" : \"" 
						+ _q_value 
						+ "\", \"q_result\" : \"" 
						+ _q_result + "\"}";

				// send back the result and finish the activity
				Intent intent = new Intent();
				intent.putExtra(QuestManager.Q_RESULT, _q_result); //value should be a string
				setResult(RESULT_OK, intent); //The data you want to send back

				Log.d(TAG,"sent result: " + _q_result);

				finish();

			}
		});
		///
		///
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_quest, menu);
		return true;
	}



}
