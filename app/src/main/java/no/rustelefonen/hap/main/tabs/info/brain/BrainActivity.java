package no.rustelefonen.hap.main.tabs.info.brain;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.surface.RajawaliSurfaceView;
import org.rajawali3d.util.Capabilities;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.rustelefonen.hap.R;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

public class BrainActivity extends OrmLiteActivity implements View.OnTouchListener {
    private Renderer brainRenderer;
    @BindView(R.id.rajwali_surface) RajawaliSurfaceView surface;
    @BindView(R.id.braintext) TextView brainTitleText;
    @BindView(R.id.brain_info_text) TextView brainInfoText;
    @BindView(R.id.brain_overlay) View brainOverlay;
    private PointF lastTouchPoint;
    private int glesVersion;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initGlesVersion();
        initGLESVersionOverride();
        setContentView(R.layout.brain_activity);
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        initToolbar();

        if(glesVersion < 0x20000){
            Toast.makeText(this, "3D-Hjernen støttes dessverre ikke på din telefon", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        brainRenderer = new Renderer(this, new Renderer.OnObjectClickedListener() {
            @Override public void onClick(final String brainTitle, final String brainInfo) {
                brainTitleText.setText(brainTitle);
                brainInfoText.setText(brainInfo);

            }
        });
        surface.setSurfaceRenderer(brainRenderer);
        surface.setOnTouchListener(this);

        ArcballCamera arcball = new ArcballCamera(this, brainOverlay);
        arcball.setPosition(0.34, 0.25, 6);
        arcball.setRotation(0, 0, 0);
        brainRenderer.getCurrentScene().replaceAndSwitchCamera(brainRenderer.getCurrentCamera(), arcball);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        brainOverlay.dispatchTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchPoint = new PointF(event.getRawX(), event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
                float movedX = Math.abs(event.getRawX() - lastTouchPoint.x);
                float movedY = Math.abs(event.getRawY() - lastTouchPoint.y);
                if (movedX < 40 && movedY < 40)
                    brainRenderer.touchObjectAt(event.getX(), event.getY());
                break;

        }
        return true;
    }

    private void initGlesVersion(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        glesVersion = configurationInfo.reqGlEsVersion;
    }

    private void initGLESVersionOverride() {
        int rajawaliThinks = Capabilities.getGLESMajorVersion();
        if(glesVersion >= 0x30000 || rajawaliThinks < 3) return; //we only take action if rajawali thinks supported gles version is more than 3, when it really is less than 3

        try {
            Field mGLESMajorVersion = Capabilities.class.getDeclaredField("mGLESMajorVersion");
            mGLESMajorVersion.setAccessible(true);
            mGLESMajorVersion.setInt(null, 2);
            mGLESMajorVersion.setAccessible(false);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }
    }
}