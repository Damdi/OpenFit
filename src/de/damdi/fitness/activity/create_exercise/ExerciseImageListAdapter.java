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

import java.util.HashMap;
import java.util.Map;

import de.damdi.fitness.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ExerciseImageListAdapter extends BaseAdapter {

	private Context mContext;
	private static LayoutInflater mInflater = null;

	private Map<String,Bitmap> mNameImageMap= new HashMap<String,Bitmap>();


	public ExerciseImageListAdapter(Context context, Map<String,Bitmap> nameImageMap) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		mNameImageMap = nameImageMap;
	}

	public int getCount() {
		return mNameImageMap.values().size();
	}

	public Object getItem(int position) {
		if (position > mNameImageMap.size() - 1)
			return null;

		return mNameImageMap.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	public String getImageName(int position){
		return (String) mNameImageMap.keySet().toArray()[position];
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View vi = convertView;

		vi = mInflater.inflate(R.layout.exercise_image_list_row, null);

		return vi;

	}

	public void remove(String name) {
		mNameImageMap.remove(name);
	}	
	
	public void remove(int position){
		mNameImageMap.remove(getImageName(position));
	}
	
	
}
