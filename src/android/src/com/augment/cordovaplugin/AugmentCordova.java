package com.augment.cordovaplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.ar.augment.arplayer.AugmentPlayerSDK;
import com.ar.augment.arplayer.Product;
import com.ar.augment.arplayer.ProductDataController;
import com.ar.augment.arplayer.ProductQuery;
import com.ar.augment.arplayer.WebserviceException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
public class AugmentCordova extends CordovaPlugin {

    static final int AUGMENT_AR_ACTIVITY = 467;

    static final String AUGMENT_LOCAL_BROADCAST_EXEC    = "com.augment.cordova-plugin.exec";
    static final String AUGMENT_LOCAL_BROADCAST_STARTED = "com.augment.cordova-plugin.started";
    static final String AUGMENT_INTENT_EXTRA_ACTION     = "action";

    static CordovaWebView webViewReference = null;

    // State management
    boolean isAugmentARActivityIsStarted = false;

    // Arguments keys

    static final String ARG_APP_ID      = "id";
    static final String ARG_APP_KEY     = "key";
    static final String ARG_VUFORIA_KEY = "vuforia";
    static final String ARG_UI_ELEMENTS = "uiElements";
    static final String ARG_TITLE       = "title";
    static final String ARG_MESSAGE     = "message";
    static final String ARG_BUTTON_TEXT = "buttonText";
    static final String ARG_IDENTIFIER  = "identifier";
    static final String ARG_BRAND       = "brand";
    static final String ARG_NAME        = "name";
    static final String ARG_EAN         = "ean";
    static final String ARG_CODE        = "code";

    // Javascript methods

    static final String JS_METHOD_INIT_PLUGIN                                = "initPlugin";
    static final String JS_METHOD_CHECK_IF_MODEL_DOES_EXIST_FOR_USER_PRODUCT = "checkIfModelDoesExistForUserProduct";
    static final String JS_METHOD_START                                      = "start";
    static final String JS_METHOD_ADD_PRODUCT_TO_AUGMENT_PLAYER              = "addProductToAugmentPlayer";
    static final String JS_METHOD_RECENTER_PRODUCTS                          = "recenterProducts";
    static final String JS_METHOD_SHARE_SCREENSHOT                           = "shareScreenshot";
    static final String JS_METHOD_SHOW_ALERT_MESSAGE                         = "showAlertMessage";
    static final String JS_METHOD_STOP                                       = "stop";

    // Results keys

    static final String ARG_ERROR   = "error";
    static final String ARG_SUCCESS = "success";

    /**
     * Static to keep configuration values that we need to access from everywhere
     */
    static class AugmentCordovaConfig {

        static String AppId      = null;
        static String AppKey     = null;
        static String VuforiaKey = null;

        /**
         * This is an ArrayList of HashMap of String, String
         *
         * It is a hacky way to allow our external developers to customize the ARView
         * by adding buttons they need that will trigger their Javascript logic
         *
         * see Javascript documentation for more information
         */
        static ArrayList<HashMap<String, String>> UIElements = new ArrayList<HashMap<String, String>>();
    }

    /**
     * This method corresponds to `AugmentCordova.init`
     * args[0] is a HashMap object with "id" "key" "vuforia" keys
     * it may have an optional "uiElements" keys that is itself a HashMap (@see AugmentCordovaConfig.UIElements)
     */
    public void initPlugin(JSONArray args, CallbackContext callbackContext) {
        try {
            JSONObject data = args.getJSONObject(0);
            AugmentCordovaConfig.AppId      = data.getString(ARG_APP_ID);
            AugmentCordovaConfig.AppKey     = data.getString(ARG_APP_KEY);
            AugmentCordovaConfig.VuforiaKey = data.getString(ARG_VUFORIA_KEY);

            if (data.has(ARG_UI_ELEMENTS)) {
                AugmentCordovaConfig.UIElements = buildUIElementsFromArgs(data.getJSONArray(ARG_UI_ELEMENTS));
            }

            callbackContext.success(getJSONObjectForSuccess(null));
        } catch (JSONException e) {
            callbackContext.error(getJSONObjectForErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * This method corresponds to `AugmentCordova.checkIfModelDoesExistForUserProduct`
     * args[0] is a HashMap object that represent a product
     * it has "identifier" "brand" "name" and "ean" keys
     * This method returns an augmentProduct thru the Cordova callback mechanism
     */
    public void checkIfModelDoesExistForUserProduct(JSONArray args, final CallbackContext callbackContext) {
        try {
            JSONObject data = args.getJSONObject(0);
            HashMap<String, String> product = buildProductHashMapFromArgs(data);
            ProductQuery query = BuildProductQueryFromProductHashMap(product);

            AugmentPlayerSDK augmentPlayerSDK = new AugmentPlayerSDK(cordova.getActivity().getApplicationContext(),
                    AugmentCordova.AugmentCordovaConfig.AppId,
                    AugmentCordova.AugmentCordovaConfig.AppKey,
                    AugmentCordova.AugmentCordovaConfig.VuforiaKey
            );

            augmentPlayerSDK.getProductDataController().checkIfModelDoesExistForUserProductQuery(query, new ProductDataController.ProductQueryListener() {
                @Override
                public void onResponse(@Nullable Product product) {
                    try {
                        callbackContext.success(getJSONObjectForProduct(product));
                    } catch (JSONException e) {
                        callbackContext.error(getJSONObjectForErrorMessage(e.getLocalizedMessage()));
                    }
                }

                @Override
                public void onError(WebserviceException e) {
                    callbackContext.error(getJSONObjectForErrorMessage(e.getLocalizedMessage()));
                }
            });
        } catch (JSONException e) {
            callbackContext.error(getJSONObjectForErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * This method corresponds to `AugmentCordova.start`
     *
     * It is a little tricky as it register for a broadcast receiver that will be triggered by the AugmentARActivity
     * It is to be able to execute the Javascript callback when the ARView is ready to get commands
     */
    public void start(final CallbackContext callbackContext) {
        final Context context = cordova.getActivity().getApplicationContext();
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        isAugmentARActivityIsStarted = true;
                        // Start the Javascript success callback when the ARView is ready
                        callbackContext.success(getJSONObjectForSuccess(null));
                        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                    }
                };

                LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(AugmentCordova.AUGMENT_LOCAL_BROADCAST_STARTED));
                Intent intent = new Intent(context, AugmentARActivity.class);
                cordova.getActivity().startActivityForResult(intent, AUGMENT_AR_ACTIVITY);
            }
        });
    }

