package com.vlg.alex.superherotestapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.vlg.alex.superherotestapp.data.DatabaseConstants.Hero;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class AddEditFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

   // defines callback method implemented by MainActivity
   interface AddEditFragmentListener {
      // called when hero is saved
      void onAddEditCompleted(Uri HeroUri);
   }
   // constant used to identify the Loader
   private static final int HERO_LOADER = 0;
   private static final int CHECK_PERMISSIONS = 13;

   private AddEditFragmentListener listener;
   private Uri HeroUri; // Uri of selected hero
   private boolean addingNewHero = true; // adding or editing (flag)

   @BindView(R.id. nameTextInputLayout) TextInputLayout nameTextInputLayout;
   @BindView(R.id. realNameTextUnputLayout) TextInputLayout realNameTextInputLayout;
   @BindView(R.id. descriptionTextInputLayout)TextInputLayout descriptionTextInputLayout;
   @BindView(R.id.spinnerUniverse) Spinner spinner;
   @BindView(R.id.ivProfilePhoto) ImageView photo;
   @BindView(R.id.getImageButton) RelativeLayout getButton;
   @BindView(R.id.saveFab) FloatingActionButton saveHeroFAB;
   @BindView(R.id.toolbarAddEdit)Toolbar toolbar;

   // set AddEditFragmentListener when Fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (AddEditFragmentListener) context;
   }

   // remove AddEditFragmentListener when Fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // called when Fragment's view needs to be created
   @Override
   public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // fragment has menu items to display

      // inflate GUI
      View view =
         inflater.inflate(R.layout.fragment_add_edit, container, false);
      ButterKnife.bind(this, view);

      ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
      ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

      // set FloatingActionButton's event listener
      nameTextInputLayout.getEditText().addTextChangedListener(nameChangedListener);
      saveHeroFAB.setOnClickListener(saveHeroButtonClicked);
      updateSaveButtonFAB();

      Bundle arguments = getArguments(); // null if creating new Hero

      // set toolbar title
      if (arguments == null) {
         toolbar.setTitle(getString(R.string.add_label));
      } else {
         toolbar.setTitle(getString(R.string.edit_label));
      }

      if (arguments != null) {
         addingNewHero = false;
         HeroUri = arguments.getParcelable(MainActivity.HERO_URI);
      }

      // if editing an existing Hero, create Loader to get the Hero
      if (HeroUri != null)
         getLoaderManager().initLoader(HERO_LOADER, null, this);
      
      //check runtime permissions and choose photo from storage
      getButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

               requestPermissions(new String[]{
                               Manifest.permission.READ_EXTERNAL_STORAGE},CHECK_PERMISSIONS);

            }else{
               Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
               startActivityForResult(i, 1);
            }

         }
      });

      return view;
   }


   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            getActivity().onBackPressed();
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   // detects when the text changes
   // to hide or show saveButtonFAB
   private final TextWatcher nameChangedListener = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
         int after) {
         updateSaveButtonFAB();
      }

      // called when the text in nameTextInputLayout changes
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
         updateSaveButtonFAB();
      }

      @Override
      public void afterTextChanged(Editable s) {
         updateSaveButtonFAB();
      }
   };


   //receive image and set to imageview
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (resultCode == RESULT_OK && null != data) {
         Uri selectedImage = data.getData();
         String[] filePathColumn = { MediaStore.Images.Media.DATA };
         Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
         cursor.moveToFirst();
         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
         String picturePath = cursor.getString(columnIndex);
         cursor.close();
         photo.setImageBitmap(BitmapFactory.decodeFile(picturePath));
      }
   }


   //convert image from imageview to byteArray
   public byte[] imageViewToByte(ImageView image){
      Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
      return stream.toByteArray();
   }

   // shows saveButtonFAB only if the name is not empty
   private void updateSaveButtonFAB() {
      String input =
         nameTextInputLayout.getEditText().getText().toString();
      // if there is a text in field, show the FloatingActionButton
      if (input.trim().length() != 0 && !nameTextInputLayout.getEditText().getText().equals(""))
         saveHeroFAB.show();
      else
         saveHeroFAB.hide();
   }

   // responds to event generated when user saves a hero
   private final View.OnClickListener saveHeroButtonClicked =
      new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // hide the virtual keyboard
            ((InputMethodManager) getActivity().getSystemService(
               Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
               getView().getWindowToken(), 0);
            saveHero(); // save hero to the database
         }
      };

   // saves hero information to the database
   private void saveHero() {
      // create ContentValues object containing hero's key-value pairs
      ContentValues contentValues = new ContentValues();
      contentValues.put(Hero.COLUMN_NAME,
         nameTextInputLayout.getEditText().getText().toString());
      contentValues.put(Hero.COLUMN_REAL_NAME,
         realNameTextInputLayout.getEditText().getText().toString());
      contentValues.put(Hero.COLUMN_DESCRIPTION,
         descriptionTextInputLayout.getEditText().getText().toString());
      contentValues.put(Hero.COLUMN_PHOTO,imageViewToByte(photo));
      contentValues.put(Hero.COLUMN_UNIVERSE,spinner.getSelectedItem().toString());


      //change MainActivity constant to show snackbar
      if (addingNewHero) {
         Uri newHeroUri = getActivity().getContentResolver().insert(
            Hero.CONTENT_URI, contentValues);
         if (newHeroUri != null) {
            ((MainActivity)getActivity()).setFragmentTransactionMessage(getString(R.string.hero_added));
            listener.onAddEditCompleted(newHeroUri);
         }
         else {
            ((MainActivity)getActivity()).setFragmentTransactionMessage(getString(R.string.hero_not_added));
         }
      }
      else {
         int updatedRows = getActivity().getContentResolver().update(
            HeroUri, contentValues, null, null);
         if (updatedRows > 0) {
            listener.onAddEditCompleted(HeroUri);
            ((MainActivity)getActivity()).setFragmentTransactionMessage(getString(R.string.hero_updated));
         }
         else {
            ((MainActivity)getActivity()).setFragmentTransactionMessage(getString(R.string.hero_not_updated));
         }
      }
   }

   // called by LoaderManager to create a Loader
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      switch (id) {
         case HERO_LOADER:
            return new CursorLoader(getActivity(),
               HeroUri, // Uri of hero to display
               null, // null projection returns all columns
               null, // null selection returns all rows
               null, // no selection arguments
               null); // sort order
         default:
            return null;
      }
   }

   // called by LoaderManager when loading completes
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      // if the hero exists in the database, display its dataset
      if (data != null && data.moveToFirst()) {
         // get the column index for each data item
         int nameIndex = data.getColumnIndex(Hero.COLUMN_NAME);
         int realIndex = data.getColumnIndex(Hero.COLUMN_REAL_NAME);
         int descriptionIndex = data.getColumnIndex(Hero.COLUMN_DESCRIPTION);
         int universeIndex = data.getColumnIndex(Hero.COLUMN_UNIVERSE);
         int photoIndex = data.getColumnIndex(Hero.COLUMN_PHOTO);

         nameTextInputLayout.getEditText().setText(
            data.getString(nameIndex));
         realNameTextInputLayout.getEditText().setText(
            data.getString(realIndex));
         descriptionTextInputLayout.getEditText().setText(
            data.getString(descriptionIndex));
         String compareValue = data.getString(universeIndex);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.universe, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         if (!compareValue.equals(null)) {
            int spinnerPosition = adapter.getPosition(compareValue);
            spinner.setSelection(spinnerPosition);
         }
         byte[] outImage=data.getBlob(photoIndex);
         ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
         Bitmap theImage = BitmapFactory.decodeStream(imageStream);
         photo.setImageBitmap(theImage);
            updateSaveButtonFAB();
      }
   }

   // called by LoaderManager when the Loader is being reset
   @Override
   public void onLoaderReset(Loader<Cursor> loader) { }
}



