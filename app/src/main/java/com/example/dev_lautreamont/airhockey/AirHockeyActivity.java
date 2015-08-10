package com.example.dev_lautreamont.airhockey;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


public class AirHockeyActivity extends ActionBarActivity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    private final String TAG = "AirHockeyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_hockey);
        glSurfaceView = new GLSurfaceView(this);
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        final AirHockeyRenderer airHockeyRenderer = new AirHockeyRenderer(this);

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);
            // Choose config.
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            // Assign our renderer.
            glSurfaceView.setRenderer(airHockeyRenderer);
            rendererSet = true;
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
            return;
        }

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    // Convert touch coordinates into normalized device
                    // coordiantes, keeping in mind that Android's Y
                    // coordinates are inverted.
                    final float normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
                    final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRenderer.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRenderer.handleTouchDrag(normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRenderer.handleTouchRelease();
                            }
                        });
                    }

                    //Log.e(TAG, "Action : " + event.getPointerCount());


                    return true;
                } else {
                    return false;
                }
            }

        });

        setContentView(glSurfaceView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_air_hockey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }


}
