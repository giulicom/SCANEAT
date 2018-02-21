package com.app.project.scaneatapp.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.app.project.scaneatapp.MyNumberPicker;
import scaneat.R;

public class MyNewProductDialog extends DialogFragment {

    private MyNumberPicker prodQuantity;
    String[] array;
    int id_prod;
    TextView tv;

    public static MyNewProductDialog newInstance(String[] array){
        MyNewProductDialog fragment = new MyNewProductDialog();
        Bundle args = new Bundle();
        args.putStringArray("product_details", array);
        fragment.setArguments(args);
        return fragment;
    }

    /* The activity that creates an instance of this dialog fragment must
      * implement this interface in order to receive event callbacks.
      * Each method passes the DialogFragment in case the host needs to query it. */

    public interface MyNewProductDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogModifyPositiveClick(DialogFragment dialog, int id_list, String TAG);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    MyNewProductDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the MyNewProductDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (MyNewProductDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement MyNewProductDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_insert_product, null);

            tv = (TextView) view.findViewById(R.id.dialog_modifyProduct_title);

            prodQuantity = (MyNumberPicker) view.findViewById(R.id.quantity);
            // set number picker parameters
            prodQuantity.setWrapSelectorWheel(false);
            prodQuantity.setFocusable(true);
            prodQuantity.setFocusableInTouchMode(true);

            // Build the dialog and set up the button click handlers
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


            final Bundle bundle = getArguments();
            if (bundle != null) {
                // change existing title
                tv.setText(R.string.modify_product_dialog);
                //
                array = bundle.getStringArray("product_details");
                id_prod = Integer.parseInt(array[0]);
                String name = array[1];
                String brand = array[2];
                int quantity = Integer.parseInt(array[3]);
                int noGluten = Integer.parseInt(array[4]);
                int noLactose = Integer.parseInt(array[5]);
                int vegan = Integer.parseInt(array[6]);

                EditText prodName = (EditText) view.findViewById(R.id.product_name);
                prodName.setText(name);
                EditText prodBrand = (EditText) view.findViewById(R.id.product_brand);
                prodBrand.setText(brand);
                prodQuantity.setValue(quantity);

                CheckBox noGlutenCheckbox = (CheckBox) view.findViewById(R.id.checkbox_no_gluten);
                if(noGluten==1) {
                    noGlutenCheckbox.setChecked(true);
                }

                CheckBox noLactoseCheckbox = (CheckBox) view.findViewById(R.id.checkbox_no_lactose);
                if(noLactose==1) {
                    noLactoseCheckbox.setChecked(true);
                }

                CheckBox veganCheckbox = (CheckBox) view.findViewById(R.id.checkbox_vegan);
                if(vegan==1) {
                    veganCheckbox.setChecked(true);
                }
            }

            else {
                prodQuantity.setValue(1);
            }

            builder.setView(view)
                    .setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the positive button event back to the host activity
                            if(bundle == null) {
                                mListener.onDialogPositiveClick(MyNewProductDialog.this);
                            }
                            else mListener.onDialogModifyPositiveClick(MyNewProductDialog.this, id_prod, "DIALOG-MODIFY");
                        }
                    })
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            mListener.onDialogNegativeClick(MyNewProductDialog.this);
                        }
                    });
            return builder.create();
    }


}



