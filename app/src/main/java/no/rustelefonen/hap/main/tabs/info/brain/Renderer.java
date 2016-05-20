package no.rustelefonen.hap.main.tabs.info.brain;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.ScaleAnimation3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import java.util.ArrayList;
import java.util.List;

import no.rustelefonen.hap.R;

/**
 * Created by martinnikolaisorlie on 03/03/16.
 */
public class Renderer extends RajawaliRenderer implements OnObjectPickedListener {
    private Vector3 scaledDown = new Vector3(1, 1, 1);
    private Vector3 scaledUp = new Vector3(1.1, 1.1, 1.1);

    private OnObjectPickedListener pickedListener;
    private List<Object3D> objects, clickableObjects;
    private String[] clickedStrings, braindetailStrings;
    private ObjectColorPicker picker;

    private Object3D selectedObject;

    private OnObjectClickedListener objectClickedListener;

    public Renderer(Context context) {
        super(context);
    }

    public Renderer(Context context, OnObjectClickedListener objectClickedListener) {
        super(context);
        this.objectClickedListener = objectClickedListener;
        pickedListener = this;
    }

    @Override
    protected void initScene() {
        new BrainLoader().execute();
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {}

    @Override
    public void onTouchEvent(MotionEvent event) {}

    public void touchObjectAt(float x, float y) {
        picker.getObjectAt(x, y);
    }

    @Override
    public void onObjectPicked(final Object3D object) {
        if (objectClickedListener == null) return;
        final int brainPartIndex = clickableObjects.indexOf(object);

        if (selectedObject == object) {
            selectedObject = null;
            playScaleAnimation(object, scaledDown);
        } else if (brainPartIndex > -1) {
            selectedObject = object;
            for (Object3D object3D : clickableObjects) {
                if (object3D == selectedObject) continue;
                if (object3D.getScale().equals(scaledDown)) continue;
                playScaleAnimation(object3D, scaledDown);
            }
            playScaleAnimation(selectedObject, scaledUp);
        }

        if (brainPartIndex == -1) return; // avoid out of bounds exception for clickable parts

        //calling listener on GUI thread
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                objectClickedListener.onClick(clickedStrings[brainPartIndex], braindetailStrings[brainPartIndex]);
            }
        });
    }

    private void playScaleAnimation(Object3D object, Vector3 vector3) {
        Animation3D anim = new ScaleAnimation3D(vector3);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDurationMilliseconds(300);
        anim.setTransformable3D(object);
        getCurrentScene().registerAnimation(anim);
        anim.play();
    }

    private Material initMaterial(Texture texture) throws ATexture.TextureException {
        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColorInfluence(0);
        material.addTexture(texture);
        return material;
    }

    private Object3D initObject(int objId, Material material) throws ParsingException {
        LoaderOBJ loader = new LoaderOBJ(mContext.getResources(), getTextureManager(), objId);
        loader.parse();
        Object3D obj = loader.getParsedObject();
        obj.setMaterial(material);
        obj.setDoubleSided(true);
        obj.setScale(1);

        if(objId == R.raw.hippocampus){
            obj.setTransparent(true);
        }

        return obj;
    }

    public void initLightning() {
        DirectionalLight light = new DirectionalLight();
        light.setLookAt(1, -1, 1);
        light.enableLookAt();
        light.setPower(1.5f);
        getCurrentScene().addLight(light);

        light = new DirectionalLight();
        light.setLookAt(-1, 1, -1);
        light.enableLookAt();
        light.setPower(1.5f);
        getCurrentScene().addLight(light);
    }

    public interface OnObjectClickedListener {
        void onClick(String brainTitle, String brainInfo);
    }

    private class BrainLoader extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;
        Handler h;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            h = new Handler(Looper.getMainLooper());
            clickableObjects = new ArrayList<>();
            h.post(new Runnable() {
                public void run() {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage("Hjernen laster inn...");
                    progressDialog.setMax(100);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();
                }
            });
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressDialog.incrementProgressBy(100/8);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            clickedStrings = mContext.getResources().getStringArray(R.array.brain_clicked_strings);
            braindetailStrings = mContext.getResources().getStringArray(R.array.brain_clicked_info);
            objects = new ArrayList<>();
            getCurrentScene().setBackgroundColor(Color.WHITE);
            initLightning();
            picker = new ObjectColorPicker(Renderer.this);
            picker.setOnObjectPickedListener(pickedListener);

            TypedArray rawObjects = mContext.getResources().obtainTypedArray(R.array.objects_3d);

            Texture frontBrainTexture = new Texture("unique_mtl_frontSide", R.drawable.transparant_front_side_brain);
            Texture hippocampusTexture = new Texture("unique_mtl_hippocampusOutline", R.drawable.transparent_hippocampus);
            Texture backBrainTexture = new Texture("unique_mtl_backBrainTexture", R.drawable.transparent_back_side_brain);
            try {
                for (int i = 0; i < rawObjects.length(); i++) {
                    Material material;
                    if (i == 7) material = initMaterial(hippocampusTexture);
                    else if (i == 3) material = initMaterial(backBrainTexture);
                    else material = initMaterial(frontBrainTexture);

                    Object3D obj = initObject(rawObjects.getResourceId(i, 0), material);

                    getCurrentScene().addChild(obj);
                    picker.registerObject(obj);
                    objects.add(obj);
                    publishProgress();
                }
            } catch (ParsingException | ATexture.TextureException e) {
                e.printStackTrace();
            }
            rawObjects.recycle();
            for(int i = 4; i<objects.size(); i++){
                clickableObjects.add(objects.get(i));
            }
            return true;
        }
    }
}