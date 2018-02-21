package com.app.project.scaneatapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.app.project.scaneatapp.MyApplication;
import com.app.project.scaneatapp.RecyclerViewHolder;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import java.util.List;
import scaneat.R;

public class MySearchProductsAdapter extends RecyclerView.Adapter {

    private List<Product> products;
    private LayoutInflater inflater;
    private MyProduct_DAO dao;
    private Context context;
    private SparseBooleanArray sparse;
    private RecyclerClickListener mListener;

    public MySearchProductsAdapter(String search, Context context, RecyclerClickListener mListener) {
        populate(search);
        this.context = context;
        inflater = LayoutInflater.from(MyApplication.getAppContext());
        sparse = new SparseBooleanArray();
        this.mListener = mListener;
    }

    public MySearchProductsAdapter(String user, Context context, RecyclerClickListener mListener, boolean forPersonalArea){
        populateWithMail(user);
        this.context = context;
        inflater = LayoutInflater.from(MyApplication.getAppContext());
        sparse = new SparseBooleanArray();
        this.mListener = mListener;
    }

    public Context getContext()
    {return this.context;}

    public SparseBooleanArray getSelectedId(){
        return sparse;
    }


    public void populate(String search) {
        dao = new MyProduct_impl();
        dao.open();
        products = dao.getFilteredProducts(search);

        dao.close();
    }

    public void populateWithMail(String mail) {
        dao = new MyProduct_impl();
        dao.open();
        products = dao.getUserProducts(mail);

        dao.close();
    }

    public List<Product> getProducts() {
        return products;
    }

    public Product getItem(int location){
        return products.get(location);
    }

    @Override
    public int getItemCount() {
        return (null != products ? products.size() : 0);
    }


    @Override // QUI METTERE L'OGGETTO
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(MyApplication.getAppContext());
        View view = inflater.inflate(R.layout.product_in_search, null);
        RecyclerViewHolder holder = new RecyclerViewHolder(view);
        return holder;
    }

    public interface RecyclerClickListener{
        void onItemClicked(Product prod);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final RecyclerViewHolder current = (RecyclerViewHolder) holder;
        final Product prod = products.get(position);
        current.getName().setText(prod.getName());
        current.getBrand().setText(prod.getBrand());
        if (prod.getNoGluten() == 0) {
            current.getNoGluten().setVisibility(View.GONE);
        } else
            current.getNoGluten().setVisibility(View.VISIBLE);
        if (prod.getNoLactose() == 0) {
            current.getNoLactose().setVisibility(View.GONE);
        } else
            current.getNoLactose().setVisibility(View.VISIBLE);
        if (prod.getVegan() == 0) {
            current.getVegan().setVisibility(View.INVISIBLE);
        } else
            current.getVegan().setVisibility(View.VISIBLE);

        current.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClicked(products.get(position));
            }
        });
    }
}




