package com.example.dev_lautreamont.airhockey;

import android.content.Context;
import android.media.SoundPool;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import com.example.dev_lautreamont.airhockey.objects.Board;
import com.example.dev_lautreamont.airhockey.objects.Mallet;
import com.example.dev_lautreamont.airhockey.objects.Puck;
import com.example.dev_lautreamont.airhockey.objects.Skybox;
import com.example.dev_lautreamont.airhockey.objects.Table;
import com.example.dev_lautreamont.airhockey.programs.ColorShaderProgram;
import com.example.dev_lautreamont.airhockey.programs.ObjectShaderProgram;
import com.example.dev_lautreamont.airhockey.programs.SkyboxShaderProgram;
import com.example.dev_lautreamont.airhockey.programs.TextureShaderProgram;
import com.example.dev_lautreamont.airhockey.util.Geometry.Plane;
import com.example.dev_lautreamont.airhockey.util.Geometry.Point;
import com.example.dev_lautreamont.airhockey.util.Geometry.Ray;
import com.example.dev_lautreamont.airhockey.util.Geometry.Sphere;
import com.example.dev_lautreamont.airhockey.util.Geometry.Vector;
import com.example.dev_lautreamont.airhockey.util.LoggerConfig;
import com.example.dev_lautreamont.airhockey.util.MatrixHelper;
import com.example.dev_lautreamont.airhockey.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.opengl.GLES20.GL_ALWAYS;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_EQUAL;
import static android.opengl.GLES20.GL_FALSE;
import static android.opengl.GLES20.GL_KEEP;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_REPLACE;
import static android.opengl.GLES20.GL_STENCIL_BUFFER_BIT;
import static android.opengl.GLES20.GL_STENCIL_TEST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glColorMask;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glStencilFunc;
import static android.opengl.GLES20.glStencilOp;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;
import static com.example.dev_lautreamont.airhockey.util.Constants.BOARD_HEIGHT;
import static com.example.dev_lautreamont.airhockey.util.Constants.BOARD_INSIDE_AREA_LENGTH;
import static com.example.dev_lautreamont.airhockey.util.Constants.BOARD_INSIDE_AREA_WIDTH;
import static com.example.dev_lautreamont.airhockey.util.Constants.BOARD_THICKNESS;
import static com.example.dev_lautreamont.airhockey.util.Constants.LIGHT_POSITION;
import static com.example.dev_lautreamont.airhockey.util.Constants.TABLE_CENTER_LINE;
import static com.example.dev_lautreamont.airhockey.util.Constants.TABLE_FAR_BOUND;
import static com.example.dev_lautreamont.airhockey.util.Constants.TABLE_LEFT_BOUND;
import static com.example.dev_lautreamont.airhockey.util.Constants.TABLE_NEAR_BOUND;
import static com.example.dev_lautreamont.airhockey.util.Constants.TABLE_RIGHT_BOUND;
import static com.example.dev_lautreamont.airhockey.util.Geometry.angleBetween;
import static com.example.dev_lautreamont.airhockey.util.Geometry.intersectionPoint;
import static com.example.dev_lautreamont.airhockey.util.Geometry.intersects;
import static com.example.dev_lautreamont.airhockey.util.Geometry.vectorBetween;

