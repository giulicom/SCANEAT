package com.app.project.scaneatapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.app.project.scaneatapp.MyApplication;
import com.app.project.scaneatapp.db_lists.MyList;
import com.app.project.scaneatapp.db_lists.MyList_DAO;
import com.app.project.scaneatapp.db_lists.MyListsDAO_impl;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;

import java.util.ArrayList;
import java.util.List;

import scaneat.R;


public class MyListAdapter extends BaseAdapter {

    List<MyList> lists = new ArrayList<>();
    LayoutInflater inflater;
    private SparseBooleanArray mSelectedItems;

    private MyList_DAO listDao;
    private MyProductInList_DAO prodDao = new MyProductInList_impl();
    private List<MyList> items;

    private class ViewHolder {
        TextView name;
        TextView numOfElements;
    }

    public MyListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mSelectedItems = new SparseBooleanArray();
        populate();
    }

    // 2nd constructor for chooseAListDialog
    public MyListAdapter(Context context, List<MyList> lists) {
        inflater = LayoutInflater.from(context);
        populate(lists);
    }

    public void populate(List<MyList> lists) {
        items = lists;

    }

    public void refresh(List<MyList> lists){
        populate(lists);
        notifyDataSetChanged();
    }
    public void populate() {
        listDao = new MyListsDAO_impl();
        listDao.open();
        lists = listDao.getAllLists();

        items = lists;
        items.add(0, getFavouritesField());
        notifyDataSetChanged();
        Log.d("MyListADAPTER", items.toString());

        listDao.close();
    }

    public MyList getFavouritesField() {
        String fav = MyApplication.getAppContext().getResources().getString(R.string.favourites);
        MyList list = new MyList(fav);
        return list;
    }

    public List<MyList> getItems() { return items; }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public MyList getItem(int location) {
        return items.get(location);
    }

    @Override
    public long getItemId(int location) {
        final MyList list = getItem(location);
        return list.getId();
    }

    public void refresh() {
        populate();
        notifyDataSetChanged();
    }

    public void remove(MyList list){
        // DELETE LIST
        listDao.open();
        int idList = list.getId();
        listDao.deleteList(list);
        listDao.close();
        items.remove(list);
        // DELETE ALL PRODUCTS IN LIST (not favourites)
        prodDao.open();
        prodDao.deleteProductsOfAListNotFavourites(idList);
        prodDao.getAllProducts();
        prodDao.close();

        refresh();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItems.get(position));
    }

    public void removeSelection() {
        mSelectedItems = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItems.put(position, value);
        else
            mSelectedItems.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItems.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItems;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater inflater =  LayoutInflater.from(MyApplication.getAppContext());
            convertView = inflater.inflate(R.layout.simple_list, null);

            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.list_name);
            holder.name.setSelected(true);

            holder.numOfElements = (TextView) convertView.findViewById(R.id.numOfElements);
            holder.numOfElements.setSelected(true);

            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (items.size() < 1) { // the first element is always present so this is never called

        } else {
            MyList list = items.get(position);
            int idList = list.getId();
            prodDao.open();
            // SET FAVOURITES SECTION
            if(position == 0 && idList==-1) {
                holder.name.setText(R.string.favourites);
                holder.name.setTypeface(null, Typeface.ITALIC);

                int num = prodDao.countFavourites();
                holder.numOfElements.setText(""+num);
                Log.d("numOfElement", "idList= " + idList + ", num= " + num);
            }
            else {
                holder.name.setText(list.getName());

                int num = prodDao.countElementsOfAList(idList);
                Log.d("numOfElement","idList= "+idList + ", num= "+num);
                holder.numOfElements.setText(""+num);
            }

            prodDao.close();

        }
        return convertView;
    }
}

