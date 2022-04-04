package com.shivam.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

public class PetAdapter extends CursorAdapter {
    public PetAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
       return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name_view = (TextView) view.findViewById(R.id.pet_name);
        TextView breed_view = (TextView) view.findViewById(R.id.pet_breed);
        String name= cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME));
        String breed = cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_BREED));

        if(breed.isEmpty())breed="Unknown";
        name_view.setText(name);
        breed_view.setText(breed);
    }
}
