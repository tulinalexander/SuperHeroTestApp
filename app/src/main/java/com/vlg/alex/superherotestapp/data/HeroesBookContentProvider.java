package com.vlg.alex.superherotestapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.vlg.alex.superherotestapp.R;
import static com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero.TABLE_NAME;
import static com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero.buildHeroUri;


public class HeroesBookContentProvider extends ContentProvider {
   // used to access to database
   private HeroesBookDatabaseHelper dbHelper;

   // UriMatcher helps ContentProvider determine operation to perform
   private static final UriMatcher uriMatcher =
      new UriMatcher(UriMatcher.NO_MATCH);

   // constants used with UriMatcher to determine operation to perform
   private static final int ONE_HERO = 1; // manipulate one hero
   private static final int HEROES = 2; // manipulate heroes table

   // static block to configure this ContentProvider's UriMatcher
   static {
      // Uri for Hero with the specified id (#)
      uriMatcher.addURI(DatabaseConstants.AUTHORITY,
         TABLE_NAME + "/#", ONE_HERO);

      // Uri for Heroes table
      uriMatcher.addURI(DatabaseConstants.AUTHORITY,
         TABLE_NAME, HEROES);
   }

   // called when the HeroesBookContentProvider is created
   @Override
   public boolean onCreate() {
      // create the HeroesBookDatabaseHelper
      dbHelper = new HeroesBookDatabaseHelper(getContext());
      return true;
   }

   // required method: Not used in this app, so we return null
   @Override
   public String getType(Uri uri) {
      return null;
   }

   // query the database
   @Override
   public Cursor query(Uri uri, String[] projection,
      String selection, String[] selectionArgs, String sortOrder) {

      // create SQLiteQueryBuilder for querying heroes table
      SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
      queryBuilder.setTables(TABLE_NAME);

      switch (uriMatcher.match(uri)) {
         case ONE_HERO: // hero with specified id will be selected
            queryBuilder.appendWhere(
               BaseColumns._ID + "=" + uri.getLastPathSegment());
            break;
         case HEROES: // all heroes will be selected
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_query_uri) + uri);
      }

      // execute the query to select one or all heroes
      Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
         projection, selection, selectionArgs, null, null, sortOrder);

      // configure to watch for content changes
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
      return cursor;
   }

   // insert a new hero in the database
   @Override
   public Uri insert(Uri uri, ContentValues values) {
      Uri newHeroUri = null;

      switch (uriMatcher.match(uri)) {
         case HEROES:
            long rowId = dbHelper.getWritableDatabase().insert(
               TABLE_NAME, null, values);
            if (rowId > 0) {
               newHeroUri = buildHeroUri(rowId);
               // notify observers that the database changed
               getContext().getContentResolver().notifyChange(uri, null);
            }
            else
               throw new SQLException(
                  getContext().getString(R.string.insert_failed) + uri);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_insert_uri) + uri);
      }

      return newHeroUri;
   }

   // update an existing hero in the database
   @Override
   public int update(Uri uri, ContentValues values,
      String selection, String[] selectionArgs) {
      int numberOfRowsUpdated; // 1 if update successful; 0 otherwise

      switch (uriMatcher.match(uri)) {
         case ONE_HERO:
            String id = uri.getLastPathSegment();
            // update the hero
            numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
               TABLE_NAME, values, BaseColumns._ID + "=" + id,
               selectionArgs);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_update_uri) + uri);
      }

      // if changes were made, notify observers that the database changed
      if (numberOfRowsUpdated != 0) {
         getContext().getContentResolver().notifyChange(uri, null);
      }

      return numberOfRowsUpdated;
   }

   // delete an existing hero from the database
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      int numberOfRowsDeleted;

      switch (uriMatcher.match(uri)) {
         case ONE_HERO:
            // get from the uri the id of hero to update
            String id = uri.getLastPathSegment();
            // delete the hero
            numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
               TABLE_NAME, BaseColumns._ID + "=" + id, selectionArgs);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_delete_uri) + uri);
      }

      // notify observers that the database changed
      if (numberOfRowsDeleted != 0) {
         getContext().getContentResolver().notifyChange(uri, null);
      }

      return numberOfRowsDeleted;
   }
}
