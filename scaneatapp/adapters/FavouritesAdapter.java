package com.app.project.scaneatapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.project.scaneatapp.MyApplication;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.db_products_in_list.ProductInList;
import java.util.ArrayList;
import java.util.List;
import scaneat.R;


public class FavouritesAdapter extends BaseAdapter {
    List<ProductInList> favourites = new ArrayList<>();

    LayoutInflater inflater;
    RemoveFavouriteListener mListener;

    Context context;

    private MyProductInList_DAO prodDao;
    private List<ProductInList> items;

    private class ViewHolder {
        ImageView favourite;
        TextView name;
        TextView brand;
        ImageView noGluten;
        ImageView noLactose;
        ImageView vegan;
    }

    public interface RemoveFavouriteListener {
        void removeFavourite(ProductInList prod);
    }

    public FavouritesAdapter(Context context, RemoveFavouriteListener mListener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.mListener = mListener;
        populate();
    }

    public void populate() {
        prodDao = new MyProductInList_impl();
        prodDao.open();
        favourites = prodDao.getFavourites();
        items = favourites;
        prodDao.close();
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater inflater =  LayoutInflater.from(MyApplication.getAppContext());
            convertView = inflater.inflate(R.layout.product_favourite, null);

            holder = new ViewHolder();
            holder.favourite = (ImageView) convertView.findViewById(R.id.favourite);
            holder.name = (TextView) convertView.findViewById(R.id.product_name1);
            holder.name.setSelected(true);
            holder.brand = (TextView) convertView.findViewById(R.id.product_brand1);
            holder.brand.setSelected(true);
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

            holder.favourite.setImageResource(R.drawable.ic_star_24dp);

            // MANAGE IMAGE CHANGING
            holder.favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.remove_favourites);
                    builder.setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ... remove from favourites
                            prod.setIsFavourite(0);
                            mListener.removeFavourite(prod);
                        }
                    })
                            .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    builder.show();
                }
            });

            holder.name.setText(prod.getName());

            holder.brand.setText(prod.getBrand());

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