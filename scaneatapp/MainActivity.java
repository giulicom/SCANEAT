package com.app.project.scaneatapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.project.scaneatapp.notifications.MyFirebaseMessagingService;
import com.firebase.client.Firebase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scaneat.R;


public class MainActivity extends AppCompatActivity {

    private NavigationView mNavigationView;

    private Menu drawerMenu;
    private MenuItem esci;
    private MenuItem nonLoggato;
    private MenuItem registrati;
    private MenuItem areaPersonale;


    private MenuInflater inflater;
    private SharedPreferences settings;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    FragmentManager mFragmentManager;

    private ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;

    private boolean logged;
    private boolean activate;

    private Menu menu_action_bar;

    public static MainActivity activityInstance = new MainActivity();

    public static MainActivity getInstance() {
        return activityInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaneat_main);
        Firebase.setAndroidContext(this);
        settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("not_first_time", false);
        editor.apply();

        logged = settings.getBoolean("logged", false);

        if(logged && Utils.isOnline(MainActivity.this)){
            onTokenRefresh();
        }

        if(!TextUtils.isEmpty(getIntent().getStringExtra(MyFirebaseMessagingService.FROM_NOTIFICATION))){
            MyFirebaseMessagingService.clearMessages();
            MyFirebaseMessagingService.resetNotificationID();
        }

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerToggle.onDrawerSlide(drawerView,slideOffset);

                logged = settings.getBoolean("logged", false);

                drawerMenu = mNavigationView.getMenu();

                esci = drawerMenu.findItem(R.id.logout);
                nonLoggato = drawerMenu.findItem(R.id.unlogged);
                registrati = drawerMenu.findItem(R.id.signup_nav_bar);
                areaPersonale = drawerMenu.findItem(R.id.personal_area);

                ImageView circleView_logged = (ImageView) mNavigationView.findViewById(R.id.circleView_logged);
                ImageView circleView_unlogged = (ImageView) mNavigationView.findViewById(R.id.circleView_unlogged);
                TextView nome_account = (TextView) mNavigationView.findViewById(R.id.name_account);
                TextView email_account = (TextView) mNavigationView.findViewById(R.id.email_account);

                /** controllo se login effettuato **/

