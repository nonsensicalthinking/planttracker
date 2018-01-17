/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nonsense.planttracker.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlantCam extends AppCompatActivity {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final int MAX_IMAGES = 2;

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Size cameraPreviewSize;

    private CameraManager cameraManager;
    private CameraDevice curCameraDevice;
    private CameraCaptureSession curCameraCaptureSession;

    private Handler backgroundHandler;
    private Semaphore cameraLock = new Semaphore(1);;
    private String cameraId;

    private CaptureRequest.Builder cameraPreviewRequestBuilder;
    private ImageReader imageReader;
    private CaptureRequest curPreviewRequest;
    private boolean isFlashSupported;
    private TextureView cameraPreviewTextureView;
    private int cameraState = STATE_PREVIEW;
    private int sensorOrientation;
    private HandlerThread backgroundThread;

    private CameraCharacteristics characteristics;

    private ArrayList<String> fileNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_cam);


        bindUi();

        startBackgroundThread();

        if (cameraPreviewTextureView.isAvailable()) {
            openCamera(cameraPreviewTextureView.getWidth(), cameraPreviewTextureView.getHeight());
        } else {
            cameraPreviewTextureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (cameraPreviewTextureView.isAvailable()) {
            openCamera(cameraPreviewTextureView.getWidth(), cameraPreviewTextureView.getHeight());
        } else {
            cameraPreviewTextureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onBackPressed()    {
        launchImageChooser();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setMessage("BLAH!")
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        cancelActivity();
                                    }
                                }).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        switch(requestCode) {

            case AndroidConstants.ACTIVITY_IMAGE_CHOOSER:
                if (resultCode == RESULT_OK)    {
                    try {
                        ArrayList<String> selectedFiles = (ArrayList<String>)returnedIntent.
                                getSerializableExtra("selectedFiles");

                        Intent retIntent = new Intent();
                        retIntent.putExtra("selectedFiles", selectedFiles);

                        setResult(RESULT_OK, retIntent);
                        finish();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    cancelActivity();
                }
                else    {
                    cancelActivity();
                }
                break;
        }
    }

    private void bindUi()   {
        cameraPreviewTextureView = (TextureView) findViewById(R.id.cameraPreviewTextureView);
        cameraPreviewTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener()
        {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,
                                                  int height) {
                openCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width,
                                                    int height) {
                configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

        cameraPreviewTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureStillPicture();
            }
        });
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                cancelActivity();
                                }
                            }).show();

        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void openCamera(int width, int height)   {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        setupCameraOutputs(width, height);
        configureTransform(width, height);

        try {
            if (!cameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS))    {
                throw new TimeoutException("Unable to acquire camera lock");
            }

            cameraManager.openCamera(cameraId, callback, backgroundHandler);
        }
        catch(Exception e)  {
            e.printStackTrace();
        }

    }

    private void closeCamera() {
        try {
            cameraLock.acquire();
            if (null != curCameraCaptureSession) {
                curCameraCaptureSession.close();
                curCameraCaptureSession = null;
            }
            if (null != curCameraDevice) {
                curCameraDevice.close();
                curCameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraLock.release();
        }
    }

    private void setupCameraOutputs(int width, int height)  {
        try {
            for(String cId : cameraManager.getCameraIdList())   {
                characteristics = cameraManager.getCameraCharacteristics(cId);

                Integer facingDirection = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facingDirection != null &&
                        facingDirection == CameraCharacteristics.LENS_FACING_FRONT)  {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null)    {
                    continue;
                }

                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size size, Size t1) {
                                return Long.signum((long) size.getWidth() * size.getHeight() -
                                        (long) t1.getWidth() * t1.getHeight());
                            }
                        });

                imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, MAX_IMAGES);

                imageReader.setOnImageAvailableListener(onImageAvailableListener,
                        backgroundHandler);

                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                boolean swappedDimensions = false;
                switch (displayRotation)    {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270)    {
                            swappedDimensions = true;
                        }
                        break;

                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                }

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions)  {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH)    {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT)  {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                cameraPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                isFlashSupported = available == null ? false : available;

                cameraId = cId;
                return;
            }
        }
        catch(Exception e)  {
            e.printStackTrace();
        }
    }

    private void configureTransform(int width, int height)  {
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, width, height);
        RectF bufferRect = new RectF(0, 0, cameraPreviewSize.getWidth(),
                cameraPreviewSize.getHeight());

        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if ( rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)   {
            bufferRect.offset(centerX - bufferRect.centerX(),
                    centerY - bufferRect.centerY());

            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float scale = Math.max((float) height / cameraPreviewSize.getHeight(),
                    (float) width / cameraPreviewSize.getWidth());

            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        else if (rotation == Surface.ROTATION_180)  {
            matrix.postRotate(180, centerX, centerY);
        }
        cameraPreviewTextureView.setTransform(matrix);
    }

    private void createCameraPreviewSession()   {
        try {
            SurfaceTexture surfaceTexture = cameraPreviewTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(cameraPreviewSize.getWidth(),
                    cameraPreviewSize.getHeight());

            Surface surface = new Surface(surfaceTexture);

            cameraPreviewRequestBuilder = curCameraDevice.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW);

            cameraPreviewRequestBuilder.addTarget(surface);

            curCameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (curCameraDevice == null)    {
                                return;
                            }

                            curCameraCaptureSession = cameraCaptureSession;

                            try {
                                cameraPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                setAutoFlash(cameraPreviewRequestBuilder);

                                curPreviewRequest = cameraPreviewRequestBuilder.build();
                                curCameraCaptureSession.setRepeatingRequest(curPreviewRequest,
                                        cameraCaptureCallback, backgroundHandler);
                            }
                            catch(Exception e)  {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(PlantCam.this, "Camera Capture Failed",
                                    Toast.LENGTH_LONG);
                        }
                    }, null);

        }
        catch(Exception e)  {
            e.printStackTrace();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (isFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Size chooseOptimalSize(Size[] choices, int textureViewWidth,
          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    // We cast here to ensure the multiplications won't overflow
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                            (long) rhs.getWidth() * rhs.getHeight());
                }

            });
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    // We cast here to ensure the multiplications won't overflow
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                            (long) rhs.getWidth() * rhs.getHeight());
                }

            });
        } else {
            Toast.makeText(PlantCam.this, "Couldn't find any suitable preview size",
                    Toast.LENGTH_LONG).show();

            return choices[0];
        }
    }

    private void captureStillPicture() {
        try {
            final CaptureRequest.Builder captureBuilder =
                    curCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

//            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback captureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {

                    Toast.makeText(PlantCam.this, "Image Saved",
                            Toast.LENGTH_SHORT).show();

                    unlockFocus();
                }
            };

            curCameraCaptureSession.stopRepeating();
            curCameraCaptureSession.abortCaptures();
            curCameraCaptureSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            cameraPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(cameraPreviewRequestBuilder);
            curCameraCaptureSession.capture(cameraPreviewRequestBuilder.build(), cameraCaptureCallback,
                    backgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            cameraState = STATE_PREVIEW;
            curCameraCaptureSession.setRepeatingRequest(curPreviewRequest, cameraCaptureCallback,
                    backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            cameraPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            cameraState = STATE_WAITING_PRECAPTURE;
            curCameraCaptureSession.capture(cameraPreviewRequestBuilder.build(), cameraCaptureCallback,
                    backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int deviceOrientation) {
        return (ORIENTATIONS.get(deviceOrientation) + sensorOrientation + 270) % 360;
    }

    private void startBackgroundThread() {
        try {
            backgroundThread = new HandlerThread("CameraBackground");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopBackgroundThread() {
        try {
            backgroundThread.quitSafely();
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchImageChooser()   {
        cleanUpActivity();
        Intent imgPick = new Intent(this, CameraImagePicker.class);
        imgPick.putExtra("files", fileNames);
        startActivityForResult(imgPick, AndroidConstants.ACTIVITY_IMAGE_CHOOSER);
    }

    private void cleanUpActivity()  {
        stopBackgroundThread();
    }

    private void cancelActivity()   {
        cleanUpActivity();

        setResult(RESULT_CANCELED);
        finish();
    }

    private File getFileHandle()    {
        String imageName = System.currentTimeMillis() + ".jpg";

        fileNames.add(imageName);

        return new File(getExternalFilesDir("camera/"), imageName);
    }

    final CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            cameraLock.release();
            curCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraLock.release();
            cameraDevice.close();
            curCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraLock.release();
            cameraDevice.close();
            curCameraDevice = null;
            cancelActivity();
        }
    };

    private CameraCaptureSession.CaptureCallback cameraCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (cameraState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            cameraState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        cameraState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        cameraState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    private final ImageReader.OnImageAvailableListener onImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), getFileHandle()));
        }

    };

    private final TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

}
