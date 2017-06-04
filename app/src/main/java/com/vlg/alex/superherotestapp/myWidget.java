package com.vlg.alex.superherotestapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;

import com.vlg.alex.superherotestapp.data.DatabaseConstants;
import com.vlg.alex.superherotestapp.data.HeroesBookDatabaseHelper;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class myWidget extends AppWidgetProvider {

    final String LOG_TAG = "myWidgetLogs";
    int[] allWidgetIds;


    //call when widget get pending intent
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals("update_widget")) {
            onUpdate(context, AppWidgetManager.getInstance(context),allWidgetIds);
        }

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
    }

    //call when widget update
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        HeroesBookDatabaseHelper dbHelper = new HeroesBookDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ComponentName thisWidget = new ComponentName(context,
                myWidget.class);
        allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            // take random raw from database
            Cursor cursor = db.rawQuery("SELECT * FROM heroes ORDER BY RANDOM() LIMIT 1;", null);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget);

            //set placeholder if dataset is empty
            if(!cursor.moveToPosition(0)){
                remoteViews.setTextViewText(R.id.tvWidget,context.getString(R.string.app_widget_no_data));
                remoteViews.setImageViewResource(R.id.imgWidget,R.drawable.widget_placeholder);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            } else {
                // set data to widget fields
                cursor.moveToPosition(0);
                String widgetNameShow = cursor.getString(cursor.getColumnIndex(DatabaseConstants.Hero.COLUMN_NAME));
                int row = cursor.getInt(cursor.getColumnIndex("_id"));
                remoteViews.setTextViewText(R.id.tvWidget, widgetNameShow);
                byte[] outImage=cursor.getBlob(cursor.getColumnIndex(DatabaseConstants.Hero.COLUMN_PHOTO));
                ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap theImage = BitmapFactory.decodeStream(imageStream,null,options);
                remoteViews.setBitmap(R.id.imgWidget,"setImageBitmap",theImage);

                // make intent for click on widget
                Intent configIntent = new Intent(context, MainActivity.class);
                configIntent.putExtra("key", row);
                configIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                PendingIntent configPendingIntent = PendingIntent.getActivity(context, row, configIntent, 0);

                // set onclicklistener for widget
                remoteViews.setOnClickPendingIntent(R.id.imgWidget, configPendingIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }

            //close connection with database
            dbHelper.close();
            db.close();
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");

    }




}