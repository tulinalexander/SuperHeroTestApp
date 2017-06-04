package com.vlg.alex.superherotestapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero;

public class HeroesBookDatabaseHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "heroes.db";
   private static final int DATABASE_VERSION = 1;


   public HeroesBookDatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   // creates the heroes table when the database is created
   @Override
   public void onCreate(SQLiteDatabase db) {
      // SQL for creating the heroes table
      final String CREATE_HEROES_TABLE =
         "CREATE TABLE " + Hero.TABLE_NAME + "(" +
         Hero._ID + " integer primary key, " +
         Hero.COLUMN_NAME + " TEXT, " +
         Hero.COLUMN_REAL_NAME + " TEXT, " +
         Hero.COLUMN_DESCRIPTION + " TEXT, " +
         Hero.COLUMN_UNIVERSE + " TEXT, " +
         Hero.COLUMN_PHOTO + " BLOB);";
      db.execSQL(CREATE_HEROES_TABLE); // create the heroes table
   }


   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion,
      int newVersion) { }
}


