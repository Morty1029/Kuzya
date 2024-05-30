package com.example.smartthings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraService extends Service {
    private static final String TAG = "CameraService";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private CameraManager cameraManager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Timer timer;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private SurfaceView surfaceView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private ImageReader imageReader;
    private Image mPendingImage;
    private File tempImageFile;
    private boolean isCapturing = false;
    private Handler mMainHandler;
    private Timer mTimer;
    Image mCurrentImage;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startBackgroundThread();
        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        startCamera();
        initImageReader();

        surfaceView = new SurfaceView(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) { // делаем возможность двигать window с камерой по всему экрану
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastAction = MotionEvent.ACTION_DOWN;
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            return true;
                        }
                        lastAction = MotionEvent.ACTION_UP;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        lastAction = MotionEvent.ACTION_MOVE;

                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);

                        layoutParams.x = initialX + deltaX;
                        layoutParams.y = initialY + deltaY;

                        windowManager.updateViewLayout(surfaceView, layoutParams);
                        return true;
                }
                return false;
            }
        });

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);



        int windowWidth = 400;
        int windowHeight = 400;

        layoutParams = new WindowManager.LayoutParams(
                windowWidth,
                windowHeight,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenH = displayMetrics.heightPixels;

        layoutParams.x = screenWidth - windowWidth;
        layoutParams.y = screenH; // Начальные координаты для верхнего правого угла

        layoutParams.packageName = getPackageName(); // проверка, что окно остается в пределах приложения

        windowManager.addView(surfaceView, layoutParams);

        mMainHandler = new Handler(Looper.getMainLooper());
        startTimer();
        return START_STICKY;
    }

    private void initImageReader() {
        int imageWidth = 1920;
        int imageHeight = 1080;
        int maxImages = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        imageReader = ImageReader.newInstance(imageWidth, imageHeight, ImageFormat.JPEG, maxImages);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                // Закрываем текущее изображение перед получением нового
                if (mCurrentImage != null) {
                    mCurrentImage.close();
                }

                Image image = reader.acquireLatestImage();
                if (image != null) {
                    mCurrentImage = image;
                    //saveImageToFile(image);
                    // После сохранения изображения, вы можете отправить его на сервер
                    // sendImageToServer(image);
                }
            }
        }, backgroundHandler);
    }

    @Override
    public void onDestroy() {
        stopCapturing();
        closeCamera();
        stopBackgroundThread();

        if (windowManager != null && surfaceView != null) {
            windowManager.removeView(surfaceView);
        }

        super.onDestroy();
    }

    private void startCamera() {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Нет разрешения на использование камеры.", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        openCamera();
    }

    private void openCamera() {
        if (cameraId != null) {
            try {
                closeCamera();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraManager.openCamera(cameraId, stateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void startCapturing() {
        if (cameraDevice != null) {
            isCapturing = true;
        }
    }

    private void stopCapturing() {
        isCapturing = false;
        stopTimer();
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    // Выполняем операцию в фоновом потоке с использованием backgroundHandler
                    backgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            takePicture();
                        }
                    });
                }
            }, 0, 1000); // снимок происходящего каждую секунду
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void takePicture() {
        if (cameraDevice == null || surfaceView == null) {
            return;
        }

        if (mCurrentImage != null) {
            try {
                File imageFile = createImageFile();
                ByteBuffer buffer = mCurrentImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                // Запишите данные изображения в файл
                try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(surfaceView.getHolder().getSurface());

                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                cameraCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);

                        // Здесь отправьте изображение на сервер
                        if (mCurrentImage != null)
                            sendImageToServer(imageFile);

                        // Закройте изображение после отправки
                        mCurrentImage.close();
                        mCurrentImage = null;
                    }
                }, backgroundHandler);
            } catch (CameraAccessException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH__mm__ss").format(new Date());
        String imageFileName =  timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return imageFile;
    }
   /* private void saveImageToFile(Image image) {
        // Создайте уникальное имя файла на основе времени
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Получите каталог, в котором будет сохранено изображение
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Создайте файл
        File imageFile = new File(
                storageDir,
                imageFileName + ".jpg"
        );

        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    private void sendImageToServer(File imageFile) {

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(), RequestBody.create(imageFile, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.0.107:5555/upload") // Замените на URL вашего сервера
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to send image: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Обработка успешного ответа от сервера
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Server response: " + responseBody);
                }
                response.close();
            }
        });
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        Log.d(TAG, "Background thread started.");
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
            startCapturing();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            Surface surface = imageReader.getSurface();

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface, surfaceHolder.getSurface()), captureSessionCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (cameraDevice == null) {
                return;
            }

            cameraCaptureSession = session;

            try {
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };
}
