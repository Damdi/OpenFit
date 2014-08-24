/**
 * 
 * This is OpenTraining, an Android application for planning your your fitness training.
 * Copyright (C) 2012-2013 Christian Skubich
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

package de.damdi.fitness.activity.create_exercise;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import de.damdi.fitness.activity.start_training.SwipeDismissListViewTouchListener;
import de.damdi.fitness.basic.Muscle;
import de.damdi.fitness.db.DataProvider;
import de.damdi.fitness.db.IDataProvider;
import de.damdi.fitness.R;

public class MuscleDataFragment extends Fragment implements OnItemSelectedListener{
	private Spinner mMuscleSpinner;
	
	private ListView mMuscleListView;
	private ArrayAdapter<Muscle> mListAdapter;
	private List<Muscle> mMuscleList = new ArrayList<Muscle>();


	public MuscleDataFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_create_exercise_muscle_data, container, false);

		IDataProvider dataProvider = new DataProvider(getActivity());
		
		mMuscleSpinner = (Spinner) layout.findViewById(R.id.spinner_muscle);
		ArrayAdapter<Muscle> madapter = new ArrayAdapter<Muscle>(getActivity(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, dataProvider.getMuscles());
		mMuscleSpinner.setAdapter(madapter);
		// if you dont post a runnable, the first item will be added to the mListAdapter on activity start
		mMuscleSpinner.post(new Runnable() {
			public void run() {
				mMuscleSpinner
						.setOnItemSelectedListener(MuscleDataFragment.this);
				;
			}
		});
		
		mMuscleListView = (ListView) layout.findViewById(R.id.listview_ex_muscle);
		mListAdapter = new ArrayAdapter<Muscle>(getActivity(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, mMuscleList);
		mMuscleListView.setAdapter(mListAdapter);
		
		
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				mMuscleListView,
				new SwipeDismissListViewTouchListener.OnDismissCallback() {
					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							mListAdapter.remove((Muscle)(mListAdapter.getItem(position)));
						}
						mListAdapter.notifyDataSetChanged();
					}
				});
		mMuscleListView.setOnTouchListener(touchListener);			
		
		return layout;
	}
	
	public List<Muscle> getMuscles(){
		return mMuscleList;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Muscle selectedItem = (Muscle) mMuscleSpinner.getItemAtPosition(position);
		if(mMuscleList.contains(selectedItem)){
			Toast.makeText(getActivity(), getString(R.string.muscle_already_in_list), Toast.LENGTH_LONG).show();
			return;
		}
		
		
		mListAdapter.add(selectedItem);
		((CreateExerciseActivity) getActivity()).swipeToDismissAdvise();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}		
}