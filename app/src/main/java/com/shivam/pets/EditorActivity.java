/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shivam.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.shivam.pets.PetsContract.PetEntry;
/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private int CURRENT_PET_LOADER=0;

    Uri currentPetUri=null;
    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private boolean hasPetChanged=false;
    private View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hasPetChanged=true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent=getIntent();
         currentPetUri= intent.getData();
         if(currentPetUri!=null){
             setTitle(R.string.editor_activity_title_edit_pet);
             getLoaderManager().initLoader(CURRENT_PET_LOADER,null,this);
         }
         else setTitle(R.string.editor_activity_title_new_pet);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);


        mNameEditText.setOnTouchListener(onTouchListener);
        mBreedEditText.setOnTouchListener(onTouchListener);
        mGenderSpinner.setOnTouchListener(onTouchListener);
        mWeightEditText.setOnTouchListener(onTouchListener );
        setupSpinner();
    }
    @Override
    public void onBackPressed(){
        if(!hasPetChanged){
            super.onBackPressed();
        return; }
        DialogInterface.OnClickListener discardButtonClickListener= new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //to check user clicked on back button
                finish();
            }
        };
        showDialog(discardButtonClickListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        if(currentPetUri==null){
            MenuItem menuItem= menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do insertion data given
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showUnsavedDeleteDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if(!hasPetChanged){
                NavUtils.navigateUpFromSameTask(this);
                return true; }
                else{ DialogInterface.OnClickListener discardButtonClickListener= new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //to check user clicked on back button
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showDialog(discardButtonClickListener);
                return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }
    private void savePet(){
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        String weight = mWeightEditText.getText().toString().trim();
        int weight_int=0;
        try{
            weight_int =Integer.parseInt(weight);
        } catch (Exception e){
            Log.e("Editor_activity","weight can't be converted to int",e);
        }
//        Database database = new Database(this);
//        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,name);
        values.put(PetEntry.COLUMN_PET_BREED,breed);
        values.put(PetEntry.COLUMN_PET_GENDER,mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT,weight_int);
        if(currentPetUri==null) {
             getContentResolver().insert(PetEntry.CONTENT_URI, values);
        }else {
            getContentResolver().update(currentPetUri,values,null,null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection ={PetEntry._id, PetEntry.COLUMN_PET_NAME,PetEntry.COLUMN_PET_BREED,PetEntry.COLUMN_PET_GENDER,PetEntry.COLUMN_PET_WEIGHT};

        return new  CursorLoader(this,currentPetUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor==null|| cursor.getCount()<=0) return;

        while (cursor.moveToNext()){
            int name_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breed_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int gender_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weight_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            mNameEditText.setText(cursor.getString(name_index));
            mBreedEditText.setText(cursor.getString(breed_index));
            mWeightEditText.setText(Integer.toString(cursor.getInt(weight_index)));
            int gender= cursor.getInt(gender_index);
            switch(gender){
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);

            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
    private void showDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_msg);
        builder.setPositiveButton(R.string.discard,  discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_writing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null)dialog.dismiss();
            }
        });
        AlertDialog alertDialog= builder.create();
        alertDialog.show();
    }
    private void showUnsavedDeleteDialog(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage(R.string.sure_to_delete);
        builder.setPositiveButton(R.string.delete,new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which)  {
                deletePet();
                finish();
            }
        });
        builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }
    private void deletePet(){
        if(currentPetUri!=null){
            int row_deleted= getContentResolver().delete(currentPetUri,null,null);
            if(row_deleted!=0){
                Toast.makeText(this,"deleted successfully",Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(this,"could't delete",Toast.LENGTH_SHORT).show();
        }
    }
}