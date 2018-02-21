package com.app.project.scaneatapp;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.project.scaneatapp.adapters.ListDetailsAdapter;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.db_products_in_list.ProductInList;
import com.app.project.scaneatapp.dialogs.MyNewProductDialog;
import com.app.project.scaneatapp.dialogs.MyProductInfoDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.zxing.integration.android.IntentIntegrator;
import scaneat.R;

public class MyListDetailsActivity extends AppCompatActivity implements MyNewProductDialog.MyNewProductDialogListener{

    FloatingActionMenu fabMenu;
    FloatingActionButton fab_write, fab_search, fab_scan;
    ActionMode mActionMode;
    private RelativeLayout listDetails;
    private FrameLayout searchFromList;
    public EditText prodName;
    public EditText prodBrand;
    public MyNumberPicker prodQuantity;
    public CheckBox prodNoGluten;
    public CheckBox prodNoLactose;
    public CheckBox prodVegan;
    private static ListDetailsAdapter mAdapter;
    private ListView lv;
    android.support.v7.widget.Toolbar toolbar;
    private EditText search;
    int checkedCount;
    private MyProductInList_DAO prodListDao = new MyProductInList_impl();
    private MyProduct_DAO prodDao = new MyProduct_impl();

    private int id_list;
    final Fragment searchFragment = new SearchFragment();