    /**
     * This method corresponds to `AugmentCordova.addProductToAugmentPlayer`
     * args[0] is a HashMap object that represent a product
     * it has "identifier" "brand" "name" and "ean" keys
     * This method needs to be called after the success of `AugmentCordova.start`
     */
    public void addProductToAugmentPlayer(JSONArray args, CallbackContext callbackContext) {
        if (!isAugmentARActivityIsStarted) {
            callbackContext.error(getJSONObjectForErrorMessage("addProductToAugmentPlayer() must be used after a success call to start()"));
            return;
        }

        try {
            JSONObject data = args.getJSONObject(0);
            HashMap<String, String> product = buildProductHashMapFromArgs(data);
            executeOnAugmentARActivity(AugmentARActivity.METHOD_ADD_PRODUCT, product);
            callbackContext.success(getJSONObjectForSuccess(null));
        } catch (JSONException e) {
            callbackContext.error(getJSONObjectForErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * This method corresponds to `AugmentCordova.recenterProducts`
     * This method needs to be called after the success of `AugmentCordova.start`
     */
    public void recenterProducts(final CallbackContext callbackContext) {
        if (!isAugmentARActivityIsStarted) {
            callbackContext.error(getJSONObjectForErrorMessage("recenterProducts() must be used after a success call to start()"));
            return;
        }

        executeOnAugmentARActivity(AugmentARActivity.METHOD_RECENTER_PRODUCTS, null);
        callbackContext.success(getJSONObjectForSuccess(null));
    }

    /**
     * This method corresponds to `AugmentCordova.shareScreenshot`
     * This method needs to be called after the success of `AugmentCordova.start`
     */
    public void shareScreenshot(final CallbackContext callbackContext) {
        if (!isAugmentARActivityIsStarted) {
            callbackContext.error(getJSONObjectForErrorMessage("shareScreenshot() must be used after a success call to start()"));
            return;
        }

        executeOnAugmentARActivity(AugmentARActivity.METHOD_SHARE_SCREENSHOT, null);
        callbackContext.success(getJSONObjectForSuccess(null));
    }

    /**
     * This method corresponds to `AugmentCordova.showAlertMessage`
     * args[0] is a HashMap object with "title" "message" "buttonText" keys
     * This method needs to be called after the success of `AugmentCordova.start`
     */
    public void showAlertMessage(JSONArray args, CallbackContext callbackContext) {
        if (!isAugmentARActivityIsStarted) {
            callbackContext.error(getJSONObjectForErrorMessage("showAlertMessage() must be used after a success call to start()"));
            return;
        }

        try {
            JSONObject data = args.getJSONObject(0);
            HashMap<String, String> info = new HashMap<String, String>();
            info.put(ARG_TITLE,       data.getString(ARG_TITLE));
            info.put(ARG_MESSAGE,     data.getString(ARG_MESSAGE));
            info.put(ARG_BUTTON_TEXT, data.getString(ARG_BUTTON_TEXT));
            executeOnAugmentARActivity(AugmentARActivity.METHOD_SHOW_ALERT_MESSAGE, info);
            callbackContext.success(getJSONObjectForSuccess(null));
        } catch (JSONException e) {
            callbackContext.error(getJSONObjectForErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * This method corresponds to `AugmentCordova.stop`
     * This method needs to be called after the success of `AugmentCordova.start`
     */
    public void stop(final CallbackContext callbackContext) {
        if (!isAugmentARActivityIsStarted) {
            callbackContext.error(getJSONObjectForErrorMessage("stop() must be used after a success call to start()"));
            return;
        }

        cordova.getActivity().finishActivity(AUGMENT_AR_ACTIVITY);
        callbackContext.success(getJSONObjectForSuccess(null));
    }

    // Cordova Plugin Overrides

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        webViewReference = webView;
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (JS_METHOD_INIT_PLUGIN.equals(action)) {
            initPlugin(args, callbackContext);
            return true;
        }
        else if (JS_METHOD_CHECK_IF_MODEL_DOES_EXIST_FOR_USER_PRODUCT.equals(action)) {
            checkIfModelDoesExistForUserProduct(args, callbackContext);
            return true;
        }
        else if (JS_METHOD_START.equals(action)) {
            start(callbackContext);
            return true;
        }
        else if (JS_METHOD_ADD_PRODUCT_TO_AUGMENT_PLAYER.equals(action)) {
            addProductToAugmentPlayer(args, callbackContext);
            return true;
        }
        else if (JS_METHOD_RECENTER_PRODUCTS.equals(action)) {
            recenterProducts(callbackContext);
            return true;
        }
        else if (JS_METHOD_SHARE_SCREENSHOT.equals(action)) {
            shareScreenshot(callbackContext);
            return true;
        }
        else if (JS_METHOD_SHOW_ALERT_MESSAGE.equals(action)) {
            showAlertMessage(args, callbackContext);
            return true;
        }
        else if (JS_METHOD_STOP.equals(action)) {
            stop(callbackContext);
            return true;
        }

        return false;
    }

    // Helpers

    protected void executeOnAugmentARActivity(String methodName, @Nullable Map<String, String> data) {
        Intent intent = new Intent(AUGMENT_LOCAL_BROADCAST_EXEC);
        intent.putExtra(AUGMENT_INTENT_EXTRA_ACTION, methodName);
        if (data != null) {
            for (Map.Entry<String, String> entry: data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        LocalBroadcastManager.getInstance(cordova.getActivity().getApplicationContext()).sendBroadcast(intent);
    }

    protected JSONObject getJSONObjectForProduct(@Nullable Product product) throws JSONException {
        JSONObject result = new JSONObject();
        if (product == null) {
            return result;
        }

        result.put("identifier", product.getIdentifier());
        result.put("ean", product.getEan());
        result.put("brand", product.getBrand());
        result.put("name", product.getName());
        result.put("model_number", product.getModelNumber());
        return result;
    }

    protected JSONObject getJSONObjectForErrorMessage(String message) {
        JSONObject result = new JSONObject();
        try {
            result.put(ARG_ERROR, message);
        } catch (JSONException e) {
            // OMG Error in error ...
            e.printStackTrace();
        }
        return result;
    }

    protected JSONObject getJSONObjectForSuccess(@Nullable String message) {
        if (message == null) {
            message = "ok";
        }

        JSONObject result = new JSONObject();
        try {
            result.put(ARG_SUCCESS, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected HashMap<String, String> buildProductHashMapFromArgs(JSONObject data) throws JSONException {
        HashMap<String, String> product = new HashMap<String, String>();
        product.put(ARG_IDENTIFIER, data.getString(ARG_IDENTIFIER));
        product.put(ARG_BRAND,      data.getString(ARG_BRAND));
        product.put(ARG_NAME,       data.getString(ARG_NAME));
        if (data.has(ARG_EAN)) {
            product.put(ARG_EAN, data.getString(ARG_EAN));
        }
        else {
            product.put(ARG_EAN, "");
        }
        return product;
    }

    protected ArrayList<HashMap<String, String>> buildUIElementsFromArgs(JSONArray data) throws JSONException {
        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        for (int i = 0, max = data.length(); i < max; i++) {
            JSONObject current = data.getJSONObject(i);
            HashMap<String, String> interm = new HashMap<String, String>();
            Iterator<String> keys = current.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                interm.put(key, current.getString(key));
            }
            result.add(interm);
        }
        return result;
    }

    public static ProductQuery BuildProductQueryFromProductHashMap(HashMap<String, String> product) {
        ProductQuery.Builder builder = new ProductQuery.Builder(
                product.get(ARG_IDENTIFIER),
                product.get(ARG_BRAND),
                product.get(ARG_NAME)
        );
        if (!product.get(ARG_EAN).isEmpty()) {
            builder.setEan(product.get(ARG_EAN));
        }
        return builder.build();
    }
}
