package com.app.project.scaneatapp;


public class Constants {

    //Firebase app url
    public static final String FIREBASE_APP = "https://console.firebase.google.com/project/scaneatapp-e78d3/";

    //Constant to store shared preferences
    public static final String SHARED_PREF = "ScanEat";

    //To store the firebase id in shared preferences
    public static final String TOKEN = "token";

    // token-registration php-file address
    public static final String REGISTER_TOKEN_URL = "http://www.bonostaff.it/scaneat/php/registerToken.php";

    //registration php-file address
    public static final String REGISTER_URL = "http://www.bonostaff.it/scaneat/php/registrazione_mobile.php";

    //login php-file address
    public static final String LOGIN_URL = "http://www.bonostaff.it/scaneat/php/login_mobile.php";

    // change name file address
    public static final String URL_CHANGE_NAME = "http://www.bonostaff.it/scaneat/php/change_name.php";

    // change password file address
    public static final String URL_SIGN_UP = "http://www.bonostaff.it/scaneat/php/change_password.php";

    // get external db address
    public static final String URL_GET_PRODUCTS = "http://www.bonostaff.it/scaneat/php/getProductsDB.php";

    // propose a product address
    public static final String PROPOSE_A_PRODUCT_URL = "http://www.bonostaff.it/scaneat/php/insert_proposal_object.php";

    // propose a modify of a product address
    public static final  String PROPOSE_A_MODIFY_URL = "http://www.bonostaff.it/scaneat/php/insert_modify_object.php";

}