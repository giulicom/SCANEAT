package com.app.project.scaneatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.app.project.scaneatapp.db_products.MyProduct_DAO;
import com.app.project.scaneatapp.db_products.MyProduct_impl;
import com.app.project.scaneatapp.db_products.Product;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_DAO;
import com.app.project.scaneatapp.db_products_in_list.MyProductInList_impl;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import scaneat.R;

public class SyncExtDB {

    Context context;
    Activity parent;

    public SyncExtDB(Context context, Activity parent)
    {
        this.context = context;
        this.parent = parent;
    }

    public void syncDB(String timeStamp)
    {
        if(Utils.isOnline(context)) {
            try {
                new PostTask(parent).execute(timeStamp).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            Utils.displayNoConnectionAlert(context,true);
        }
    }

    public void syncDB(String timeStamp, boolean fromSyncDbActivity)
    {
        if(fromSyncDbActivity) {
            if (Utils.isOnline(context)) {
                try {
                    new PostTask(parent).execute(timeStamp).get();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                Utils.displayNoConnectionAlert(context, true);
            }
        }
    }
}

    class PostTask extends AsyncTask<String, String, String> {
        private final static String URL_GET_PRODUCTS = Constants.URL_GET_PRODUCTS;
        JSONArray json;
        JSONParser jsonParser = new JSONParser();
        private MyProduct_DAO dao = new MyProduct_impl();
        private Activity parent;

        public PostTask(Activity parent)
        {
            this.parent = parent;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... data) {
            // Create a new HttpClient and Post Header

            String lastUpdate = data[0];

            String toReturn;

            HashMap<String, String> hashMap = new HashMap<>(2);
            hashMap.put("database", "1");
            hashMap.put("ts", lastUpdate);

            json = jsonParser.serverRequestArray(URL_GET_PRODUCTS, hashMap);

            try {

                int jsonSize = json.length();
                JSONObject o;

                dao.open();
                for(int i=0; i<jsonSize; i++){

                    o = json.getJSONObject(i);
                    long barcode = o.getLong("barcode");
                    String name =  o.getString("name");
                    String brand = o.getString("brand");
                    String ingredients = o.getString("ingredients");
                    int noGluten = o.getInt("gluten_free");
                    int noLactose = o.getInt("lactose_free");
                    int vegan = o.getInt("vegan");
                    String user = o.getString("user");
                    Product p = new Product(barcode, name, brand, ingredients, noGluten, noLactose, vegan, user);

                    Product p1 = dao.getProduct(barcode);
                    Log.d("p1: " ,"");
                    if(p1==null){

                        dao.insertProduct(p);
                    } else {

                        dao.updateProduct(p);
                        updateProductsInList(p);
                    }

                }

                dao.close();
                toReturn = "1";

            } catch (Exception e) {
                e.printStackTrace();
                toReturn = "0";
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MyApplication.getAppContext(), R.style.AppTheme_Dark_Dialog);
                builder1.setTitle(R.string.oops_error);
                builder1.setMessage(R.string.something_went_wrong);
                builder1.setCancelable(false);
                builder1.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            return toReturn;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("1")) {
                Toast.makeText(parent.getBaseContext(), R.string.db_updated, Toast.LENGTH_SHORT).show();
            }
        }

        private void updateProductsInList(Product p){
            MyProductInList_DAO productInList_dao = new MyProductInList_impl();
            productInList_dao.open();
            productInList_dao.editProductByBarcode(p);
            productInList_dao.close();
        }
}
