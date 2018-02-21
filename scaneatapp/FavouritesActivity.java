package com.app.project.scaneatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.app.project.scaneatapp.adapters.FavouritesAdapter;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import com.app.project.scaneatapp.db_products_in_list.ProductInList;
import com.app.project.scaneatapp.dialogs.MyProductInfoDialog;

import scaneat.R;

public class FavouritesActivity extends AppCompatActivity  implements FavouritesAdapter.RemoveFavouriteListener{

    android.support.v7.widget.Toolbar toolbar;
    TextView tv;
    ListView lv;
    MyProductInList_DAO prodDao  = new MyProductInList_impl();
    FavouritesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.favourites);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // SET ADAPTER
        adapter = new FavouritesAdapter(FavouritesActivity.this, this);
        lv = (ListView) findViewById(R.id.myFavouritesLv);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] array;
                prodDao.open();
                ProductInList prod = adapter.getItem(position);
                prodDao.close();
                int idList = -1; // l'idList dei preferiti è -1 (non è una vera e propria lista
                boolean addToCurrentList = false; // la lista dei preferiti non è da considerarsi
                if(prod.getBarcode()==-1) {
                    array = new String[]{Integer.toString(prod.getIdProd()), prod.getName(), prod.getBrand(), null,
                            Long.toString(prod.getBarcode()), Integer.toString(prod.getNoGluten()), Integer.toString(prod.getNoLactose()),
                            Integer.toString(prod.getVegan()), Integer.toString(idList), Boolean.toString(addToCurrentList)};
                }
                else {
                    MyProduct_DAO product_dao = new MyProduct_impl();
                    product_dao.open();
                    Product fromExtDb = product_dao.getProduct(prod.getBarcode());
                    product_dao.close();
                    array = new String[]{Integer.toString(prod.getIdProd()), fromExtDb.getName(), fromExtDb.getBrand(),
                            fromExtDb.getIngredients(), Long.toString(fromExtDb.getBarcode()),
                            Integer.toString(fromExtDb.getNoGluten()), Integer.toString(fromExtDb.getNoLactose()),
                            Integer.toString(fromExtDb.getVegan()), Integer.toString(idList), Boolean.toString(addToCurrentList)};
                }
                MyProductInfoDialog dialog = MyProductInfoDialog.newInstance(array);
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                dialog.show(manager, "SEARCH_DIALOG");
            }
        });

        // IF THE ADAPTER IS EMPTY
        tv = (TextView) findViewById(R.id.no_favourites);
        if (adapter.getCount() < 1) {
            tv.setVisibility(View.VISIBLE);
        } else tv.setVisibility(View.INVISIBLE);

    }

    @Override
    public void removeFavourite(ProductInList prod) {
            prodDao.open();
            // RIMUOVO DAI PREFERITI
            prodDao.setFavourite(prod);
            prodDao.close();

            adapter.refresh();

    }
}
