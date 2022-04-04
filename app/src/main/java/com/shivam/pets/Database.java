package com.shivam.pets;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.shivam.pets.PetsContract.PetEntry;
import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION =1;
    public static final String DATABASE_NAME ="datafeeder.db";
    public Database( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE = "CREATE TABLE "+ PetEntry.TABLE_NAME+"( "+PetEntry._id+ " INTEGER PRIMARY KEY AUTOINCREMENT "+
                    " , "+ PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL "+ " , " + PetEntry.COLUMN_PET_BREED + " TEXT " + " , "+
                    PetEntry.COLUMN_PET_GENDER +" INTEGER NOT NULL "+ " , " + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
            db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
