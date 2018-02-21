package com.app.project.scaneatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.app.project.scaneatapp.adapters.ListDetailsAdapter;
import com.app.project.scaneatapp.adapters.MySearchProductsAdapter;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.dialogs.MyProductInfoDialog;
import com.app.project.scaneatapp.notifications.MyFirebaseMessagingService;


import java.util.List;

import scaneat.R;


public class PersonalAreaActivity extends AppCompatActivity implements MySearchProductsAdapter.RecyclerClickListener{

    private TextView userName;
    private TextView mail;
    private TextView numberOfProposals;
    private SharedPreferences settings;

    private RecyclerView mRecyclerView;
    private MySearchProductsAdapter searchAdapter;
    private MySearchProductsAdapter.RecyclerClickListener mListener;
    android.support.v7.widget.Toolbar toolbar;

    private MyProduct_DAO dao;
    private List<Product> items;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_area);

        settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        if(!TextUtils.isEmpty(getIntent().getStringExtra(MyFirebaseMessagingService.FROM_NOTIFICATION))){
            MyFirebaseMessagingService.clearMessages();
            MyFirebaseMessagingService.resetNotificationID();
        }

        userName = (TextView) findViewById(R.id.personal_area_username);
        mail = (TextView) findViewById(R.id.personal_area_mail);
        numberOfProposals = (TextView) findViewById(R.id.personal_area_proposals);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.personal_area_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String userMail = settings.getString("email","");

        // FILL THE ADAPTER
        mRecyclerView = (RecyclerView) findViewById(R.id.personal_area_products);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
        searchAdapter = new MySearchProductsAdapter(userMail, MyApplication.getAppContext(), PersonalAreaActivity.this, true);

        int proposalCount = searchAdapter.getItemCount();

        userName.setText(getResources().getString(R.string.username) + " " + settings.getString("name",""));
        mail.setText(getResources().getString(R.string.mail) + " " + userMail);
        numberOfProposals.setText(getResources().getString(R.string.number_of_proposals) + " " + proposalCount);

        // FILL THE ADAPTER
        SimpleDividerItemDecoration divider = new SimpleDividerItemDecoration(MyApplication.getAppContext());
        mRecyclerView.addItemDecoration(divider);
        mRecyclerView.setAdapter(searchAdapter);

    }

    @Override
    public void onItemClicked(Product prod) {
        boolean addToCurrentList = false;
        int idList = -1; // means it doesn't come from a listdetailsactivity
        String[] array = {"-1", prod.getName(), prod.getBrand(), prod.getIngredients(), Long.toString(prod.getBarcode()),
                Integer.toString(prod.getNoGluten()), Integer.toString(prod.getNoLactose()),
                Integer.toString(prod.getVegan()), Integer.toString(idList), Boolean.toString(addToCurrentList)};
        // -1 is because the product isn't from the list db but from the external products db
        MyProductInfoDialog dialog = MyProductInfoDialog.newInstance(array);
        android.support.v4.app.FragmentManager manager = this.getSupportFragmentManager();

        dialog.show(manager, "SEARCH_DIALOG");

    }


    class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
