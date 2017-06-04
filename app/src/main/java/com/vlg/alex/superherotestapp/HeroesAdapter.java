package com.vlg.alex.superherotestapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero;

import java.io.ByteArrayInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

class HeroesAdapter
   extends RecyclerView.Adapter<HeroesAdapter.ViewHolder> {



   // interface implemented by HeroesFragment to respond
   // when the user touches an item in the RecyclerView
   interface HeroClickListener {
      void onClick(Uri heroUri);
   }


   // HeroesAdapter instance variables
   private Cursor cursor = null;
   private final HeroClickListener clickListener;

   // nested subclass of RecyclerView.ViewHolder used to implement
   // the view-holder pattern in the context of a RecyclerView
   public class ViewHolder extends RecyclerView.ViewHolder {
      @BindView(R.id.tvNameItem)  TextView textView;
      @BindView(R.id.ivImageItem) ImageView imageView;
      private long rowID;

      // configures a RecyclerView item's ViewHolder
      public ViewHolder(View itemView) {
         super(itemView);
         ButterKnife.bind(this, itemView);
         Typeface type = Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/10322.ttf");
         textView.setTypeface(type);

         // attach listener to itemView
         itemView.setOnClickListener(
            new View.OnClickListener() {
               // executes when the hero in this ViewHolder is clicked
               @Override
               public void onClick(View view) {
                  clickListener.onClick(Hero.buildHeroUri(rowID));
               }
            }
         );
      }
      // set the database row ID for the hero in this ViewHolder
      public void setRowID(long rowID) {
         this.rowID = rowID;
      }
   }

   public HeroesAdapter(HeroClickListener clickListener) {
      this.clickListener = clickListener;
   }

   // sets up new list item and its ViewHolder
   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
      return new ViewHolder(view); // return current item's ViewHolder
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      cursor.moveToPosition(position);
      holder.setRowID(cursor.getLong(cursor.getColumnIndex(Hero._ID)));
      holder.textView.setText(cursor.getString(cursor.getColumnIndex(
         Hero.COLUMN_NAME)));
      byte[] outImage=cursor.getBlob(cursor.getColumnIndex(Hero.COLUMN_PHOTO));
      ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
      Bitmap theImage = BitmapFactory.decodeStream(imageStream);
      holder.imageView.setImageBitmap(theImage);
   }

   // returns the number of items that adapter binds
   @Override
   public int getItemCount() {
      return (cursor != null) ? cursor.getCount() : 0;
   }

   // swap this adapter's current Cursor for a new one
   public void swapCursor(Cursor cursor) {
      this.cursor = cursor;
      notifyDataSetChanged();
   }
}


