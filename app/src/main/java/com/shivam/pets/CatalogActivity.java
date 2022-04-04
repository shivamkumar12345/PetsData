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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.shivam.pets.PetsContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Database database;
    private static final int PET_LOADER=0;
    PetAdapter petAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
         database =new Database(this);
            ListView listView = (ListView) findViewById(R.id.list_view);
            View emptyView = findViewById(R.id.empty_view);
            listView.setEmptyView(emptyView);
       petAdapter =new PetAdapter(this,null);
       listView.setAdapter(petAdapter);

       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Intent intent= new Intent(CatalogActivity.this,EditorActivity.class);
               Uri currentItemUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI,id);
               intent.setData(currentItemUri);
               startActivity(intent);
           }
       });

       getLoaderManager().initLoader(PET_LOADER,null,this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do insertion
                insertDummy();
              //  displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
               // displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
  //  private void displayDatabaseInfo(){

//        Cursor  cursor =db.rawQuery("SELECT * FROM "+ PetsContract.PetEntry.TABLE_NAME,null);
//        String[] projection ={PetEntry._id, PetEntry.COLUMN_PET_NAME,PetEntry.COLUMN_PET_BREED,PetEntry.COLUMN_PET_GENDER,PetEntry.COLUMN_PET_WEIGHT};
//        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI,projection,null,null,null);
//        ListView listView =(ListView) findViewById(R.id.list_view);
//        PetAdapter petAdapter = new PetAdapter(this,cursor);
//        listView.setAdapter(petAdapter);
//        try {
//          TextView displayView = (TextView) findViewById(R.id.text_view_pet);
//            displayView.setText("number of rows ="+cursor.getCount());
//            displayView.append("\n"+PetEntry._id+" - "+PetEntry.COLUMN_PET_NAME+" - "+PetEntry.COLUMN_PET_BREED+" - "+PetEntry.COLUMN_PET_GENDER+" - " +PetEntry.COLUMN_PET_WEIGHT);
//            int Id=cursor.getColumnIndex(PetEntry._id);
//            int name_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
//            int breed_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
//            int gender_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
//            int weight_index= cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
//
//            while(cursor.moveToNext()){
//                displayView.append("\n"+cursor.getInt(Id)+" - "+cursor.getString(name_index)+" - "+cursor.getString(breed_index)+" - "+cursor.getInt(gender_index)+" - "+cursor.getInt(weight_index));
//
//            }
//        }finally{
//            cursor.close();
//        }
 //   }
    private void insertDummy(){
      //  SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,"tomyy");
        values.put(PetEntry.COLUMN_PET_BREED,"jhabra");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,10);

        getContentResolver().insert(PetEntry.CONTENT_URI,values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection ={PetEntry._id, PetEntry.COLUMN_PET_NAME,PetEntry.COLUMN_PET_BREED,PetEntry.COLUMN_PET_GENDER,PetEntry.COLUMN_PET_WEIGHT};


        return new CursorLoader(this,PetEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        petAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        petAdapter.swapCursor(null);
    }
}
