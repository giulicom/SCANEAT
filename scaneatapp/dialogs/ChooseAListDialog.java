package com.app.project.scaneatapp.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.project.scaneatapp.MyApplication;
import com.app.project.scaneatapp.MyListDetailsActivity;
import com.app.project.scaneatapp.MyNumberPicker;
import com.app.project.scaneatapp.adapters.MyListAdapter;
import com.app.project.scaneatapp.db_lists.MyList;
import com.app.project.scaneatapp.db_lists.MyList_DAO;
import com.app.project.scaneatapp.db_lists.MyListsDAO_impl;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.db_products_in_list.ProductInList;
import java.util.List;
import scaneat.R;


public class ChooseAListDialog extends DialogFragment {


    private List<MyList> lists;

    private TextView noLists;
    private ListView listView;
    private MyNumberPicker prodQuantity;
    private MyListAdapter myListAdapter;


    public static ChooseAListDialog newInstance(String name, String brand, int id, Long barcode, int idCurrentList) {
        ChooseAListDialog f = new ChooseAListDialog();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("brand", brand);
        args.putInt("id", id);
        args.putLong("barcode", barcode);
        args.putInt("idList", idCurrentList);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_choose_a_list, null);

        // Build the dialog and set up the button click handlers
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Bundle bundle = getArguments();
        if(bundle != null) {
            final String name = (String) bundle.get("name");
            final String brand = (String) bundle.get("brand");
            final long barcode = (Long) bundle.get("barcode");
            final int idProd = (Integer) bundle.get("id");
            final int idList = (Integer) bundle.get("idList");

            noLists = (TextView) view.findViewById(R.id.choose_a_list_empty);
            listView = (ListView) view.findViewById(R.id.choose_a_list_listview);
            prodQuantity = (MyNumberPicker) view.findViewById(R.id.quantity);

            // GET ALL LISTS (SO WE DON'T HAVE THE "FAVOURITES" FIELD)
            MyList_DAO listsDAO = new MyListsDAO_impl();
            listsDAO.open();

            //  idList = -1 solo quando arrivo dal search fragment della main activity (tab di dx)
            if(idList == -1) {
                lists = listsDAO.getAllLists();
            }
            else {
                lists = listsDAO.getAllListsExceptOne(idList);
            }

            try {
                myListAdapter = new MyListAdapter(getParentFragment().getContext(), lists);
            } catch (Exception e) {
                myListAdapter = new MyListAdapter(MyApplication.getAppContext(), lists);
            }

            listView.setAdapter(myListAdapter);

            if (myListAdapter.getItems().size() == 0) {
                noLists.setVisibility(View.VISIBLE);
            }


            //final MyNewProductDialogListener mListener = (MyNewProductDialogListener)getTargetFragment();
            builder.setView(view)
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            ChooseAListDialog.this.getDialog().cancel();
                        }
                    });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // "false" = vengo dal pulsante "cerca" in ListDetailsActivity
                    showPicker(false, position, name, brand, idProd, barcode, 0, getActivity());
                }
            });
        }

        return builder.create();

    }

    public void showPicker(final boolean fromListDetailsActivity, int position, final String name, final String brand, final int idProd,
                           final long barcode, final int idListFromListDetailsActivity, final Activity activity){
        final int idList;
        LayoutInflater inflaterPicker = activity.getLayoutInflater();
        if(!fromListDetailsActivity) { // NON vengo da cerca/scan
            idList = myListAdapter.getItem(position).getId();
        }
        else{ // arrivo da cerca/scan
            idList = idListFromListDetailsActivity;
        }
        final AlertDialog.Builder dialogPicker = new AlertDialog.Builder(activity);
        View viewPicker = inflaterPicker.inflate(R.layout.dialog_number_picker, null);
        prodQuantity = (MyNumberPicker) viewPicker.findViewById(R.id.choose_a_list_quantity);
        dialogPicker.setView(viewPicker)
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int quantity = prodQuantity.getValue();
                        addToList(name, brand, idProd, barcode, quantity, idList, activity);
                        if(fromListDetailsActivity){
                            MyListDetailsActivity.refresh();
                        }
                        dialog.dismiss();
                    }
                });

        AlertDialog ad = dialogPicker.create();
        ad.show();
    }


    private void addToList(String productName, String productBrand, int idProd, long barcode, int quantity, int idList, Activity activity)
    {
        boolean inserito = false;
        try {
            Product toInsert;
            MyProductInList_DAO prodListDao = new MyProductInList_impl();
            if(barcode != -1) { // the product comes from the extDB
                MyProduct_DAO prodDao = new MyProduct_impl();
                prodDao.open();
                toInsert = prodDao.getProduct(barcode);
                prodDao.close();
                prodListDao.open();
                ProductInList productInList = new ProductInList(idList, barcode, toInsert.getName(), toInsert.getBrand(),
                         quantity, toInsert.getNoGluten(), toInsert.getNoLactose(), toInsert.getVegan());
                List<ProductInList> allProductsInList = prodListDao.getAllProductsInList(idList);
                if(!alreadyInList(allProductsInList, barcode, productName, productBrand)){
                    prodListDao.insertExtProductFromInfoDialog(productInList);
                    prodListDao.close();
                    inserito = true;
                }
                else{
                    showAlreadyInListAlert(activity);
                }
            }
            else{ // the product is already in the productInListDb, in order to add it we need to check wether it was
                // already in the idList. If so, you just need to update the quantity, otherwise you need to insert a new product
                prodListDao.open();
                List<ProductInList> allProductsInList = prodListDao.getAllProductsInList(idList);
                //ProductInList productInList = new ProductInList(idList, barcode, productName, productBrand, )
                if(!alreadyInList(allProductsInList, barcode, productName, productBrand)) { // barcode = -1
                    prodListDao.insertListProductFromInfoDialog(idProd, idList, quantity);
                    prodListDao.close();
                    inserito = true;
                }
                else{
                    showAlreadyInListAlert(activity);
                }
            }
            if(inserito) {
                // update the number of elements of the list
                if(myListAdapter!=null) {
                    myListAdapter.refresh(lists);
                }

                String success = productName +" è stato inserito con successo";// + getResources().getString(R.string.product_successfully_inserted);
                Toast.makeText(MyApplication.getAppContext(), success, Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dark_Dialog);
            builder.setTitle(R.string.oops_error);
            builder.setMessage(R.string.something_went_wrong);
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

    private boolean alreadyInList(List<ProductInList> allProducts, long barcode, String name, String brand)
    {
        // se il barcode è != da -1, allora il prodotto viene dall'esterno -> va controllato che ce ne sia uno con lo stesso barcode
        // se il barcode è == a -1, allora il prodotto viene da una lista -> va controllato che ce ne sia uno con lo stesso nome e marca
        for(ProductInList p: allProducts){
            if((barcode != -1 && p.getBarcode() == barcode) || (p.getName().equals(name) && p.getBrand().equals(brand) && barcode==-1))
                return true;
        }
        return false;
    }

    private void showAlreadyInListAlert(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dark_Dialog);
        builder.setTitle(R.string.already_in_list_title);
        builder.setMessage(R.string.already_in_list_message);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog  = builder.create();
        alertDialog.show();
    }
}
