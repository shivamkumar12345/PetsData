package com.shivam.pets;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PetProvider extends ContentProvider {

    private Database database;
    private static final int PETS = 100;
    private static final int PETS_ID = 101;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS, PETS);

        uriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS + "/#", PETS_ID);
    }

    @Override
    public boolean onCreate() {
        database = new Database(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = database.getReadableDatabase();
        int match = uriMatcher.match(uri);
        Cursor cursor;
        switch (match) {
            case PETS:
                cursor = db.query(PetsContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PETS_ID:
                selection = PetsContract.PetEntry._id + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetsContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("no uri found" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsContract.PetEntry.PET_CONTENT_DIR_TYPE;
            case PETS_ID:
                return PetsContract.PetEntry.PET_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("invalid type" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        int match = uriMatcher.match(uri);
        if (match == PETS) {
            return insertPet(uri, values);
        } else {
            throw new IllegalArgumentException("can't insert");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int deleted_row;
        switch (match) {
            case PETS:
                deleted_row = db.delete(PetsContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PETS_ID:
                selection = PetsContract.PetEntry._id + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deleted_row = db.delete(PetsContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Can't delete table" + uri);
        }
        if (deleted_row != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deleted_row;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = uriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);

            case PETS_ID:
                selection = PetsContract.PetEntry._id + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Invalid uri to update" + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        String name = values.getAsString(PetsContract.PetEntry.COLUMN_PET_NAME);
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name required");
        Integer gender = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_GENDER);
        if (!(gender != null && (gender >= 0 && gender <= 2)))
            throw new IllegalArgumentException("valid gender required");


        Integer weight = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_WEIGHT);
        if (!(weight != null && weight > 0))
            throw new IllegalArgumentException("valid weight required");

        SQLiteDatabase db = database.getWritableDatabase();
        long id = db.insert(PetsContract.PetEntry.TABLE_NAME, null, values);

        if (id > 0) {
            Toast.makeText(getContext(), "saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "error in data", Toast.LENGTH_SHORT).show();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PetsContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsContract.PetEntry.COLUMN_PET_NAME);
            if (name == null || name.isEmpty()) throw new IllegalArgumentException("name required");
        }
        if (values.containsKey(PetsContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_GENDER);
            if (!(gender != null && (gender >= 0 && gender <= 2)))
                throw new IllegalArgumentException("valid gender required");
        }
        if (values.containsKey(PetsContract.PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_WEIGHT);
            if (!(weight != null && weight > 0))
                throw new IllegalArgumentException("valid weight required");
        }

        SQLiteDatabase db = database.getWritableDatabase();
        int updated_row = db.update(PetsContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);

        if (updated_row != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updated_row;
    }
}