/**
 * AirHockey
 * com.example.dev_lautreamont.airhockey
 * Created by Leblanc, Frédéric on 2015-03-09, 22:07.
 * <p/>
 * Cette classe
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer{

    private static final String TAG = "AirHockeyRenderer";

    private final Context context;

    // Matrices
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];
    private final float[] viewMatrixForSkybox = new float[16];
    private final float[] tempMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] it_modelViewMatrix = new float[16];

    // Skybox
    private Skybox skybox;

    // Table
    private Table table;

    // Board
    private Board board;

    // Mallets
    private Mallet mallet;
    private boolean userMalletPressed = false;
    private boolean CPUMalletPressed = false;
    private Point userMalletPosition;
    private Point CPUMalletPosition;
    private Point previousUserMalletPosition;
    private boolean isCollisionOn = true;

    // Puck
    private Puck puck;
    private Point puckPosition;
    private Vector puckVector;

    // Colors & textures
    private int textureTable;
    private int textureSkybox;

    // Programs
    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;
    private ObjectShaderProgram objectProgram;
    private SkyboxShaderProgram skyboxProgram;

    // Sounds
    private int soundIDBoardCollision;
    private int soundIDMalletCollision;
    SoundPool soundPool;

    private float[] eyePos = {0f, 1f, 1.8f};
    byte framesAfterCollision = 0;
    private long frameStartTimeMs;
    private long startTimeMs;
    private int frameCount;

    public AirHockeyRenderer(Context context) {
        this.context = context;
    }

    /**
     * Called when the surface is created or recreated.
     * <p/>
     * Called when the rendering thread
     * starts and whenever the EGL context is lost. The EGL context will typically
     * be lost when the Android device awakes after going to sleep.
     * <p/>
     * Since this method is called at the beginning of rendering, as well as
     * every time the EGL context is lost, this method is a convenient place to put
     * code to create resources that need to be created when the rendering
     * starts, and that need to be recreated when the EGL context is lost.
     * Textures are an example of a resource that you might want to create
     * here.
     * <p/>
     * Note that when the EGL context is lost, all OpenGL resources associated
     * with that context will be automatically deleted. You do not need to call
     * the corresponding "glDelete" methods such as glDeleteTextures to
     * manually delete these lost resources.
     * <p/>
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param config the EGLConfig of the created surface. Can be used
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_CULL_FACE);

        skyboxProgram = new SkyboxShaderProgram(context);
        skybox = new Skybox();

        textureSkybox = TextureHelper.loadCubeMap(context,
                new int[] {
                        R.drawable.left,
                        R.drawable.right,
                        R.drawable.bottom,
                        R.drawable.top,
                        R.drawable.front,
                        R.drawable.back});

        table = new Table();
        board = new Board(BOARD_INSIDE_AREA_WIDTH, BOARD_INSIDE_AREA_LENGTH, BOARD_THICKNESS, BOARD_HEIGHT);
        mallet = new Mallet(0.10f, 0.08f, 64);
        puck = new Puck(0.06f, 0.018f, 64);

        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);
        objectProgram = new ObjectShaderProgram(context);

        textureTable = TextureHelper.loadTexture(context, R.drawable.airhockey_table);

        //TODO: SoundPool.builder API 21 et hashmap pour les id's.
        soundPool = new SoundPool(3, STREAM_MUSIC, 0);
        soundIDBoardCollision = soundPool.load(context, R.raw.plastic_hit_board, 1);
        soundIDMalletCollision = soundPool.load(context, R.raw.plastic_hit_mallet, 1);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.e(TAG, "Son charcgé : " + sampleId + " : " + status);
            }
        });

        resetPositons();
}

    /**
     * Called when the surface changed size.
     * <p/>
     * Called after the surface is created and whenever
     * the OpenGL ES surface size changes.
     * <p/>
     * Typically you will set your viewport here. If your camera
     * is fixed then you could also set your projection matrix here:
     * <pre class="prettyprint">
     * void onSurfaceChanged(GL10 gl, int insideAreaWidth, int height) {
     * gl.glViewport(0, 0, insideAreaWidth, height);
     * // for a fixed camera, set the projection too
     * float ratio = (float) insideAreaWidth / height;
     * gl.glMatrixMode(GL10.GL_PROJECTION);
     * gl.glLoadIdentity();
     * gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
     * }
     * </pre>
     *
     * @param gl     the GL interface. Use <code>instanceof</code> to
     *               test if the interface supports GL11 or higher interfaces.
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);
        setLookAtM(viewMatrix, 0, 0f, 1f, 1.8f, 0f, 0f, 0f, 0f, 1f, 0f);
        updateViewMatrices();
        Log.e(TAG, "Ready!!! : " + Math.abs(TABLE_FAR_BOUND));
    }

    /**
     * Called to draw the current frame.
     * <p/>
     * This method is responsible for drawing the current frame.
     * <p/>
     * The implementation of this method typically looks like this:
     * <pre class="prettyprint">
     * void onDrawFrame(GL10 gl) {
     * gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
     * //... other gl calls to render the scene ...
     * }
     * </pre>
     *
     * @param gl the GL interface. Use <code>instanceof</code> to
     *           test if the interface supports GL11 or higher interfaces.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear the rendering surface.
        limitFrameRate(40);
        //logFrameRate();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        puckPosition = puckPosition.translate(puckVector);

        float distanceUserMallet = vectorBetween(userMalletPosition, puckPosition).length();
        float distanceCPUMallet = vectorBetween(CPUMalletPosition, puckPosition).length();

        framesAfterCollision++;
        if ((!isCollisionOn) && (framesAfterCollision > 3)) {
            isCollisionOn = true;
            framesAfterCollision = 0;
        }

        //TODO: Finish the logic to see if mallet move or not.
        if ((distanceUserMallet <= (puck.radius + mallet.radius)) && (puckVector.length() > 0.0f) && !userMalletPressed) {
            // The puck has struck one mallet
            collisionPuckMallet(userMalletPosition, vectorBetween(userMalletPosition, puckPosition));
        } else if ((distanceCPUMallet <= (puck.radius + mallet.radius)) && (puckVector.length() > 0.0f) && !CPUMalletPressed) {
            // The puck has struck one mallet.
            collisionPuckMallet(CPUMalletPosition, vectorBetween(CPUMalletPosition, puckPosition));
        }

        if (puckPosition.x < TABLE_LEFT_BOUND + puck.radius || puckPosition.x > TABLE_RIGHT_BOUND - puck.radius) {
            puckVector = new Vector(-puckVector.x, puckVector.y, puckVector.z);
            puckVector = puckVector.scale(0.9f);
            playSoundCollision(soundIDBoardCollision, puckVector.length());
        }
        if (puckPosition.z < TABLE_FAR_BOUND + puck.radius || puckPosition.z > TABLE_NEAR_BOUND - puck.radius) {
            puckVector = new Vector(puckVector.x, puckVector.y, -puckVector.z);
            puckVector = puckVector.scale(0.9f);
            playSoundCollision(soundIDBoardCollision, puckVector.length());
        }
        // Clamp the puck position.
        puckPosition = new Point(
                clamp(puckPosition.x, TABLE_LEFT_BOUND + puck.radius, TABLE_RIGHT_BOUND - puck.radius),
                puckPosition.y,
                clamp(puckPosition.z, TABLE_FAR_BOUND + puck.radius, TABLE_NEAR_BOUND - puck.radius));
        puckVector = puckVector.scale(0.99f);

        if ((puckVector.length() < 0.0001f) && (!userMalletPressed)) {
            resetPositons();
        }

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        drawSkybox();

        /*glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glStencilFunc(GL_ALWAYS, 0xff, 0xff);

        //glDisable(GL_TEXTURE_2D);
        glDepthMask(false);
        glColorMask(false, false, false, false);
        drawTable();
        glColorMask(true, true, true, true);
        glDepthMask(true);
        //glEnable(GL_TEXTURE_2D);

        glDisable(GL_STENCIL_TEST);
        glClear(GL_DEPTH_BUFFER_BIT);*/

        drawTable();
        drawBoard();

        updateMvpMatrix();

        // Draw the puck.
        drawPuck(puckPosition.x, puckPosition.y, puckPosition.z);
        //drawPuck(puckPosition.x, -puck.height, puckPosition.z);
        // Draw the mallets.
        drawMallet(CPUMalletPosition.x, CPUMalletPosition.y, CPUMalletPosition.z, true);
        drawMallet(userMalletPosition.x, userMalletPosition.y, userMalletPosition.z, false);

    }

    public void handleTouchPress(float normalizedX, float normalizedY) {
        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        //Sphere malletBoundingSphere = new Sphere(new Point(userMalletPosition.x, userMalletPosition.y, userMalletPosition.z), mallet.height / 2f);
        Sphere malletBoundingSphere = new Sphere(new Point(userMalletPosition.x, userMalletPosition.y, userMalletPosition.z), mallet.height);

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set userMalletPressed =
        // true.
        userMalletPressed = intersects(malletBoundingSphere, ray);
        Log.w(TAG, "Touché : " + userMalletPressed);
    }

    public void handleTouchDrag(float normalizedX, float normalizedY) {
        if (userMalletPressed) {
            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            // Define a plane representing our air hockey table.
            Plane plane = new Plane(new Point(0, 0, 0), new Vector(0, 1, 0));
            // Find out where the touched center intersects the plane
            // representing our table.  We'll move the mallet along this plane.
            Point touchedPoint = intersectionPoint(ray, plane);
            previousUserMalletPosition = userMalletPosition;
            userMalletPosition = new Point(
                    clamp(touchedPoint.x, TABLE_LEFT_BOUND + mallet.radius, TABLE_RIGHT_BOUND - mallet.radius),
                    mallet.height / 2f,
                    clamp(touchedPoint.z, TABLE_CENTER_LINE + mallet.radius, TABLE_NEAR_BOUND - mallet.radius));

            float distance = vectorBetween(userMalletPosition, puckPosition).length();

            //float angle = angleBetween(puckVector, vectorBetween(puckPosition, userMalletPosition));
            //Log.e(TAG, "Angle User: " + angle);

            if (distance <= (puck.radius + mallet.radius) && isCollisionOn) {
                // The mallet has struck the puck.  Now send the puck flying
                // based on the mallet velocity.
                //puckVector = vectorBetween(previousUserMalletPosition, userMalletPosition);
                collisionPuckMallet(userMalletPosition, vectorBetween(previousUserMalletPosition, userMalletPosition));
                isCollisionOn = false;
                framesAfterCollision = 0;
                //playSoundCollision(soundIDMalletCollision, puckVector.length());
            }
        }
    }

    public void handleTouchRelease() {
        userMalletPressed = false;
    }

    private Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};

        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        Point nearPointRay = new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Point farPointRay = new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

        return new Ray(nearPointRay, vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    private float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }

    private void resetPositons() {
        //userMalletPosition = new Point(0f, mallet.height / 2f, 0.3f);
        userMalletPosition = new Point(0f, mallet.height / 2f, 0.3f);
        CPUMalletPosition = new Point(0f, mallet.height / 2f, -0.7f);

        puckPosition = new Point(0f, puck.height / 2f, -0.2f);
        puckVector = new Vector(0f, 0f, 0f);
    }

    private void drawSkybox() {
        setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();
        glDepthFunc(GL_LEQUAL);
        skyboxProgram.useProgram();
        skyboxProgram.setUniforms(modelViewProjectionMatrix, textureSkybox);
        skybox.bindData(skyboxProgram);
        skybox.draw();
        glDepthFunc(GL_LESS);
    }

    private void drawTable() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0);
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, textureTable);
        table.bindData(textureProgram);
        table.draw();
    }

    private void drawBoard() {
        setIdentityM(modelMatrix, 0);
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

        objectProgram.useProgram();
        objectProgram.setUniforms(modelViewProjectionMatrix, modelViewMatrix, LIGHT_POSITION, eyePos, 0.999f, 0.999f, 0.999f, 1f, 10f, 25f);
        board.bindData(objectProgram);
        board.draw();
    }

    private void drawPuck(float x, float y, float z) {
        positionObjectInScene(x, y, z);
        objectProgram.setUniforms(modelViewProjectionMatrix, modelViewMatrix, LIGHT_POSITION, eyePos, 0.25f, 0.25f, 0.25f, 1f, 5f, 10f);
        puck.bindData(objectProgram);
        puck.draw();
    }

    private void drawMallet(float x, float y, float z, boolean isCPUMallet) {
        positionObjectInScene(x, y, z);
        objectProgram.useProgram();
        if (isCPUMallet) {
            objectProgram.setUniforms(modelViewProjectionMatrix, modelViewMatrix, LIGHT_POSITION, eyePos, 0.1f, 0.1f, 1f, 1f, 10f, 25f);
        } else {
            objectProgram.setUniforms(modelViewProjectionMatrix, modelViewMatrix, LIGHT_POSITION, eyePos, 1f, 0.1f, 0.1f, 1f, 10f, 25f);
        }
        mallet.bindData(objectProgram);
        mallet.draw();
    }

    private void updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }

