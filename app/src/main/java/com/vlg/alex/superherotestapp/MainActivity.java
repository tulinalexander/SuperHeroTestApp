package com.vlg.alex.superherotestapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.vlg.alex.superherotestapp.data.DatabaseConstants;

public class MainActivity extends AppCompatActivity
        implements HeroesListFragment.HeroesFragmentListener,
        DetailFragment.DetailFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    // key for storing a hero Uri in a Bundle passed to a fragment
    public static final String HERO_URI = "heroes_uri";
    public static String HERO_ID = null;
    private String fragmentTransactionMessage;

    private HeroesListFragment heroesListFragment; // displays heroes list


    // display HeroesListFragment when MainActivity first loads
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHeroes);
        setSupportActionBar(toolbar);

        heroesListFragment = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("key")) {
            String  value = String.valueOf(extras.get("key"));
            HERO_ID = value;
            Long row = Long.parseLong(value);
            displayHero(DatabaseConstants.Hero.buildHeroUri(row), R.id.fragmentContainer,false);
        } else if (savedInstanceState == null &&
                findViewById(R.id.fragmentContainer) != null) {
            // create HeroesListFragment
            heroesListFragment = new HeroesListFragment();

            // add the fragment to the FrameLayout
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, heroesListFragment);
            transaction.commit(); // display HeroesListFragment
        }
        else {
            heroesListFragment =
                    (HeroesListFragment) getSupportFragmentManager().
                            findFragmentById(R.id.heroesFragment);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String  value = String.valueOf(extras.get("key"));
            Long row = Long.parseLong(value);
            displayHero(DatabaseConstants.Hero.buildHeroUri(row), R.id.fragmentContainer, false);
        }
    }


    // display DetailFragment for selected hero
    @Override
    public void onHeroSelected(Uri heroUri) {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayHero(heroUri, R.id.fragmentContainer,true);
        else {
            // removes top of back stack
            getSupportFragmentManager().popBackStack();
            displayHero(heroUri, R.id.rightPaneContainer,true);
        }
    }

    // display AddEditFragment to add a new hero
    @Override
    public void onAddHero() {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, null);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, null);
    }

    // display a hero
    public void displayHero(Uri heroUri, int viewID, boolean showMenu) {
        Bundle extras = getIntent().getExtras();
        DetailFragment detailFragment = new DetailFragment();

        // specify heroes Uri as an argument to the DetailFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(HERO_URI, heroUri);
        if(!showMenu){
            arguments.putBoolean("show",false);
        }

        detailFragment.setArguments(arguments);
        // use a FragmentTransaction to display the DetailFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailFragment);
        if (extras != null && extras.containsKey("key")) {
        } else {
            transaction.addToBackStack(null);
        }
        transaction.commit(); // causes DetailFragment to display
    }

    // display fragment for adding a new or editing an existing hero
    private void displayAddEditFragment(int viewID, Uri heroUri) {
        AddEditFragment addEditFragment = new AddEditFragment();

        // if editing existing hero, provide heroUri as an argument
        if (heroUri != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(HERO_URI, heroUri);
            addEditFragment.setArguments(arguments);
        }

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes AddEditFragment to display
    }

    // return to hero list when displayed hero deleted
    @Override
    public void onHeroDeleted() {
        // removes top of back stack
        getSupportFragmentManager().popBackStack();

        heroesListFragment.updateHeroesList(); // refresh heroes
    }

    // display the AddEditFragment to edit an existing hero
    @Override
    public void onEditHero(Uri heroUri) {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, heroUri);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, heroUri);
    }

    // update GUI after new hero or updated hero saved
    @Override
    public void onAddEditCompleted(Uri heroUri) {
        // removes top of back stack
        getSupportFragmentManager().popBackStack();
        heroesListFragment.updateHeroesList(); // refresh heroes

        if (findViewById(R.id.fragmentContainer) == null) { // tablet
            // removes top of back stack
            getSupportFragmentManager().popBackStack();

            // on tablet, display hero that was just added or edited
            displayHero(heroUri, R.id.rightPaneContainer,true);
        }
    }


    // Three methods used to send information (text) between fragments
    public void setFragmentTransactionMessage(String message) {
        this.fragmentTransactionMessage = message;
    }

    public String getFragmentTransactionMessage() {
        return fragmentTransactionMessage;
    }

    public void resetFragmentTransactionMessage() {
        this.fragmentTransactionMessage = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent updateWidget = new Intent(getApplicationContext(), myWidget.class); // Widget.class is your widget class
        updateWidget.setAction("update_widget");
        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, updateWidget, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pending.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


}