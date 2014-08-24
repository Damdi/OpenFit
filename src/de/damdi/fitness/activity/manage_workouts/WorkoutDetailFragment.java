/**
 * 
 * This is OpenTraining, an Android application for planning your your fitness training.
 * Copyright (C) 2012 Christian Skubich
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

package de.damdi.fitness.activity.manage_workouts;

import de.damdi.fitness.activity.create_workout.ExerciseTypeListActivity;
import de.damdi.fitness.basic.FitnessExercise;
import de.damdi.fitness.basic.Workout;
import de.damdi.fitness.db.DataProvider;
import de.damdi.fitness.db.IDataProvider;
import de.damdi.fitness.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment representing a single Workout detail screen. This fragment is
 * either contained in a {@link WorkoutListActivity} in two-pane mode (on
 * tablets) or a {@link WorkoutDetailActivity} on handsets.
 */
public class WorkoutDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_WORKOUT = "workout";

	/**
	 * The {@link Workout} this fragment is presenting.
	 */
	private Workout mWorkout;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public WorkoutDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);

		if (getArguments().containsKey(ARG_WORKOUT)) {
			mWorkout = (Workout) getArguments().getSerializable(ARG_WORKOUT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_workout_detail, container, false);

		// Show the dummy content as text in a TextView.
		if (mWorkout != null) {
			((TextView) rootView.findViewById(R.id.textview_workout_name)).setText(mWorkout.getName());

			ListView listview_exercises = (ListView) rootView.findViewById(R.id.listview_exercises);
			FitnessExercise[] arr = mWorkout.getFitnessExercises().toArray(new FitnessExercise[mWorkout.getFitnessExercises().size()]);
			ArrayAdapter<FitnessExercise> adapter = new ArrayAdapter<FitnessExercise>(getActivity(), android.R.layout.simple_list_item_2,
					android.R.id.text1, arr);

			listview_exercises.setAdapter(adapter);
		}

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.workout_detail_menu, menu);

		// configure menu_item_rename_workout
		MenuItem menu_item_rename_workout = (MenuItem) menu.findItem(R.id.menu_item_rename_workout);
		menu_item_rename_workout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {

				FragmentTransaction ft = getFragmentManager().beginTransaction();
				Fragment prev = getFragmentManager().findFragmentByTag("dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);

				// Create and show the dialog.
				DialogFragment newFragment = RenameWorkoutDialogFragment.newInstance(mWorkout);
				newFragment.show(ft, "dialog");

				return true;
			}
		});

		// configure menu_item_delete_workout
		MenuItem menu_item_delete_workout = (MenuItem) menu.findItem(R.id.menu_item_delete_workout);
		menu_item_delete_workout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

				builder.setTitle(getString(R.string.really_delete));
				builder.setMessage(getString(R.string.really_delete_long));

				builder.setPositiveButton(getString(R.string.delete_workout), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int wich) {
						IDataProvider dataProvider = new DataProvider(getActivity());
						dataProvider.deleteWorkout(mWorkout);

						if (getActivity() instanceof WorkoutDetailActivity) {
							// request WorkoutListActivity to
							// finish too
							Intent i = new Intent();
							getActivity().setResult(WorkoutListActivity.REQUEST_EXIT, i);
						}

						// finish WorkoutListActivity
						getActivity().finish();

						startActivity(new Intent(getActivity(), WorkoutListActivity.class));

					}
				});
				builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int wich) {
						dialog.dismiss();
					}
				});

				builder.create().show();

				return true;
			}
		});
		

		// configure menu_item_edit_workout
		MenuItem menu_item_edit_workout = (MenuItem) menu.findItem(R.id.menu_item_edit_workout);
		menu_item_edit_workout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				
				Intent editItent = new Intent(getActivity(), ExerciseTypeListActivity.class);
				editItent.putExtra(ExerciseTypeListActivity.ARG_WORKOUT, mWorkout);
				startActivity(editItent);
				
				// close the manage workout activities
				if(getActivity() instanceof WorkoutDetailActivity)
					getActivity().finishFromChild(getActivity());
				getActivity().finish();

				return true;
			}
		});

	}

}