/*    private void updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        invertM(tempMatrix, 0, modelViewMatrix, 0);
        transposeM(it_modelViewMatrix, 0, tempMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }*/

    private void updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    private void updateViewMatrices() {
        Log.w(TAG, "Update view matrices");
        setIdentityM(viewMatrix, 0);
        //rotateM(viewMatrix, 0, 1.2f, 1f, 0f, 0f);
        //rotateM(viewMatrix, 0, 2.2f, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);

        // We want the translation to apply to the regular view matrix, and not
        // the skybox.
        //setIdentityM(viewMatrix, 0);
        //translateM(viewMatrix, 0, 0f, -1.5f, -4.5f);
        //translateM(viewMatrix, 0, 0f, -0.5f, -3f);
        setLookAtM(viewMatrix, 0, 0f, 1f, 1.8f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    private void collisionPuckMallet(Point malletPosition, Vector malletVector) {
        Vector malletPuckVector = vectorBetween(malletPosition, puckPosition);
        //puckMalletVector.adjustOnYPlane(0f);
        //Log.e(TAG, "Puck position before collision : " + puckPosition.x + " : " + puckPosition.y + " : " + puckPosition.z);
        puckPosition = malletPosition.translate(malletPuckVector);
        //puckVector = malletPuckVector;
        puckVector = vectorBetween(malletPosition, puckPosition);
        puckVector = puckVector.add(malletVector);
        puckVector = puckVector.adjustOnYPlane(0f);

        //Log.e(TAG, "Puck position after collision : " + puckPosition.x + " : " + puckPosition.y + " : " + puckPosition.z);

        //float angle = angleBetween(puckVector, vectorBetween(puckPosition, malletPosition));
        //Log.e(TAG, "PI / 2 = " + Math.PI / 2 + "Angle : " + angle + " Sinus de l'angle " + Math.sin(angle));

        //puckVector = puckVector.rotateOnYPlane((float) (Math.PI / 2) - angle);
        //puckVector = puckVector.add(puckMalletVector);
        //puckVector = puckVector.adjustOnYPlane(0f);
        //puckVector = puckVector.scale((float) (puckVector.length() * Math.sin(angle)));
        //puckVector = puckVector.scale(0.25f);
        //Log.e(TAG, " Vector : " + malletVector.length() + " : " + puckVector.length());
        puckVector = puckVector.scale(0.25f);
        //Log.e(TAG, " Vector : " + puckVector.length());
        playSoundCollision(soundIDMalletCollision, puckVector.length());
    }

    private void playSoundCollision(int soundID, float volume) {
        float volumeAdjustment = volume * 100;
        if (volumeAdjustment > 1.0) {
            volumeAdjustment = 1.0f;
        } else if (volumeAdjustment < 0.5) {
            volumeAdjustment = 0.5f;
        }
        soundPool.play(soundID, volumeAdjustment, volumeAdjustment, 1, 0, 1.0f);
        //Log.e(TAG, "Son actif : " + soundID);
    }

    private void limitFrameRate(int framesPerSecond) {
        long elapsedFrameTimeMs = SystemClock.elapsedRealtime() - frameStartTimeMs;
        long expectedFrameTimeMs = 1000 / framesPerSecond;
        long timeToSleepMs = expectedFrameTimeMs - elapsedFrameTimeMs;

        if (timeToSleepMs > 0) {
            SystemClock.sleep(timeToSleepMs);
        }
        frameStartTimeMs = SystemClock.elapsedRealtime();
    }

    private void logFrameRate() {
            long elapsedRealtimeMs = SystemClock.elapsedRealtime();
            double elapsedSeconds = (elapsedRealtimeMs - startTimeMs) / 1000.0;

            if (elapsedSeconds >= 1.0) {
                Log.e(TAG, frameCount / elapsedSeconds + "fps");
                startTimeMs = SystemClock.elapsedRealtime();
                frameCount = 0;
            }
            frameCount++;
    }
}
