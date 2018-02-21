package com.app.project.scaneatapp.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.project.scaneatapp.MyApplication;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.db_products_in_list.ProductInList;
import java.util.ArrayList;
import java.util.List;

import scaneat.R;

public class ListDetailsAdapter extends BaseAdapter {
    List<ProductInList> productInLists = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    int id_list;
    String mail;
    int isFavourite;
    LayoutInflater inflater;
    private SparseBooleanArray mSelectedItems;
    private SparseBooleanArray mFavouriteItems;

    private MyProductInList_DAO prodDao;
    private List<ProductInList> items;

    private class ViewHolder {
        ImageView favourite;
        TextView name;
        TextView brand;
        TextView quantText;
        TextView quantity;
        ImageView noGluten;
        ImageView noLactose;
        ImageView vegan;
    }

    public ListDetailsAdapter(Context context, int id_list) {
        inflater = LayoutInflater.from(context);
        this.id_list = id_list;
        mSelectedItems = new SparseBooleanArray();
        mFavouriteItems = new SparseBooleanArray();
        populate();
    }


    public void populate() {
        prodDao = new MyProductInList_impl();
        prodDao.open();
        // GET ALL PRODUCTS IN LIST
        productInLists = prodDao.getAllProductsInList(id_list);

        items = productInLists;
        for(int i= items.size()-1; i>=0; i--) {
            if(items.get(i).getIsFavourite()==1) {
                mFavouriteItems.put(i,true);
            }
            else mFavouriteItems.put(i,false);
        }

        prodDao.close();

        items = order(items);
    }

    // order alphabetically
    private List<ProductInList> order(List<ProductInList> list) {
        ProductInList temp;
        for(int i = 0; i < list.size(); i++) {
            for (int j = (list.size() - 1); j > i; j--) {
                if (list.get(j).getName().compareToIgnoreCase(list.get(i).getName()) < 0) {
                    temp = list.get(j);
                    list.remove(j);
                    list.add(j, list.get(i));
                    list.remove(i);
                    list.add(i, temp);
                }
            }
        }
        return list;
    }

    public List<ProductInList> getItems() { return items; }

    @Override
    public int getCount() {
           return items.size();
    }

    @Override
    public ProductInList getItem(int location) {
        return items.get(location);
    }

    @Override
    public long getItemId(int location) {
        final ProductInList prod = getItem(location);
        return prod.getIdProd();
    }

    public void refresh() {
        populate();
        notifyDataSetChanged();
    }

    public void remove(ProductInList prod){
        prodDao.open();
        prodDao.deleteProduct(prod);
        prodDao.close();
        items.remove(prod);
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

    public void addFavourite(ProductInList prod) {
        prodDao.open();
        prodDao.setFavourite(prod);
        Log.d("databaseSetChecked", prodDao.getAllProductsInList(prod.getIdList()).toString());
        prodDao.close();
        refresh();
    }

    public int getSelectedCount() {
        return mSelectedItems.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItems;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater inflater =  LayoutInflater.from(MyApplication.getAppContext());
            convertView = inflater.inflate(R.layout.product_in_list, null);

            holder = new ViewHolder();
            holder.favourite = (ImageView) convertView.findViewById(R.id.favourite);

            holder.name = (TextView) convertView.findViewById(R.id.product_name1);
            holder.name.setSelected(true);
            holder.brand = (TextView) convertView.findViewById(R.id.product_brand1);
            holder.brand.setSelected(true);
            holder.quantText = (TextView) convertView.findViewById(R.id.tvQuantity);
            holder.quantity = (TextView) convertView.findViewById(R.id.product_quantity1);
            holder.noGluten = (ImageView) convertView.findViewById(R.id.nogluten_icon);
            holder.noLactose = (ImageView) convertView.findViewById(R.id.nomilk_icon);
            holder.vegan = (ImageView) convertView.findViewById(R.id.vegan_icon);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (items.size() <= 0){

            convertView.setVisibility(View.GONE);

        } else {
            // SET PRODUCTS PROPERTIES
            final ProductInList prod = items.get(position);

            // INITIALIZE mFavouriteItems
            if(prod.getIsFavourite()==1) {
                //mFavouriteItems.put(position,true);
                holder.favourite.setImageResource(R.drawable.ic_star_24dp);
            }
            else {

                holder.favourite.setImageResource(R.drawable.ic_star_outline_24dp);
            }

            // MANAGE IMAGE CHANGING
            holder.favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prod.getIsFavourite()==0) { //  = se non è tra i preferiti...
                        holder.favourite.setImageResource(R.drawable.ic_star_24dp);
                        prod.setIsFavourite(1); // ... lo aggiungo
                        //aggiungo ai preferiti
                        addFavourite(prod);
                        //mFavouriteItems.put(position, true);
                        Log.d("onclickfavourite","onclick "+prod.getIsFavourite());
                    }
                    else {
                        holder.favourite.setImageResource(R.drawable.ic_star_outline_24dp);
                        prod.setIsFavourite(0);
                        //rimuovo dai preferiti
                        addFavourite(prod);

                        Log.d("onclickfavourite2","onclick "+prod.getIsFavourite());
                    }

                }
            });

            holder.name.setText(prod.getName());

            holder.brand.setText(prod.getBrand());

            holder.quantText.setText(R.string.quantText);
            holder.quantity.setText("" + prod.getQuantity());

            if(prod.getNoGluten()==0) {
                holder.noGluten.setVisibility(View.GONE);
            }
            else
                holder.noGluten.setVisibility(View.VISIBLE);

            if(prod.getNoLactose()==0) {
                holder.noLactose.setVisibility(View.GONE);
            }
            else
                holder.noLactose.setVisibility(View.VISIBLE);

            if(prod.getVegan()==0) {
                holder.vegan.setVisibility(View.INVISIBLE);
            }
            else
                holder.vegan.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}

