package com.app.project.scaneatapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import scaneat.R;

public class MyCreateListDialog extends DialogFragment {

    private int current_id;
    private String past_list_name;
    private EditText listName;
    int REQUEST_CODE;

    public static MyCreateListDialog newInstance(int id, String listName){
        MyCreateListDialog fragment = new MyCreateListDialog();
        Bundle args = new Bundle();
        args.putInt("IdList", id);
        args.putString("ListName", listName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_create_list, null);

            listName = (EditText) view.findViewById(R.id.list_name);

            // CASE: MODIFY
            final Bundle bundle = getArguments();

            // CASE: MODIFY
            if (bundle != null) {
                current_id = bundle.getInt("IdList");
                past_list_name = bundle.getString("ListName");
                listName.setText(past_list_name);
                REQUEST_CODE = 2;
            }
            else
                REQUEST_CODE = 1;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // CHOOSE TITLE OF DIALOG
            if(bundle == null) {
                // CASE: CREATE-LIST
                view.findViewById(R.id.new_list).setVisibility(View.VISIBLE);
            }
            else {
                // CASE: MODIFY-LIST-NAME
                view.findViewById(R.id.modify_list).setVisibility(View.VISIBLE);
            }

            builder.setView(view)
                    .setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            String list_name = listName.getText().toString();
                            Log.d("MyCreateListDialog: ", "List name: " + list_name);

                            if (bundle == null) {
                                sendResult(list_name, REQUEST_CODE);
                            }
                            else
                                sendResult(current_id,list_name,REQUEST_CODE);
                        }
                    })
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MyCreateListDialog.this.getDialog().cancel();
                        }
                    });
            return builder.create();

        }
        catch (Exception ex){
            Log.e("MyCreateDialog", "Problemi con la creazione del dialogo", ex);
            return null;
        }
    }

    private void sendResult(String name, int REQUEST_CODE) {
        Intent intent = getActivity().getIntent();
        intent.putExtra("nameList", name);

        getTargetFragment().onActivityResult(
                REQUEST_CODE, 101, intent);
    }

    private void sendResult(int id, String name, int REQUEST_CODE) {
        Intent intent = getActivity().getIntent();
        intent.putExtra("idList",id);
        intent.putExtra("nameList", name);

        getTargetFragment().onActivityResult(
                REQUEST_CODE, 101, intent);
    }
}

