package com.app.project.scaneatapp.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.project.scaneatapp.*;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.db_products_in_list.ProductInList;
import scaneat.R;

public class MyProductInfoDialog extends DialogFragment {


    private String[] array;

    private TextView name;
    private TextView brand;
    private TextView barcode;
    private TextView ingredients;
    private ImageView noGluten;
    private ImageView noLactose;
    private ImageView vegan;

    private boolean logged;
    private SharedPreferences settings;

    public static MyProductInfoDialog newInstance(String[] array) {
        MyProductInfoDialog f = new MyProductInfoDialog();
        Bundle args = new Bundle();
        args.putStringArray("product_details", array);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_info_product, null);

        name = (TextView) view.findViewById(R.id.product_info_name);
        name.setSelected(true);
        brand = (TextView) view.findViewById(R.id.product_info_brand);
        brand.setSelected(true);
        ingredients = (TextView) view.findViewById(R.id.product_info_ingredients);
        ingredients.setMovementMethod(new ScrollingMovementMethod());
        TextView intolerances = (TextView) view.findViewById(R.id.intolerances);
        barcode = (TextView) view.findViewById(R.id.product_info_barcode);
        noGluten = (ImageView) view.findViewById(R.id.product_info_no_gluten);
        noLactose = (ImageView) view.findViewById(R.id.product_info_no_lactose);
        vegan = (ImageView) view.findViewById(R.id.product_info_vegan);


        final Bundle bundle = getArguments();
        if (bundle != null) {
            array = bundle.getStringArray("product_details");
            name.setText(array[1]);
            brand.setText(array[2]);
            if(array[3]==null) {
                ingredients.setVisibility(View.GONE);

            }
            else {
                ingredients.setText(getResources().getString(R.string.ingredients) + " " + array[3]);
            }
            if(array[4].equals("-1")) {
                barcode.setVisibility(View.GONE);
            }
            else{
                barcode.setText(getResources().getString(R.string.barcode) + " " + array[4]);
            }
            intolerances.setText(getResources().getString(R.string.intolerances) + " ");
            setupProductMenu(view, array[1], Integer.parseInt(array[0]), Long.parseLong(array[4].toString()));

            if (array[5] == "0")
                noGluten.setVisibility(View.GONE);
            if (array[6] == "0")
                noLactose.setVisibility(View.GONE);
            if (array[7] == "0")
                vegan.setVisibility(View.GONE);
            if ((array[5] == array[6]) && (array[6] == array[7]) && (array[7] == "0"))
                view.findViewById(R.id.product_info_no_intolerances).setVisibility(View.VISIBLE);
        }

            // Build the dialog and set up the button click handlers
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // set the positiveButtonlabel
        String positiveButtonString;
        final boolean addToCurrentList = Boolean.valueOf(array[9]);
        if(addToCurrentList){
            positiveButtonString = getResources().getString(R.string.add_to_chosen_list);
        }
        else{
            positiveButtonString = getResources().getString(R.string.add_to_list);
        }
            builder.setView(view)
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MyProductInfoDialog.this.getDialog().cancel();
                        }
                    })
                .setPositiveButton(positiveButtonString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(addToCurrentList){
                            ChooseAListDialog c = new ChooseAListDialog();
                            // true perchè vuol dire che vengo dai bottoni cerca/scan della ListDetailsActivity
                            // 0 non verrà usato perchè sarebbe la posizione della lista scelta nell'adapter (che qui salto)
                            c.showPicker
                                    (true, 0, name.getText().toString(), brand.getText().toString(), Integer.parseInt(array[0].toString()),
                                    Long.parseLong(array[4].toString()), Integer.parseInt(array[8]), getActivity());
                        }
                        else{
                            // array[8] è l'id della lista corrente
                            chooseAList(array[1], array[2], Integer.parseInt(array[0]), Long.parseLong(array[4].toString()),
                                        Integer.parseInt(array[8]));
                        }
                    }
                });
            return builder.create();

        }

    private void setupProductMenu(View view, final String name, final int idList, final long barcode)
    {
        // if idList != -1 the product is from a list, no menu required
        if(idList != -1){
            return;
        }
        settings = getActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        logged = settings.getBoolean("logged", false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.product_toolbar);
        toolbar.inflateMenu(R.menu.info_product_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d("onMenuItemClick: ", String.valueOf(item.getItemId()));
                switch(item.getItemId())
                {

                    case R.id.add_item_to_favorite:
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle(R.string.add_to_favourites)
                                .setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MyProductInList_DAO prodDao = new MyProductInList_impl();
                                        prodDao.open();
                                        ProductInList prod1 = new ProductInList(-1, Long.valueOf(barcode), String.valueOf(name), String.valueOf(brand), 0,
                                                Integer.valueOf(array[5]), Integer.valueOf(array[6]), Integer.valueOf(array[7]));
                                        if(!prodDao.checkExistence(prod1)) {
                                            // if it isn't favourite, set favourite = 1
                                            ProductInList prod2 = new ProductInList(-1, Long.valueOf(barcode),String.valueOf(name), brand.getText().toString(), 0,
                                                    Integer.valueOf(array[5]), Integer.valueOf(array[6]), Integer.valueOf(array[7]), 1);
                                            prodDao.insertExtProductFromInfoDialog(prod2);
                                            prodDao.setFavourite(prod2);
                                            Toast.makeText(getActivity(), name.toString() + " è stato aggiunto correttamente ai preferiti",
                                                Toast.LENGTH_SHORT).show();
                                        }
                                        else Toast.makeText(getActivity(),name.toString() + " è già presente nei tuoi preferiti!", Toast.LENGTH_SHORT).show();

                                        prodDao.close();


                                    }
                                })
                                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        dialog.show();
                        break;
                    }
                    case R.id.modify_item:
                    {
                        if(logged)
                        {
                            Intent intent = new Intent(getActivity(), ProposeAModify.class);
                            intent.putExtra("barcode", barcode);
                            startActivity(intent);
                            Log.d("modify", "logged");
                        }
                        else
                        {
                            Log.d("modify", "unlogged");
                            alertLoginToModify(barcode);

                        }
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                return true;
            }
        });
    }

    private void alertLoginToModify(final long barcode){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getResources().getString(R.string.not_login));
        alertDialog.setMessage(getResources().getString(R.string.reserved_area_login_required_message));

        alertDialog.setPositiveButton("Login",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.putExtra("origin", "modify");
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

    private void chooseAList(String name, String brand, int id, Long barcode, int idCurrentList)
    {
        ChooseAListDialog dialog = ChooseAListDialog.newInstance(name, brand, id, barcode, idCurrentList);
        android.support.v4.app.FragmentManager manager = this.getFragmentManager();
        Log.d("chooseAList: ", "creando il dialog");
        dialog.setTargetFragment(this, 1);
        dialog.show(manager, "SEARCH_DIALOG");
    }
}
