package com.app.project.scaneatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.app.project.scaneatapp.adapters.MySearchProductsAdapter;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.dialogs.MyProductInfoDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import scaneat.R;


public class SearchFragment extends Fragment implements MySearchProductsAdapter.RecyclerClickListener {

    private int idList; // if this fragment belongs to the MainActivity, it will be set to -1,
                        // otherwise it means that it belongs to the MyListDetailsActivity
    private boolean addToCurrentList;

    private RecyclerView mRecyclerView;

    private EditText queryText;

    private MyProduct_DAO prodDao = new MyProduct_impl();
    FloatingActionButton scan;

    private SharedPreferences settings;
    private boolean loggin;

    Fragment fragment = this;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_search, container, false);

        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey("id_list"))
        {
            idList = bundle.getInt("id_list");
            addToCurrentList = true;
        }
        else{
            idList = -1;
            addToCurrentList = false;
        }

        // Add the EditText for performing your product scan
        queryText = (EditText) v.findViewById(R.id.search_text);
        queryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                new PostTask(input, v).execute();
            }
        });

        // ADD LIST WITH FLOATING-ACTION-BUTTON
        scan = (FloatingActionButton) v.findViewById(R.id.scan_button);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator.forSupportFragment(fragment).initiateScan();

            }
        });

        
        mRecyclerView = (RecyclerView) v.findViewById(R.id.search_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));

        return v;
    }

    // BARCODE SCANNER RESULT
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //Log.d("parametri in uscita", "request code: " + requestCode + "\nresultCode: " + resultCode );

        if(resultCode == -1)
        {
            String contents = intent.getStringExtra("SCAN_RESULT");
            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

            try {

                Long barcode = Long.parseLong(contents);
                if (format.equals(getResources().getString(R.string.EAN_13_format)) ||
                        format.equals(getResources().getString(R.string.EAN_8_format))) {
                    prodDao.open();
                    Product p = prodDao.getProduct(Long.parseLong(contents));
                    prodDao.close();
                    showResult(p, barcode);
                } else {
                    showResult(null, barcode);
                }
            }
            catch(Exception e){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dark_Dialog);
                builder.setTitle(R.string.oops_error);
                builder.setMessage(R.string.format_error);
                builder.setCancelable(false);
                builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog  = builder.create();
                alertDialog.show();
            }
        }
    }

    private void showResult(Product product,final Long barcode){
        if(product == null) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dark_Dialog);
            builder1.setTitle(R.string.no_products_matching);
            builder1.setMessage(R.string.propose_to_insert_the_product);
            builder1.setCancelable(false);
            builder1.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder1.setPositiveButton(R.string.propose_product_button, new DialogInterface.OnClickListener() {
                // if you are logged, propose a new product
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    loggin = settings.getBoolean("logged", false);
                    if(!loggin) {
                        alertLoginToPropose(barcode);
                    }
                    else{
                        Intent intent = new Intent(getActivity(), ProposeAProduct.class);
                        intent.putExtra("barcode", barcode);
                        startActivity(intent);
                    }
                }
            });
            builder1.show();
        }
        else{

            String[] array = {"-1", product.getName(), product.getBrand(), product.getIngredients(),
                    Long.toString(product.getBarcode()), Integer.toString(product.getNoGluten()),
                    Integer.toString(product.getNoLactose()), Integer.toString(product.getVegan()), Integer.toString(idList),
                    Boolean.toString(addToCurrentList)};
            MyProductInfoDialog dialog = MyProductInfoDialog.newInstance(array);
            android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            dialog.show(ft, "SEARCH_DIALOG");

        }
    }

    private void alertLoginToPropose(final long barcode){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getResources().getString(R.string.not_login));
        alertDialog.setMessage(getResources().getString(R.string.reserved_area_login_required_message));

        alertDialog.setPositiveButton("Login",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.putExtra("origin", "scan");
                        intent.putExtra("barcode", barcode);
                        startActivity(intent);
                    }
                });

        alertDialog.setNegativeButton("Annulla",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onItemClicked(Product prod) {
        String[] array = {"-1", prod.getName(), prod.getBrand(), prod.getIngredients(), Long.toString(prod.getBarcode()),
                Integer.toString(prod.getNoGluten()), Integer.toString(prod.getNoLactose()),
                Integer.toString(prod.getVegan()), Integer.toString(idList), Boolean.toString(addToCurrentList)};
        MyProductInfoDialog dialog = MyProductInfoDialog.newInstance(array);
        android.support.v4.app.FragmentManager manager = this.getFragmentManager();
        Log.d("searchfragment", "nell'onclick");
        dialog.setTargetFragment(this, 1);
        dialog.show(manager, "SEARCH_DIALOG");
    }


    class PostTask extends AsyncTask<String, String, String> {


        MySearchProductsAdapter searchAdapter;
        private View view;
        private String search;

        public PostTask(String search, View view) {
            this.search = search;
            this.view = view;
        }

        @Override
        protected String doInBackground(String... params) {

            searchAdapter = new MySearchProductsAdapter(search, getActivity(), SearchFragment.this);
            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            TextView tv = (TextView) view.findViewById(R.id.no_match_found);
            RecyclerView rv = (RecyclerView) view.findViewById(R.id.search_view);
            SimpleDividerItemDecoration divider = new SimpleDividerItemDecoration(MyApplication.getAppContext());
            rv.addItemDecoration(divider);
            rv.setAdapter(searchAdapter);

            if (searchAdapter.getProducts().size() == 0 || search.length() == 0) {
                tv.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                return;
            }
            rv.setVisibility(View.VISIBLE);
            tv.setVisibility(View.INVISIBLE);
        }
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