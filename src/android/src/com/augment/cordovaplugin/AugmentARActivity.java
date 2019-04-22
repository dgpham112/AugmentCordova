package com.augment.cordovaplugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ar.augment.arplayer.AugmentPlayer;
import com.ar.augment.arplayer.AugmentPlayerException;
import com.ar.augment.arplayer.AugmentPlayerSDK;
import com.ar.augment.arplayer.InitializationListener;
import com.ar.augment.arplayer.LoaderCallback;
import com.ar.augment.arplayer.ProductDataController;
import com.ar.augment.arplayer.ProductQuery;
import com.ar.augment.arplayer.ScreenshotTakerCallback;
import com.ar.augment.arplayer.WebserviceException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AugmentARActivity extends Activity {

    static String LOGTAG = "AugmentARActivity";

    // Identifiers
    static final String METHOD_ADD_PRODUCT        = "addProduct";
    static final String METHOD_SHOW_ALERT_MESSAGE = "showAlertMessage";
    static final String METHOD_SHARE_SCREENSHOT   = "shareScreenshot";
    static final String METHOD_RECENTER_PRODUCTS  = "recenterProducts";

    AugmentPlayerSDK augmentPlayerSDK;
    GLSurfaceView augmentView;
    Button helpButton;
    LinearLayout bottomBarLinearLayout;
    View loadingContainer;
    TextView loadingTextView;

    // Please see Augment UX recommendations about how to onboard your users
    View tutorialContainer;
    ViewPager tutorialViewPager;
    TextView tutorialPagerTextView;
    Button tutorialGotItButton;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(AugmentCordova.AUGMENT_INTENT_EXTRA_ACTION);
            if (METHOD_RECENTER_PRODUCTS.equals(action)) {
                recenterProducts();
            }
            else if (METHOD_SHARE_SCREENSHOT.equals(action)) {
                shareScreenshot();
            }
            else if (METHOD_ADD_PRODUCT.equals(action)) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(AugmentCordova.ARG_IDENTIFIER, intent.getStringExtra(AugmentCordova.ARG_IDENTIFIER));
                data.put(AugmentCordova.ARG_BRAND,      intent.getStringExtra(AugmentCordova.ARG_BRAND));
                data.put(AugmentCordova.ARG_NAME,       intent.getStringExtra(AugmentCordova.ARG_NAME));
                data.put(AugmentCordova.ARG_EAN,        intent.getStringExtra(AugmentCordova.ARG_EAN));
                load(data);
            }
            else if (METHOD_SHOW_ALERT_MESSAGE.equals(action)) {
                showAlertMessage(
                        intent.getStringExtra(AugmentCordova.ARG_TITLE),
                        intent.getStringExtra(AugmentCordova.ARG_MESSAGE),
                        intent.getStringExtra(AugmentCordova.ARG_BUTTON_TEXT)
                );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource("activity_augment"));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(AugmentCordova.AUGMENT_LOCAL_BROADCAST_EXEC));

        augmentView           = (GLSurfaceView) findViewById(getIdResource("augmentView"));
        loadingContainer      = findViewById(getIdResource("loadingContainer"));
        loadingTextView       = (TextView) findViewById(getIdResource("loadingTextView"));
        tutorialContainer     = findViewById(getIdResource("tutorialContainer"));
        tutorialViewPager     = (ViewPager) findViewById(getIdResource("tutorialViewpager"));
        tutorialPagerTextView = (TextView) findViewById(getIdResource("tutorialPagerTextView"));
        tutorialGotItButton   = (Button) findViewById(getIdResource("tutorialGotItButton"));
        helpButton            = (Button) findViewById(getIdResource("helpButton"));
        bottomBarLinearLayout = (LinearLayout) findViewById(getIdResource("bottomBarLinearLayout"));

        // We are using a basic page adapter to show a multi-page tutorial
        tutorialViewPager.setAdapter(new PagerAdapter() {

            int[] layouts = {
                    getLayoutResource("tutorial_screen_01"),
                    getLayoutResource("tutorial_screen_02"),
                    getLayoutResource("tutorial_screen_03"),
                    getLayoutResource("tutorial_screen_04")
            };

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                LayoutInflater inflater = LayoutInflater.from(AugmentARActivity.this);
                ViewGroup layout = (ViewGroup) inflater.inflate(layouts[position], container, false);
                container.addView(layout);
                return layout;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return layouts.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });

        tutorialViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            String[] pagerText = {"● ○ ○ ○", "○ ● ○ ○", "○ ○ ● ○", "○ ○ ○ ●"};

            @Override
            public void onPageSelected(int position) {
                tutorialPagerTextView.setText(pagerText[position]);
                tutorialGotItButton.setVisibility(position < (pagerText.length - 1) ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        tutorialGotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorialHide();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorialShow();
            }
        });

        addUIElements();
        prepareARSession();

        Intent intent = new Intent(AugmentCordova.AUGMENT_LOCAL_BROADCAST_STARTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Create the user defined button with associated javascript actions
     * see AugmentCordova.AugmentCordovaConfig.UIElements for more details
     */
    void addUIElements() {

        int margin = 4;

        // Add custom actions button
        ArrayList<HashMap<String, String>> elements = AugmentCordova.AugmentCordovaConfig.UIElements;
        if (elements != null) {
            for (int i = 0, max = elements.size(); i < max; i++) {
                HashMap<String, String> element = elements.get(i);

                final String code = element.get(AugmentCordova.ARG_CODE);
                AugmentCordovaButton button = new AugmentCordovaButton(this);
                button.applyData(element);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                );
                params.setMargins(margin, margin, margin, margin);
                button.setLayoutParams(params);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // This call is deprecated, see what we can do instead
                        //noinspection deprecation
                        AugmentCordova.webViewReference.sendJavascript(code);
                    }
                });
                bottomBarLinearLayout.addView(button);
            }
        }
    }

    void tutorialHide() {
        tutorialContainer.setVisibility(View.GONE);
        tutorialViewPager.setCurrentItem(0);
        bottomBarLinearLayout.setVisibility(View.VISIBLE);
    }

    void tutorialShow() {
        tutorialContainer.setVisibility(View.VISIBLE);
        bottomBarLinearLayout.setVisibility(View.GONE);
    }

    /**
     * Init the Augment Player with our AppKeys and then init the AR view itself
     */
    private void prepareARSession() {
        // Init the Augment Player
        augmentPlayerSDK = new AugmentPlayerSDK(this.getApplicationContext(),
                AugmentCordova.AugmentCordovaConfig.AppId,
                AugmentCordova.AugmentCordovaConfig.AppKey,
                AugmentCordova.AugmentCordovaConfig.VuforiaKey
        );

        // Start the AR view and get a callback when the view is ready
        final AugmentPlayer augmentPlayer = augmentPlayerSDK.getAugmentPlayer();
        augmentPlayer.initAR(this, augmentView, new InitializationListener() {

            @Override
            public void onInitARDone(GLSurfaceView glSurfaceView, AugmentPlayerException e) {
                // Check if there is no error
                if (e != null) {
                    Log.e(LOGTAG, e.getLocalizedMessage(), e);
                    hideLoading();
                    showAlert("Error: " + e.getLocalizedMessage());
                    return;
                }

                // For now it is the responsibility of the developer to load this asynchronously, this will evolve in the future
                (new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            augmentPlayer.start();
                            augmentPlayer.resume();
                        } catch (AugmentPlayerException e) {
                            Log.e(LOGTAG, e.getLocalizedMessage(), e);
                            hideLoading();
                            showAlert("Error: " + e.getLocalizedMessage());
                        }
                        return null;
                    }
                }).execute();
            }
        });
    }

    void load(HashMap<String, String> product) {

        ProductQuery query = AugmentCordova.BuildProductQueryFromProductHashMap(product);
        augmentPlayerSDK.getProductDataController().checkIfModelDoesExistForUserProductQuery(query, new ProductDataController.ProductQueryListener() {
            @Override
            public void onResponse(@Nullable com.ar.augment.arplayer.Product augmentProduct) {
                if (augmentProduct == null) {
                    Log.d(LOGTAG, "Product is not available.");
                    hideLoading();
                    showAlert("This product is not available yet");
                    return;
                }

                addToPlayer(augmentProduct);
            }

            @Override
            public void onError(WebserviceException error) {
                Log.e(LOGTAG, "Product is not available.", error);
                hideLoading();
                showAlert("This product is not available yet");
            }
        });
    }

    void addToPlayer(@NonNull com.ar.augment.arplayer.Product product) {
        augmentPlayerSDK.addModel3DToAugmentPlayerWithProduct(product, new LoaderCallback() {
            @Override
            public void onSuccess(String modelIdentifier) {
                hideLoading();
            }

            @Override
            public void onPreparingModel() {
                // Show loading (already present and shown)
            }

            @Override
            public void onError(WebserviceException error) {
                Log.e(LOGTAG, "Error", error);
                hideLoading();
                showAlert("Error: " + error.getLocalizedMessage());
            }

            @Override
            public void onDownloadProgressUpdate(final Long aLong) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTextView.setText("Loading " + Math.min(100, aLong.intValue()) + "%");
                    }
                });
            }
        });
    }

    // Theses are mandatory overwrites of lifecycle activity method
    // to keep the AR view working nicely

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentPlayerSDK != null) {
            try {
                augmentPlayerSDK.getAugmentPlayer().resume();
            } catch (AugmentPlayerException e) {
                Log.e(LOGTAG, "EXCEPTION: " + e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (augmentPlayerSDK != null) {
            try {
                augmentPlayerSDK.getAugmentPlayer().pause();
            } catch (AugmentPlayerException e) {
                Log.e(LOGTAG, "EXCEPTION: " + e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (augmentPlayerSDK != null) {
            try {
                augmentPlayerSDK.getAugmentPlayer().stop();
            } catch (AugmentPlayerException e) {
                Log.e(LOGTAG, "EXCEPTION: " + e.getLocalizedMessage(), e);
            }
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    // Actions

    void recenterProducts() {
        if (augmentPlayerSDK == null) return;
        augmentPlayerSDK.getAugmentPlayer().recenterProducts();
    }

    @SuppressLint("SetTextI18n")
    void shareScreenshot() {
        if (augmentPlayerSDK == null) return;
        loadingTextView.setText("Screenshot in progress…");
        loadingContainer.setVisibility(View.VISIBLE);

        if (augmentPlayerSDK == null) return;
        augmentPlayerSDK.getAugmentPlayer().takeScreenshot(new ScreenshotTakerCallback() {
            @Override
            public void onScreenshotSaved(final File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        LayoutInflater inflater = getLayoutInflater();
                        @SuppressLint("InflateParams")
                        View view = inflater.inflate(getLayoutResource("dialog_preview_share"), null);
                        ImageView imageView = (ImageView) view.findViewById(getIdResource("previewImageView"));
                        imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                        showCustomDialog(view, "Share", new Runnable() {
                            @Override
                            public void run() {
                                androidShareImage(file);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        showAlert(throwable.getLocalizedMessage());
                    }
                });
            }
        });
    }

    // Helpers

    int getLayoutResource(String name) {
        return getResources().getIdentifier(name, "layout", getPackageName());
    }

    int getIdResource(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    public void androidShareImage(File image) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        Uri imageUri = Uri.fromFile(image);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        startActivity(Intent.createChooser(shareIntent, "Your screenshot is ready to be shared"));
    }

    /**
     * @param view View
     * @param buttonText String
     * @param then Runnable
     */
    public void showCustomDialog(final View view, @Nullable String buttonText, @Nullable final Runnable then) {
        if (buttonText == null) {
            buttonText = "OK";
        }

        final String finalButtonText = buttonText;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                final DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (then != null) {
                            then.run();
                        }
                    }
                };

                new AlertDialog.Builder(AugmentARActivity.this)
                        .setView(view)
                        .setPositiveButton(finalButtonText, onClick)
                        .create()
                        .show();
            }
        });
    }

    public void showAlertMessage(final String title, final String message, final String buttonText) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                new AlertDialog.Builder(AugmentARActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        })
                        .create()
                        .show();
            }
        });
    }

    /**
     * Show a message to the user as an Android standard Dialog
     * @param message String message to show
     */
    void showAlert(@NonNull final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                new AlertDialog.Builder(AugmentARActivity.this)
                        .setTitle(message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        })
                        .create()
                        .show();
            }
        });
    }

    void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingContainer.setVisibility(View.GONE);
            }
        });
    }
}
