package com.app.project.scaneatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.app.project.scaneatapp.adapters.MyListAdapter;
import com.app.project.scaneatapp.db_lists.*;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.dialogs.MyCreateListDialog;

import scaneat.R;


public class MyListsFragment extends Fragment {

    private MyList_DAO myListDao = new MyListsDAO_impl();
    private static final int REQUEST_CODE_1 = 1;
    private static final int REQUEST_CODE_2 = 2;
    public final static int RESULT_OK = 101;
    private static final String TAG_DIALOG = "dialog";
    ListView lv;
    private FloatingActionButton add;
    private MyListAdapter myListAdapter;
    TextView tv;
    private int checkedCount;

    @Nullable
    private ActionMode mActionMode = null;

    public MyListsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View V = inflater.inflate(R.layout.fragment_lists, container, false);

        // CREATE ADAPTER
        myListAdapter = new MyListAdapter(MyApplication.getAppContext());

        lv = (ListView) V.findViewById(R.id.myListView);
        lv.setAdapter(myListAdapter);

        // if there are no lists (except "favourites")
        tv = (TextView) V.findViewById(R.id.empty_list);
        setBackground(tv);

        // OPEN LIST DETAILS
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Vibrator v = (Vibrator) MyApplication.getAppContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(50);
                // field FAVOURITES
                if(position == 0) {
                    Intent intent = new Intent(getActivity(), FavouritesActivity.class);
                    startActivity(intent);
                }
                // LISTS
                else {
                    myListDao.open();
                    MyList list = myListAdapter.getItem(position);
                    Intent intent = new Intent(getActivity(), MyListDetailsActivity.class);
                    intent.putExtra("id_list", list.getId());
                    intent.putExtra("list_name", list.getName());
                    startActivity(intent);
                    myListDao.close();
                }
            }
        });


        // ADD LIST WITH FLOATING-ACTION-BUTTON
        add = (FloatingActionButton) V.findViewById(R.id.add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.add_button:
                        createListDialog();
                        break;
                }
            }

        });

        // CONTEXTUAL ACTION BAR
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                Log.d("selected", ""+position);
                if(position != 0) {
                    checkedCount = lv.getCheckedItemCount();
                    // Set the CAB title according to total checked items
                    mode.setTitle(checkedCount + " Selected");
                    // Calls toggleSelection method from ListViewAdapter Class
                    myListAdapter.toggleSelection(position);
                    // invalidate: so the next method called will be onPrepareActionMode (to manage the menu's icons)
                    mode.invalidate();
                }
                else
                    mode.finish();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_menu_main, menu);

                mActionMode = mode;
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
                        final SparseBooleanArray selectedToDelete = myListAdapter.getSelectedIds();
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setTitle(getResources().getQuantityString(R.plurals.delete_lists,checkedCount,checkedCount));
                        alertDialog.setMessage(R.string.delete_product_message);
                        alertDialog.setPositiveButton(R.string.confirm_button,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Captures all selected ids with a loop
                                        for (int i = (selectedToDelete.size() - 1); i >= 0; i--) {
                                            if (selectedToDelete.valueAt(i)) {
                                                MyList selectedItem = myListAdapter.getItem(selectedToDelete.keyAt(i));
                                                // Remove selected items following the ids
                                                myListAdapter.remove(selectedItem);
                                                // ADAPTER'S CHECK
                                                setBackground(tv);
                                                // MESSAGE
                                                Toast.makeText(getActivity(),  R.string.successfully_deleted,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        // Close CAB
                                        mode.finish();
                                    }
                                })
                                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        alertDialog.create().show();


                        return true;

                    case R.id.modify:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selectedToModify = myListAdapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selectedToModify.size() - 1); i >= 0; i--) {
                            if (selectedToModify.valueAt(i)) {
                                MyList selectedItem = myListAdapter.getItem(selectedToModify.keyAt(i));
                                // Pick the details of selected items
                                String name = selectedItem.getName();
                                createListDialog(selectedItem.getId(), name);
                            }
                        }
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                myListAdapter.removeSelection();
                mActionMode = null;
            }
        });

        return V;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        super.onResume();
        myListAdapter.refresh();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(this.isVisible()){
            if(isVisibleToUser){
                myListAdapter.refresh();

            }
        }
        if (mActionMode != null && !isVisibleToUser) {
            mActionMode.finish();
        }
    }

    /**********************************************/
    /**  CASE: CREATE-LIST **/
    protected void createListDialog() {
        MyCreateListDialog dialog = new MyCreateListDialog();

        android.support.v4.app.FragmentManager fragmentManager = this.getFragmentManager();
        dialog.setTargetFragment(this, REQUEST_CODE_1);
        dialog.show(fragmentManager, TAG_DIALOG);

    }

    /** CASE: MODIFY-LIST  **/
    protected  void createListDialog(int id, String listName) {
        MyCreateListDialog dialog = MyCreateListDialog.newInstance(id, listName);

        android.support.v4.app.FragmentManager fragmentManager = this.getFragmentManager();
        dialog.setTargetFragment(this, REQUEST_CODE_2);
        dialog.show(fragmentManager, TAG_DIALOG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        myListDao.open();
        switch (requestCode) {
            case REQUEST_CODE_1:

                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        String name = extras.getString("nameList");
                        MyList list = new MyList(name);
                        myListDao.insertList(list);

                        myListAdapter.refresh();
                        setBackground(tv);
                        // TOAST
                        Toast.makeText(getActivity(), R.string.list_created,
                                Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case REQUEST_CODE_2:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        int id = extras.getInt("idList");
                        String name = extras.getString("nameList");
                        MyList list = new MyList(id,name);

                        myListDao.editList(list);

                        myListAdapter.refresh();

                        // TOAST
                        Toast.makeText(getActivity(), R.string.successfully_modified,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

        myListDao.close();
    }

    public void goToListDetails() {
        Intent intent = new Intent(getActivity(), MyListDetailsActivity.class);
        startActivity(intent);
    }

    public void setBackground(TextView tv) {
        if (myListAdapter.getCount() < 2) { // 2 because the first element (favourites) is always present!
            tv.setVisibility(View.VISIBLE);
        } else tv.setVisibility(View.INVISIBLE);

        myListAdapter.refresh();
    }


}

