package app.studio.crafty.wallpaper.daily.dailywallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import org.michaelevans.colorart.library.ColorArt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import utils.AppController;

import static app.studio.crafty.wallpaper.daily.dailywallpaper.R.id.toolbar;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Main activity";
    ImageView imageView;
    Bitmap imageBitmap;

    SpaceNavigationView spaceNavigationView;
    boolean isAutoSettingCurrentItem = false;
    boolean isAutoSettingCurrentItem1 = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(imageBitmap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        });
        fab.hide();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        imageView = (ImageView) findViewById(R.id.imageView2);
        loadImage();

        spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);

        spaceNavigationView.addSpaceItem(new SpaceItem("Save", R.drawable.ic_menu_gallery));
        spaceNavigationView.addSpaceItem(new SpaceItem("Set", R.drawable.ic_menu_camera));


        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                //Toast.makeText(MainActivity.this, "onCentreButtonClick", Toast.LENGTH_SHORT).show();
                refreshWallPaper();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                //Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();

                if (itemIndex == 1) {
                    if (!isAutoSettingCurrentItem1) {

                        setAsWallPaper();
                    }
                    isAutoSettingCurrentItem1 =false;



                } else if (itemIndex == 0) {
                    if (!isAutoSettingCurrentItem) {
                        Toast.makeText(MainActivity.this, "Image saved in " + saveToInternalStorage(imageBitmap, "Wall_"), Toast.LENGTH_LONG).show();
                        //spaceNavigationView.setCentreButtonSelected();
                        scheduleWallChangeJob();
                    }
                    isAutoSettingCurrentItem = false;
                }

            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                //Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
                if (itemIndex == 0) {
                    if (!isAutoSettingCurrentItem) {
                        Toast.makeText(MainActivity.this, "Image saved in reselected" + saveToInternalStorage(imageBitmap, "Wall_"), Toast.LENGTH_LONG).show();
                        //spaceNavigationView.setCentreButtonSelected();
                        scheduleWallChangeJob();
                    }
                    isAutoSettingCurrentItem = false;

                }
                else if (itemIndex==1){
                    if (!isAutoSettingCurrentItem1) {

                        setAsWallPaper();
                    }
                    isAutoSettingCurrentItem1 =false;


                }
            }
        });


        spaceNavigationView.setCentreButtonSelectable(true);
        setSpaceNavHeight();
        spaceNavigationView.setSpaceBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                spaceNavigationView.setVisibility(View.INVISIBLE);

                return true;
                //return false to achivec if preview only when user is holding image view
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spaceNavigationView.getVisibility()==View.INVISIBLE
                        || spaceNavigationView.getVisibility()== View.GONE){
                    spaceNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void setSpaceViewColor() {

        if (imageBitmap != null) {
            ColorArt colorArt = new ColorArt(imageBitmap);
            spaceNavigationView.invalidate();

            //spaceNavigationView.changeSpaceBackgroundColor(ContextCompat.getColor(this, colorArt.getBackgroundColor()));
            spaceNavigationView.changeSpaceBackgroundColor(colorArt.getBackgroundColor());
            spaceNavigationView.setInActiveSpaceItemColor(colorArt.getPrimaryColor());
            spaceNavigationView.setActiveSpaceItemColor(colorArt.getDetailColor());
            spaceNavigationView.setCentreButtonColor(colorArt.getSecondaryColor());
            spaceNavigationView.setActiveCentreButtonBackgroundColor(colorArt.getSecondaryColor());

            isAutoSettingCurrentItem = true;
            spaceNavigationView.changeCurrentItem(0);
            isAutoSettingCurrentItem1 = true;
            spaceNavigationView.changeCurrentItem(1);


            spaceNavigationView.setCentreButtonSelected();


            //Toast.makeText(this, "Color changed to "+colorArt.getBackgroundColor(), Toast.LENGTH_SHORT).show();

        }
    }

    private void setSpaceNavHeight() {
        int margin = getNavBarHeight(this);

        LinearLayout linear = (LinearLayout) findViewById(R.id.space_viewGroup_layout);


        View view = (View) findViewById(R.id.space_bottomView);

        view.getLayoutParams().height = margin;


    }

    private void hideStatusBar() {


        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

        );

    }

    private void refreshWallPaper() {
        loadImage();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }

    public void loadImage() {


        ImageLoader imageLoader = AppController.getInstance().getImageLoader();


// If you are using normal ImageView
        String url = "https://source.unsplash.com/category/nature/1080x1920";

        imageLoader.get(url, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Image Load Error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    imageView.setImageBitmap(response.getBitmap());
                    imageBitmap = response.getBitmap();
                    setSpaceViewColor();
                    Toast.makeText(MainActivity.this, "Image loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setAsWallPaper() {

        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallpaperManager.setBitmap(imageBitmap);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    private boolean saveToInternalStorage(Bitmap bitmapImage, String filename) {
        //get path to external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory() + "/myAppDir/myImages/";
        File sdIconStorageDir = new File(iconsStoragePath);

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();

        try {
            String filePath = sdIconStorageDir.toString() + File.separator + filename + System.currentTimeMillis() + ".png";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //choose another format if PNG doesn't suit you
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }

        return true;

    }

    public int getNavBarHeight(Context c) {
        int result = 0;
        boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar
            Resources resources = getResources();

            int orientation = getResources().getConfiguration().orientation;
            int resourceId;
            if (isTablet(c)) {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                return getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public void scheduleWallChangeJob(){

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));


        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value");

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(WallPaperChangerJob.class)
                // uniquely identifies the job
                .setTag("my-wallchanger_job")
                // one-off job
                .setRecurring(false)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 60 seconds from now
                .setTrigger(Trigger.executionWindow(3600, 7200))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                        //Constraint.ON_UNMETERED_NETWORK,
                        // only run when the device is charging
                        //Constraint.DEVICE_CHARGING
                )
                .setExtras(myExtrasBundle)
                .build();

        dispatcher.mustSchedule(myJob);

    }


}
