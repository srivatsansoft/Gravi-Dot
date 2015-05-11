package com.example.srivatsan.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.LayoutGameActivity;


public class MainActivity extends LayoutGameActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    /*Scene scene;
    Body body;
    Sprite BlueDot;
    Text PauseTimerText;
    Rectangle greenarea,wall1,wall2,wall3;
    Sprite Gravi;
    Font TextFont;
    int Start=0;*/
    public static Context c;
    SceneManager sceneManager;
    private InterstitialAd interstitial;
    protected static final int CAMERA_WIDTH = 800;
    protected static final int CAMERA_HEIGHT = 480;
    protected static int AD_INTERVAL = 20 ; // in seconds
    // Client used to interact with Google APIs
    public static GoogleApiClient mGoogleApiClient;
    SharedPreferences.OnSharedPreferenceChangeListener BannerListener,InterstitialListener;
    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // request codes we use when invoking an external activity
    public static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    static AdView adView;
    // tag for debug logging
    final String TAG = "TanC";

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();


    /*BitmapTextureAtlas MenuSceneTexture;
    ITextureRegion playerTextureRegion,playerTextureRegionPause,playerTextureRegionPlay,playerTextureRegionMainChar,playerTextureRegionTouchStart;
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.d("oncreate()","entered");
        adView = (AdView) this.findViewById(R.id.adView);

        // Request for Ads
        AdRequest adRequest = new AdRequest.Builder()

          // / Add a test device to show Test Ads
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("3B7F9C99755A03D44BCACE7E91EAD130")
                .build();

        // Load ads into Banner Ads
        adView.loadAd(adRequest);

        SharedPreferences prefs = getSharedPreferences("GAME", 0);
        BannerListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(sharedPreferences.getString("Adview","false").equals("true")){
                   /* RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    //relativeLayout.removeView(adView);

                    relativeLayout.addView(adView, adParams);*/
                    adView.setVisibility(View.VISIBLE);
                }
                else if(sharedPreferences.getString("Adview","false").equals("false"))/*relativeLayout.removeView(adView);*/
                    adView.setVisibility(View.GONE);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(BannerListener);
        // Create the Google API Client with access to Plus and Games
        interstitial = new InterstitialAd(this);
        // Insert the Ad Unit ID
        interstitial.setAdUnitId(getString(R.string.interstitial));


    }
    public void displayInterstitial() {
        // If Ads are loaded, show Interstitial else show nothing.
        
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("3B7F9C99755A03D44BCACE7E91EAD130")
                .build();
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Call displayInterstitial() function
                Log.d("interstitial","add");
                if (interstitial.isLoaded()) {
                    startTimer();
                }
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // TODO Auto-generated method stub
                super.onAdFailedToLoad(errorCode);
                displayInterstitial();

            }
            @Override
            public void onAdClosed() {
                // TODO Auto-generated method stub
                super.onAdClosed();
                displayInterstitial();
            }

        });


    }
    public void startTimer(){
        CountDownTimer count = new CountDownTimer(AD_INTERVAL*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                dispAddAfterTimer();
            }
        };
        count.start();
    }
    public void dispAddAfterTimer(){
        SharedPreferences preferences = getSharedPreferences("AD",0);
        if(preferences.getString("InterstitialAd","false").equals("true")){
            interstitial.show();
        }
        else{
            InterstitialListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if(sharedPreferences.getString("InterstitialAd","false").equals("true")){
                        interstitial.show();
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(InterstitialListener);
                    }
                }
            };
            SharedPreferences sharedPreferences = getSharedPreferences("AD",0);
            sharedPreferences.registerOnSharedPreferenceChangeListener(InterstitialListener);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        if(mGoogleApiClient!=null)
        mGoogleApiClient.connect();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): disconnecting");
        if(mGoogleApiClient!=null)
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    Camera mCamera;
    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new Camera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT);
        EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR,new RatioResolutionPolicy(CAMERA_WIDTH,CAMERA_HEIGHT),mCamera);

        return options;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        c = getApplicationContext();
        sceneManager = new SceneManager(this,mEngine,mCamera);
        sceneManager.loadSplashResources();
        //loadGfx();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }
    @Override
    protected void onResume(){
        super.onResume();
        //onShowLeaderboardsRequested();
    }



    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        /*scene = new Scene();
        scene.setBackground(new Background(255,255,255));

        physicsWorld = new PhysicsWorld(new Vector2(0, 0),false);
        scene.registerUpdateHandler(physicsWorld);
        mEngine.registerUpdateHandler(new FPSLogger());
        createWalls();
        crateRect();
        PauseTimerText = new Text(100, 40, this.TextFont, "", new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        PauseTimerText.setColor(15,50,0);
        scene.attachChild(PauseTimerText);*/
        pOnCreateSceneCallback.onCreateSceneFinished(sceneManager.createSplashScene());
    }



    private void createWalls() {
        /*FixtureDef WALL_FIX = PhysicsFactory.createFixtureDef(0.0f,0.0f,0.0f);
        wall1 = new Rectangle(0,CAMERA_HEIGHT,CAMERA_WIDTH,1,this.mEngine.getVertexBufferObjectManager());
        wall1.setColor(new Color(15,50,0));
        PhysicsFactory.createBoxBody(physicsWorld,wall1, BodyDef.BodyType.StaticBody,WALL_FIX);
        scene.attachChild(wall1);
        //FixtureDef WALL_FIX1 = PhysicsFactory.createFixtureDef(0.0f,0.0f,0.0f);
        wall2 = new Rectangle(-1,0,1,CAMERA_HEIGHT,this.mEngine.getVertexBufferObjectManager());
        wall2.setColor(new Color(15,50,0));
        PhysicsFactory.createBoxBody(physicsWorld,wall2, BodyDef.BodyType.StaticBody,WALL_FIX);
        scene.attachChild(wall2);
//        Rectangle ground2 = new Rectangle(CAMERA_WIDTH,0,1,CAMERA_HEIGHT,this.mEngine.getVertexBufferObjectManager());
//        ground2.setColor(new Color(15,50,0));
//        PhysicsFactory.createBoxBody(physicsWorld,ground2, BodyDef.BodyType.StaticBody,WALL_FIX);
//        scene.attachChild(ground2);
        wall3 = new Rectangle(0,-1,CAMERA_WIDTH,1,this.mEngine.getVertexBufferObjectManager());
        wall3.setColor(new Color(15, 50, 0));
        PhysicsFactory.createBoxBody(physicsWorld, wall3, BodyDef.BodyType.StaticBody, WALL_FIX);
        scene.attachChild(wall3);*/
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        /**/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mGoogleApiClient.connect();
        // load outbox from file
        mOutbox.loadLocal(this);
        sceneManager.loadGameResources();


        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mEngine.registerUpdateHandler(new TimerHandler(1f,new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler pTimerHandler) {
                        mEngine.unregisterUpdateHandler(pTimerHandler);
                        sceneManager.createMenuScene();
                        sceneManager.setCurrentScene(SceneManager.AllScenes.MENU);


                        //sceneManager.loadMenuResources();
                    }
                })
        );
        displayInterstitial();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
    }
    public void onShowAchievementsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
        }
    }
    public void onShowLeaderboardsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
        }
    }
    public static boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }
    class AccomplishmentsOutbox {
        boolean mPrimeAchievement = false;
        boolean mHumbleAchievement = false;
        boolean mLeetAchievement = false;
        boolean mArrogantAchievement = false;
        int mBoredSteps = 0;
        int mEasyModeScore = -1;
        int mHardModeScore = -1;

        boolean isEmpty() {
            return !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
                    !mArrogantAchievement && mBoredSteps == 0 && mEasyModeScore < 0 &&
                    mHardModeScore < 0;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }
    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.SurfaceViewId;
    }
}
