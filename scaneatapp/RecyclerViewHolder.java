package com.app.project.scaneatapp;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import scaneat.R;


public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    private TextView name;
    private TextView brand;
    private ImageView noGluten;
    private ImageView noLactose;
    private ImageView vegan;

    public RecyclerViewHolder (View view) {
        super(view);
        this.name = (TextView) view.findViewById(R.id.product_name_search);
        name.setSelected(true);
        this.brand = (TextView) view.findViewById(R.id.product_brand_search);
        brand.setSelected(true);
        this.noGluten = (ImageView) view.findViewById(R.id.nogluten_icon_search);
        this.noLactose = (ImageView) view.findViewById(R.id.nomilk_icon_search);
        this.vegan = (ImageView) view.findViewById(R.id.vegan_icon_search);
    }


    public void setName(TextView name) {
        this.name = name;
    }
    public void setBrand(TextView brand) {
        this.brand = brand;
    }
    public TextView getName(){
        return this.name;
    }
    public TextView getBrand(){
        return this.brand;
    }
    public ImageView getNoGluten(){
        return this.noGluten;
    }
    public ImageView getNoLactose(){
        return this.noLactose;
    }
    public ImageView getVegan(){
        return this.vegan;
    }
}