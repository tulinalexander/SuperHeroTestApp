package com.vlg.alex.superherotestapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero;

import java.io.ByteArrayInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

   // callback methods implemented by MainActivity
   public interface DetailFragmentListener {
      void onHeroDeleted(); // called when a hero is deleted

      // pass Uri of hero to edit to the DetailFragmentListener
      void onEditHero(Uri heroUri);
   }

   private static final int HERO_LOADER = 0; // identifies the Loader
   Bundle arguments;
   private DetailFragmentListener listener; // MainActivity
   private Uri heroUri; // Uri of selected hero
   @BindView(R.id.imPhoto) ImageView photo;
   @BindView(R.id.tvName) TextView nameTextView;
   @BindView(R.id.tvRealName) TextView realNameTextView;
   @BindView(R.id.tvDescription) TextView descTextView;
   @BindView(R.id.tvUniverse) TextView universeTextView;
   @BindView(R.id.toolbarDetail) Toolbar toolbar;
   @BindView(R.id.splash) RelativeLayout splash;
   @BindView(R.id.toolbarLayout) CollapsingToolbarLayout toolbarLayout;


   // set DetailFragmentListener when fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (DetailFragmentListener) context;
   }

   // remove DetailFragmentListener when fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // called when DetailFragmentListener's view needs to be created
   @Override
   public View onCreateView(
           LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // this fragment has menu items to display

      // get Bundle of arguments then extract the hero Uri
      arguments = getArguments();

      if (arguments != null)
         heroUri = arguments.getParcelable(MainActivity.HERO_URI);

      // inflate DetailFragment's layout
      View view =
              inflater.inflate(R.layout.fragment_detail, container, false);
      ButterKnife.bind(this,view);

      Typeface type = Typeface.createFromAsset(getContext().getAssets(),"fonts/10322.ttf");
      nameTextView.setTypeface(type);
      realNameTextView.setTypeface(type);
      descTextView.setTypeface(type);
      universeTextView.setTypeface(type);

      ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
      ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

      // load the hero
      getLoaderManager().initLoader(HERO_LOADER, null, this);
      return view;
   }


   // display this fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.fragment_details_menu, menu);
      if(arguments.containsKey("show")){
         menu.setGroupVisible(R.id.main_menu_group,false);
      }
   }

   // handle menu item selections
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
          case android.R.id.home:
              getActivity().onBackPressed();
              return true;
          case R.id.action_edit:
            listener.onEditHero(heroUri); // pass Uri to listener
            return true;
          case R.id.action_delete:
            FragmentManager fm = getChildFragmentManager();
            myDialogFragment editNameDialogFragment = myDialogFragment.newInstance(heroUri.toString());
            editNameDialogFragment.show(fm, "fragment_delete");
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   // called by LoaderManager to create a Loader
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      // create an appropriate CursorLoader based on the id argument;
      CursorLoader cursorLoader;
      switch (id) {
         case HERO_LOADER:
            cursorLoader = new CursorLoader(getActivity(),
                    heroUri, // Uri of hero to display
                    null, // null projection returns all columns
                    null, // null selection returns all rows
                    null, // no selection arguments
                    null); // sort order
            break;
         default:
            cursorLoader = null;
            break;
      }
      return cursorLoader;
   }

   // called by LoaderManager when loading completes
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      // if the hero exists in the database, display its dataset
      if (data != null && data.moveToFirst()) {
         // get the column index for each data item
         int photoIndex = data.getColumnIndex(Hero.COLUMN_PHOTO);
         int nameIndex = data.getColumnIndex(Hero.COLUMN_NAME);
         int realNameIndex = data.getColumnIndex(Hero.COLUMN_REAL_NAME);
         int descriptionIndex = data.getColumnIndex(Hero.COLUMN_DESCRIPTION);
         int universeIndex = data.getColumnIndex(Hero.COLUMN_UNIVERSE);

         // fill TextViews with the retrieved data
         nameTextView.setText(data.getString(nameIndex));
         toolbarLayout.setTitle(data.getString(nameIndex));
         textSetter(realNameTextView,data.getString(realNameIndex));
         textSetter(descTextView,data.getString(descriptionIndex));
         textSetter(universeTextView,data.getString(universeIndex));
         byte[] outImage=data.getBlob(photoIndex);
         ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
         Bitmap theImage = BitmapFactory.decodeStream(imageStream);
         photo.setImageBitmap(theImage);
         splash.setVisibility(View.GONE);
      }
   }

   // called by LoaderManager when the Loader is being reset
   @Override
   public void onLoaderReset(Loader<Cursor> loader) { }

   public void textSetter(TextView view, String data){
      if(data.isEmpty()){
         view.setTextColor(Color.parseColor("#FF0000"));
         view.setText(getString(R.string.hero_no_field));
      } else
          view.setText(data);
   }

}
