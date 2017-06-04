package com.vlg.alex.superherotestapp.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseConstants {
   static final String AUTHORITY =
      "com.vlg.alex.superherotestapp.data";

   // base URI used to interact with the ContentProvider
   private static final Uri BASE_CONTENT_URI =
      Uri.parse("content://" + AUTHORITY);

   // nested class defines contents of the heroes table
   public static final class Hero implements BaseColumns {
      static final String TABLE_NAME = "heroes"; // table's name

      // define for the heroes table
      public static final Uri CONTENT_URI =
         BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

      // column names for sqlite
      public static final String COLUMN_NAME = "name";
      public static final String COLUMN_REAL_NAME = "real";
      public static final String COLUMN_DESCRIPTION = "description";
      public static final String COLUMN_UNIVERSE = "universe";
      public static final String COLUMN_PHOTO = "photo";

      // creates a Uri for a hero
      public static Uri buildHeroUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }
   }
}


