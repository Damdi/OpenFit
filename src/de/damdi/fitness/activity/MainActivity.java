/**
 * 
 * This is OpenTraining, an Android application for planning your your fitness training.
 * Copyright (C) 2012-2014 Christian Skubich
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package de.damdi.fitness.activity;

import java.util.List;

import de.damdi.fitness.activity.create_workout.ExerciseTypeListActivity;
import de.damdi.fitness.activity.manage_workouts.WorkoutListActivity;
import de.damdi.fitness.activity.settings.SettingsActivity;
import de.damdi.fitness.basic.Workout;
import de.damdi.fitness.db.Cache;
import de.damdi.fitness.db.DataProvider;
import de.damdi.fitness.db.IDataProvider;
import de.damdi.fitness.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import de.damdi.hiittimer.TimerActivity;
//import com.capricorn.ArcMenu;
//import com.capricorn.RayMenu;


public class MainActivity extends ActionBarActivity {
	/** Tag for logging */
	public static final String TAG = MainActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation_layout);
		getSupportActionBar().setIcon(R.drawable.icon_dumbbell);
		
		setUpNavigation();
		
	

		// load data/parse .xml files in background
		final Context mContext = this;
		new Thread() {
			@Override
			public void run() {
				Cache.INSTANCE.updateCache(mContext);
			}
		}.start();

		
		//Launch what's new dialog
		final WhatsNewDialog whatsNewDialog = new WhatsNewDialog(this);
		whatsNewDialog.show(); // (will only be shown if started the first time since last change)
		
		// show disclaimer
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//Boolean showDisclaimer = settings.getBoolean(DisclaimerDialog.PREFERENCE_SHOW_DISCLAIMER, true);
		//if (showDisclaimer) {
		//	new DisclaimerDialog(this);
		//}

		
		
	}
	
	private void setUpNavigation(){
		FancyCoverFlow mFancyCoverFlow = (FancyCoverFlow) this.findViewById(R.id.fancyCoverFlow);

		mFancyCoverFlow.setAdapter(new NavigationGalleryAdapter(this));
		mFancyCoverFlow.setUnselectedAlpha(0.5f);
		mFancyCoverFlow.setUnselectedSaturation(0.0f);
		mFancyCoverFlow.setUnselectedScale(0.2f);
		mFancyCoverFlow.setMaxRotation(35);
		mFancyCoverFlow.setScaleDownGravity(0.0f);
		mFancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
		
		mFancyCoverFlow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					showSelectWorkoutDialog();
					break;
				case 1:
					startActivity(new Intent(MainActivity.this, TimerActivity.class));
					break;
				case 2:
					startActivity(new Intent(MainActivity.this, ExerciseTypeListActivity.class));
					break;
				case 3:
					startActivity(new Intent(MainActivity.this, WorkoutListActivity.class));
					break;
				case 4:
					startActivity(new Intent(MainActivity.this, SettingsActivity.class));
					break;
				default:
					Log.wtf(TAG, "This item should not exist.");
				}
			}
		});
	}



	/**
	 * Configures the ListView for this activity.
	 */
	/*private void setUpList() {
		ListView listview = (ListView) this.findViewById(R.id.activity_main_listview);

		String[] values = new String[] { getString(R.string.create_workout), getString(R.string.manage_workouts),
				getString(R.string.start_training), getString(R.string.settings) };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);

		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					startActivity(new Intent(MainActivity.this, ExerciseTypeListActivity.class));
					break;
				case 1:
					startActivity(new Intent(MainActivity.this, WorkoutListActivity.class));
					break;
				case 2:
					showSelectWorkoutDialog();
					break;
				case 3:
					startActivity(new Intent(MainActivity.this, SettingsActivity.class));
					break;
				default:
					Log.wtf(TAG, "This item should not exist.");
				}
			}
		});
	}*/

	/** Shows a dialog for choosing a {@link Workout} */
	private void showSelectWorkoutDialog() {
		IDataProvider dataProvider = new DataProvider(this);

		// get all Workouts
		final List<Workout> workoutList = dataProvider.getWorkouts();

		Log.d(TAG, "Number of Workouts: " + workoutList.size());
		switch (workoutList.size()) {
		// show error message, if there is no Workout
		case 0:
			Toast.makeText(MainActivity.this, getString(R.string.no_workout), Toast.LENGTH_LONG).show();
			break;
		// choose Workout, if there is/are one or more Workout(s)
		default:
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the dialog.
			DialogFragment newFragment = SelectWorkoutDialogFragment.newInstance();
			newFragment.show(ft, "dialog");
		}

	}

}
