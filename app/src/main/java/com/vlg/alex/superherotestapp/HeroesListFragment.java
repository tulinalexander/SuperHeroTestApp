package com.vlg.alex.superherotestapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HeroesListFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

   // callback method implemented by MainActivity
   public interface HeroesFragmentListener {
      // called when hero selected
      void onHeroSelected(Uri heroUri);

      // called when add button is pressed
      void onAddHero();
   }

   private static final int HEROES_LOADER = 0; // identifies Loader

   // used to inform the MainActivity when a hero is selected
   private HeroesFragmentListener listener;
    @BindView(R.id.toolbarHeroes) Toolbar toolbar;
    @BindView(R.id.rvHeroes) RecyclerView recyclerView;
    @BindView(R.id.heroesCoordinator)
   CoordinatorLayout coo;
   private HeroesAdapter heroesAdapter;

   // configures this fragment's GUI
   @Override
   public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // fragment has menu items to display

      // inflate GUI and get reference to the RecyclerView
      View view = inflater.inflate(
         R.layout.fragment_heroes, container, false);

      ButterKnife.bind(this, view);
      toolbar.setTitle(getString(R.string.app_name));
      // recyclerView should display items in a vertical list
      recyclerView.setLayoutManager(
         new LinearLayoutManager(getActivity().getBaseContext()));

      // create recyclerView's adapter and item click listener
      heroesAdapter = new HeroesAdapter(
         new HeroesAdapter.HeroClickListener() {
            @Override
            public void onClick(Uri heroUri) {
               listener.onHeroSelected(heroUri);
            }
         }
      );
      recyclerView.setAdapter(heroesAdapter); // set the adapter

      // attach a custom ItemDecorator to draw dividers between list items
      recyclerView.addItemDecoration(new ItemDivider(getContext()));

      // improves performance if RecyclerView's layout size never changes
      recyclerView.setHasFixedSize(true);


      // get the FloatingActionButton and configure its listener
     final FloatingActionButton addButton =
         (FloatingActionButton) view.findViewById(R.id.addButton);
      addButton.setOnClickListener(
         new View.OnClickListener() {
           // displays the AddEditFragment when FAB is touched
           @Override
            public void onClick(View view) {
               listener.onAddHero();
            }
         }
      );

      //hide FloatingActionButton when list is scrolling down
      recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
         @Override
         public void onScrolled(RecyclerView recyclerView, int dx, int dy){
            if (dy > 0)
               addButton.hide();
            else if (dy < 0)
               addButton.show();
         }
      });
      return view;
   }

   // set HeroesFragmentListener when fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (HeroesFragmentListener) context;
   }

   // remove HeroesFragmentListener when Fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // initialize a Loader when this fragment's activity is created
   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      getLoaderManager().initLoader(HEROES_LOADER, null, this);
   }

   // called from MainActivity when other Fragment's update database
   public void updateHeroesList() {
      heroesAdapter.notifyDataSetChanged();
   }

   // called by LoaderManager to create a Loader
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      // create an appropriate CursorLoader based on the id argument;
      switch (id) {
         case HEROES_LOADER:
            return new CursorLoader(getActivity(),
               Hero.CONTENT_URI, // Uri of heroes table
               null, // null projection returns all columns
               null, // null selection returns all rows
               null, // no selection arguments
               Hero.COLUMN_NAME + " COLLATE NOCASE ASC"); // sort order
         default:
            return null;
      }
   }

   // called by LoaderManager when loading completes
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      heroesAdapter.swapCursor(data);
   }

   // called by LoaderManager when the Loader is being reset
   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
      heroesAdapter.swapCursor(null);
   }


   @Override
   public void onResume() {
      super.onResume();

      // Check for messages in main activity
      String message = ((MainActivity)getActivity()).getFragmentTransactionMessage();

      // If any, display as snackbar
      if(message != null) {
         Snackbar snack = Snackbar.make(coo, message, Snackbar.LENGTH_SHORT);
         snack.show();

         // Reset message in activity
         ((MainActivity)getActivity()).resetFragmentTransactionMessage();
      }
   }


}