    private SharedPreferences settings;
    private boolean logged;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list_details);

        settings = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        id_list = extras.getInt("id_list");
        String list_name = extras.getString("list_name");
        extras.putBoolean("comingFromList", true);

        // SET ONLY THE LISTLAYOUT
        listDetails = (RelativeLayout) findViewById(R.id.list_details);
        searchFromList = (FrameLayout) findViewById(R.id.search_from_list);
        listDetails.setVisibility(View.VISIBLE);
        searchFromList.setVisibility(View.GONE);


        // SET TOOLBAR PARAMETERS
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(list_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // FILL THE ADAPTER
        mAdapter = new ListDetailsAdapter(MyApplication.getAppContext(), id_list);
        lv = (ListView) findViewById(R.id.myListViewForProducts);
        lv.setAdapter(mAdapter);

        // FLOATING ACTION BUTTON
        fabMenu = (FloatingActionMenu) findViewById(R.id.floating_action_button_menu);
        fab_write = (FloatingActionButton) findViewById(R.id.write_button);
        fab_search = (FloatingActionButton) findViewById(R.id.search_button);
        fab_scan = (FloatingActionButton) findViewById(R.id.scan_button);

        // INSERT PRODUCT MANUALLY
        fab_write.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DialogFragment dialog = new MyNewProductDialog();
                dialog.show(getFragmentManager(), "MyNewProductFragment");

                disableActionMode();

            }

        });

        fab_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                listDetails.setVisibility(View.GONE);
                searchFromList.setVisibility(View.VISIBLE);

                disableActionMode();

            }
        });

        fab_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator integrator = new IntentIntegrator(MyListDetailsActivity.this);
                integrator.initiateScan();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                disableActionMode();

            }
        });

        // SET THE SEARCHFRAGMENT
        searchFragment.setArguments(extras);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.search_from_list, searchFragment, "SEARCH_FRAGMENT_TRANSACTION").commit();

        // CONTEXTUAL ACTION BAR
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                checkedCount = lv.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                mAdapter.toggleSelection(position);
                // invalidate: so the next method called will be onPrepareActionMode (to manage the menu's icons)
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mActionMode = mode;

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (checkedCount == 1) {
                    MenuItem item = menu.findItem(R.id.modify);
                    item.setVisible(true);

                } else {
                    MenuItem item = menu.findItem(R.id.modify);
                    item.setVisible(false);

                }

                return true;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                lv.setSelected(true);

                switch (item.getItemId()) {
                    case R.id.delete:
                        // ASK CONFIRM FOR DELETING PRODUCT(S)
                        // Calls getSelectedIds method from ListViewAdapter Class
                        final SparseBooleanArray selectedToDelete = mAdapter.getSelectedIds();
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyListDetailsActivity.this);
                        alertDialog.setTitle(getResources().getQuantityString(R.plurals.delete_products,checkedCount,checkedCount));
                        alertDialog.setMessage(R.string.delete_product_message);
                        alertDialog.setPositiveButton(R.string.confirm_button,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Captures all selected ids with a loop
                                        for (int i = (selectedToDelete.size() - 1); i >= 0; i--) {
                                            if (selectedToDelete.valueAt(i)) {
                                                ProductInList selectedItem = mAdapter
                                                        .getItem(selectedToDelete.keyAt(i));
                                                // Remove selected items following the ids
                                                mAdapter.remove(selectedItem);
                                            }
                                        }

                                        Toast.makeText(MyListDetailsActivity.this, R.string.successfully_deleted,
                                                Toast.LENGTH_SHORT).show();
                                        // Close CAB
                                        mode.finish();
                                    }
                                })
                                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        alertDialog.create().show();
                        return true;

                    case R.id.modify:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selectedToModify = mAdapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selectedToModify.size() - 1); i >= 0; i--) {
                            if (selectedToModify.valueAt(i)) {
                                ProductInList selecteditem = mAdapter
                                        .getItem(selectedToModify.keyAt(i));
                                // Pick the details of selected items
                                String id_prod = "" + selecteditem.getIdProd();
                                String name = selecteditem.getName();
                                String brand = selecteditem.getBrand();
                                String quantity = "" + selecteditem.getQuantity();
                                String noGluten = "" + selecteditem.getNoGluten();
                                String noLactose = "" + selecteditem.getNoLactose();
                                String vegan = "" + selecteditem.getVegan();

                                String[] array = new String[]{id_prod, name, brand, quantity, noGluten, noLactose, vegan};

                                MyNewProductDialog dialog = MyNewProductDialog.newInstance(array);
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                dialog.show(transaction, "MyNewProductDialog-editMode");
                                // mAdapter.remove(selecteditem);
                            }
                        }
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.removeSelection();
            }

        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] array;
                prodListDao.open();
                ProductInList prod = mAdapter.getItem(position);
                prodListDao.close();
                boolean addToCurrentList = false;
                if(prod.getBarcode()==-1) {
                    array = new String[]{Integer.toString(prod.getIdProd()), prod.getName(), prod.getBrand(), null,
                        Long.toString(prod.getBarcode()), Integer.toString(prod.getNoGluten()), Integer.toString(prod.getNoLactose()),
                        Integer.toString(prod.getVegan()), String.valueOf(id_list), Boolean.toString(addToCurrentList)};
                    // addToCurrentList = false perchè derivo dai bottoni cerca/scan => aggiungo il prod alla lista corrente
                }
                else {
                    MyProduct_DAO product_dao = new MyProduct_impl();
                    product_dao.open();
                    Product fromExtDb = product_dao.getProduct(prod.getBarcode());
                    product_dao.close();
                    array = new String[]{Integer.toString(prod.getIdProd()), fromExtDb.getName(), fromExtDb.getBrand(),
                            fromExtDb.getIngredients(), Long.toString(fromExtDb.getBarcode()),
                            Integer.toString(fromExtDb.getNoGluten()), Integer.toString(fromExtDb.getNoLactose()),
                            Integer.toString(fromExtDb.getVegan()), String.valueOf(id_list), Boolean.toString(addToCurrentList)};
                }
                MyProductInfoDialog dialog = MyProductInfoDialog.newInstance(array);
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                dialog.show(manager, "SEARCH_DIALOG");
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("MyListDetails: ", "onResume");
        mAdapter.refresh();
    }

    // BARCODE SCANNER RESULT
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //Log.d("parametri in uscita", "request code: " + requestCode + "\nresultCode: " + resultCode);

        if (resultCode == -1) {
            String contents = intent.getStringExtra("SCAN_RESULT");
            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

            try {
                Long barcode = Long.parseLong(contents);
                if (format.equals(getResources().getString(R.string.EAN_13_format))||
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MyListDetailsActivity.this, R.style.AppTheme_Dark_Dialog);
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


    private void showResult(Product product, final Long barcode){
        if(product == null) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MyListDetailsActivity.this, R.style.AppTheme_Dark_Dialog);
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
                    logged = settings.getBoolean("logged", false);
                    if(!logged) {
                        alertLoginToPropose(barcode);
                    }
                    else{
                        Intent intent = new Intent(MyListDetailsActivity.this, ProposeAProduct.class);
                        intent.putExtra("barcode", barcode);
                        startActivity(intent);
                    }
                }
            });
            builder1.show();
        }
        else{
            boolean addToCurrentList = true;
            // "-1" significa che non mi compare il menù
            String[] array = {"-1", product.getName(), product.getBrand(), product.getIngredients(),
                    Long.toString(product.getBarcode()), Integer.toString(product.getNoGluten()),
                    Integer.toString(product.getNoLactose()), Integer.toString(product.getVegan()), String.valueOf(id_list),
                    Boolean.toString(addToCurrentList)};
            MyProductInfoDialog dialog = MyProductInfoDialog.newInstance(array);
            android.support.v4.app.FragmentTransaction ft = MyListDetailsActivity.this.getSupportFragmentManager().beginTransaction();
            dialog.show(ft, "SEARCH_DIALOG");


            Log.d("fromSearchFragment", "showResult");

        }
    }

    // IF YOU ARE NOT LOGGED
    private void alertLoginToPropose(final long barcode){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyListDetailsActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.not_login));
        alertDialog.setMessage(getResources().getString(R.string.reserved_area_login_required_message));

        alertDialog.setPositiveButton("Login",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MyListDetailsActivity.this, LoginActivity.class);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // INSERT PRODUCT DIALOG'S RESULT
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        prodName = (EditText) dialog.getDialog().findViewById(R.id.product_name);
        String pr_name = prodName.getText().toString();
        prodBrand = (EditText) dialog.getDialog().findViewById(R.id.product_brand);
        String pr_brand = prodBrand.getText().toString();
        prodQuantity = (MyNumberPicker) dialog.getDialog().findViewById(R.id.quantity);
        prodNoGluten = (CheckBox) dialog.getDialog().findViewById(R.id.checkbox_no_gluten);
        prodNoLactose = (CheckBox) dialog.getDialog().findViewById(R.id.checkbox_no_lactose);
        prodVegan = (CheckBox) dialog.getDialog().findViewById(R.id.checkbox_vegan);

        int pr_quantity = prodQuantity.getValue();
        boolean temp_no_gluten = prodNoGluten.isChecked();
        boolean temp_no_lactose = prodNoLactose.isChecked();
        boolean temp_vegan = prodVegan.isChecked();
        int pr_no_gluten = 0;
        int pr_no_lactose = 0;
        int pr_vegan = 0;
        if(temp_no_gluten)
            pr_no_gluten = 1;
        if(temp_no_lactose)
            pr_no_lactose = 1;
        if(temp_vegan)
            pr_vegan = 1;

        // CREATE AND INSERT PRODUCT
        prodListDao.open();

        ProductInList productInList = new ProductInList(id_list, pr_name,pr_brand,pr_quantity, pr_no_gluten, pr_no_lactose, pr_vegan);
        prodListDao.insertProduct(productInList);

        prodListDao.close();

        mAdapter.refresh();
    }

    // MODIFY PRODUCT DIALOG RESULT
    @Override
    public void onDialogModifyPositiveClick(DialogFragment dialog, int id_prod, String TAG){

        prodName = (EditText) dialog.getDialog().findViewById(R.id.product_name);
        String pr_name = prodName.getText().toString();
        prodBrand = (EditText) dialog.getDialog().findViewById(R.id.product_brand);
        String pr_brand = prodBrand.getText().toString();
        prodQuantity = (MyNumberPicker) dialog.getDialog().findViewById(R.id.quantity);
        prodNoGluten = (CheckBox) dialog.getDialog().findViewById(R.id.checkbox_no_gluten);
        prodNoLactose = (CheckBox) dialog.getDialog().findViewById(R.id.checkbox_no_lactose);
        prodVegan = (CheckBox) dialog.getDialog().findViewById(R.id.checkbox_vegan);

        int pr_quantity = prodQuantity.getValue();
        boolean temp_no_gluten = prodNoGluten.isChecked();
        boolean temp_no_lactose = prodNoLactose.isChecked();
        boolean temp_vegan = prodVegan.isChecked();
        int pr_no_gluten = 0;
        int pr_no_lactose = 0;
        int pr_vegan = 0;
        if(temp_no_gluten)
            pr_no_gluten = 1;
        if(temp_no_lactose)
            pr_no_lactose = 1;
        if(temp_vegan)
            pr_vegan = 1;

        //
        MyProductInList_DAO prodDao = new MyProductInList_impl();
        prodDao.open();

        ProductInList productInList = new ProductInList(id_prod, id_list, pr_name, pr_brand, pr_quantity, pr_no_gluten, pr_no_lactose, pr_vegan);
        prodDao.editProduct(productInList);
        prodDao.close();

        mAdapter.refresh();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }


    @Override
    public void onBackPressed() {
        if(searchFromList.getVisibility() == View.VISIBLE)
        {
            searchFromList.setVisibility(View.GONE);
            listDetails.setVisibility(View.VISIBLE);
            search = (EditText) findViewById(R.id.search_text);
            search.setText("");

        }
        else{
            super.onBackPressed();
        }
    }


    // REFRESH THE ADAPTER
    static public void refresh() {
        Log.d("MyListDatailsActivity: ","refresh");
        mAdapter.refresh();
    }

    // DISABLE ACTION MODE
    private void disableActionMode(){
        if(mActionMode!=null) {
            mActionMode.finish();
        }
    }
}