                if (logged) {
                    /*************
                     *  LOGGED
                     ************/

                    String email = settings.getString("email", "");
                    String name = settings.getString("name", "");

                    circleView_unlogged.setVisibility(View.GONE);
                    circleView_logged.setVisibility(View.VISIBLE);
                    nome_account.setText(name);
                    nome_account.setVisibility(View.VISIBLE);
                    email_account.setText(email);
                    email_account.setVisibility(View.VISIBLE);

                    /** menu **/
                    nonLoggato.setVisible(false);
                    registrati.setVisible(false);
                    esci.setVisible(true);
                    areaPersonale.setVisible(true);
                } else {
                    /**********
                     * UNLOGGED
                     ************/

                    circleView_unlogged.setVisibility(View.VISIBLE);
                    circleView_logged.setVisibility(View.GONE);
                    nome_account.setText("");
                    nome_account.setVisibility(View.VISIBLE);
                    email_account.setText("");
                    email_account.setVisibility(View.GONE);

                    /** menu **/

                    esci.setVisible(false);
                    areaPersonale.setVisible(false);
                    //loggato.setVisible(false);
                    nonLoggato.setVisible(true);
                    registrati.setVisible(true);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {


            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.containerView, new TabFragment()).commit();



        /****************************
         * LISTENER NAVIGATION MENU
         ****************************/
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                viewPager = (ViewPager) findViewById(R.id.viewpager);
                mDrawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.nav_item_lists) {//List
                    setupViewPager(viewPager, 0);
                }
                if (menuItem.getItemId() == R.id.nav_item_search) {//Search
                    setupViewPager(viewPager, 1);
                }
                if (menuItem.getItemId() == R.id.unlogged) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.putExtra("origin", "1");
                    startActivity(intent);
                    logged = settings.getBoolean("logged", false);
                }
                if (menuItem.getItemId() == R.id.logout) {
                    Log.d("Log Out", "");
                    logout();
                }
                if(menuItem.getItemId() == R.id.personal_area) {
                    Intent intent = new Intent(getApplicationContext(), PersonalAreaActivity.class);
                    startActivity(intent);
                }
                if (menuItem.getItemId() == R.id.signup_nav_bar) {
                    Log.d("Main: ", "goto signup");
                    Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                    startActivity(intent);
                }
                if (menuItem.getItemId() == R.id.setting_nav_drw) {//settings
                    Intent intent = new Intent(getApplicationContext(), MyPreferencesActivity.class);
                    startActivity(intent);
                }
                if (menuItem.getItemId() == R.id.credits_nav_drw) { //info
                    Intent intent = new Intent(getApplicationContext(), CreditsActivity.class);
                    startActivity(intent);
                }

                Log.d("fine", "oncreate");
                return false;
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        setSupportActionBar(toolbar);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


    }

    /******************
     * ACTION BAR MENU
     ****************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("main activity", "oncreateoptionsmenu");
        super.onCreateOptionsMenu(menu);
        inflater = getMenuInflater();
        menu_action_bar = menu;

        // controllo se utente loggato
        boolean loggin = settings.getBoolean("logged", false);
        inflater.inflate(R.menu.menu_actionbar, menu);
        if (!loggin) { // not logged
            MenuItem login = menu.findItem(R.id.login_menu);
            login.setVisible(true);
            MenuItem logout = menu.findItem(R.id.logout_menu);
            logout.setVisible(false);
        } else { //loggato
            MenuItem login = menu.findItem(R.id.login_menu);
            login.setVisible(false);
            MenuItem logout = menu.findItem(R.id.logout_menu);
            logout.setVisible(true);

        }

        activate = true;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Log.d("onoptions", "itemselected");
        switch (item.getItemId()) {
            case R.id.login_menu:
                intent = new Intent(this, LoginActivity.class);
                intent.putExtra("origin", "1");
                startActivityForResult(intent, 1);
                logged = settings.getBoolean("logged", false);

                return true;
            case R.id.sync_menu:
                SyncExtDB syncDB = new SyncExtDB(this, MainActivity.this);
                syncDB.syncDB(settings.getString("lastUpdate","").toString());
                return true;
            case R.id.logout_menu:
                logout();
        }

        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    private void setupUserMenu() {

        /*********************************
         * PER LEGGERE I VALORI DI LOGIN *
         *********************************/

        boolean loggin = settings.getBoolean("logged", false);
        String email = settings.getString("email", "");
        String name = settings.getString("name", "");

        /**********************************
         *
         **********************************/
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        drawerMenu = mNavigationView.getMenu();


        esci = drawerMenu.findItem(R.id.logout);
        nonLoggato = drawerMenu.findItem(R.id.unlogged);
        registrati = drawerMenu.findItem(R.id.signup_nav_bar);
        areaPersonale = drawerMenu.findItem(R.id.personal_area);


        /** voci header **/
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);

        ImageView circleView_logged = (ImageView) navigationView.findViewById(R.id.circleView_logged);
        ImageView circleView_unlogged = (ImageView) navigationView.findViewById(R.id.circleView_unlogged);
        TextView nome_account = (TextView) navigationView.findViewById(R.id.name_account);
        TextView email_account = (TextView) navigationView.findViewById(R.id.email_account);


        /** controllo se login effettuato **/

        if (loggin) {
            /*************
             *  LOGGED
             ************/
            /** header **/
            circleView_unlogged.setVisibility(View.GONE);
            circleView_logged.setVisibility(View.VISIBLE);
            nome_account.setText(name);
            nome_account.setVisibility(View.VISIBLE);
            email_account.setText(email);
            email_account.setVisibility(View.VISIBLE);

            /** menu **/
            nonLoggato.setVisible(false);
            registrati.setVisible(false);
            esci.setVisible(true);
            areaPersonale.setVisible(true);
        } else {
            /**********
             * UNLOGGED
             ************/

            circleView_unlogged.setVisibility(View.VISIBLE);
            circleView_logged.setVisibility(View.GONE);
            nome_account.setText("");
            nome_account.setVisibility(View.VISIBLE);
            email_account.setText("");
            email_account.setVisibility(View.GONE);

            /** menu **/
            esci.setVisible(false);
            areaPersonale.setVisible(false);
            nonLoggato.setVisible(true);
            registrati.setVisible(true);
        }
    }

    private void updateMenus() {
        Log.d("Update Menus: ", " update!!!");

        /** aggiorno action bar */

        MenuItem login = menu_action_bar.findItem(R.id.login_menu);
        MenuItem logout = menu_action_bar.findItem(R.id.logout_menu);

        boolean loggin = settings.getBoolean("logged", false);
        if (loggin) {
            login.setVisible(false);
            logout.setVisible(true);
        } else {
            login.setVisible(true);
            logout.setVisible(false);
        }
    }

    private void logout() {
        Log.d("logout: ", "mi sloggo!!!");
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("logged", false);
        editor.apply();

        /** aggiorno navigation menu */
        setupUserMenu();


        /** aggiorno action bar */
        MenuItem login = menu_action_bar.findItem(R.id.login_menu);
        MenuItem logout = menu_action_bar.findItem(R.id.logout_menu);

        boolean loggin = settings.getBoolean("logged", false);
        if (loggin) {
            login.setVisible(false);
            logout.setVisible(true);
        } else {
            login.setVisible(true);
            logout.setVisible(false);
        }

        // avviso logout
        Toast.makeText(getBaseContext(), R.string.logout_done, Toast.LENGTH_LONG).show();
    }

    private void setupViewPager(ViewPager viewPager, int item) {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFrag(new MyListsFragment(), "LISTE");
        pagerAdapter.addFrag(new SearchFragment(), "CERCA");
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(settings.contains("not_first_time") && !settings.getBoolean("not_first_time", false)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("not_first_time", true);
            editor.apply();
        }
        else{
            updateMenus();
        }
    }


    private void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("onTokenRefresh", "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String uniqueId) {
        //getting the email entered
        final String email = settings.getString("email","");
        //Creating a string request
        StringRequest req = new StringRequest(Request.Method.POST, Constants.REGISTER_TOKEN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //if the server returned the string success
                        if (response.trim().equalsIgnoreCase("success")) {
                            //Displaying a success toast
                            //Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                            //Opening shared preference
                            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, 0);

                            //Opening the shared preferences editor to save values
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Storing the unique id
                            editor.putString(Constants.TOKEN, uniqueId);

                            //Saving the boolean as true i.e. the device is registered
                            editor.putBoolean("logged", true);

                            //Applying the changes on sharedpreferences
                            editor.apply();

                        } else {
                            Log.d("sendRegistration","error ");
                            //Toast.makeText(MainActivity.this, "Choose a different email", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //adding parameters to post request as we need to send firebase id and email
                params.put("token", uniqueId);
                params.put("email", email);
                Log.d("onTokenRefresh-send","Params: "+params);
                return params;
            }
        };

        //Adding the request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener{
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }



        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d("Main","onPageScrolled");
        }

        @Override
        public void onPageSelected(int position) {
            Log.d("Main","onPageSelected");
            onTabChangeListener fragment = (onTabChangeListener) pagerAdapter.instantiateItem(viewPager, position);
            if (fragment != null) {
                fragment.fragmentBecameVisible();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }

    public interface onTabChangeListener {
        void fragmentBecameVisible();
    }


}