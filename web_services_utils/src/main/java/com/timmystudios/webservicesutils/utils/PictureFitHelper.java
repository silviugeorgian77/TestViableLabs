package com.timmystudios.webservicesutils.utils;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.View;
import android.widget.ImageView;

import com.timmystudios.webservicesutils.utils.thread.ThreadPool;
import com.timmystudios.webservicesutils.utils.thread.WorkerThread;

import java.io.IOException;
import java.util.List;


public class PictureFitHelper implements WorkerThread.Task {

    public enum FitType {
        FIT_XY, FILL
    }

    private String picturePath = "";
    private int pictureResource = 0;
    private FitType fitType;
    private boolean widthAndHeightSwitched;
    private ImageView targetView;
    private ValueAnimator valueAnimator;
    private Context context;
    private int targetWidth;
    private int targetHeight;
    private OnPictureFitFinishedListener listener;
    private boolean stopped = false;
    private Bitmap bitmap;
    private Matrix matrix = new Matrix();
    private float scale;
    private long durationFadeAnimation;

    private final float MIN_ALPHA = 0f;
    private final float MAX_ALPHA = 1f;
    private static int MAX_FIT_PROCESSES = 4;

    private static ThreadPool threadPool = new ThreadPool(MAX_FIT_PROCESSES);

    public static final long DEFAULT_DURATION_FADE_ANIMATION = 400;

    private PictureFitHelper(){

    }

    public static synchronized void fitPictureInImageView(String picturePath,
                                                          ImageView targetView,
                                                          FitType fitType,
                                                          long durationFadeAnimation,
                                                          OnPictureFitFinishedListener listener) {
        PictureFitHelper pictureFitHelper = new PictureFitHelper();
        pictureFitHelper.picturePath = picturePath;
        pictureFitHelper.targetView = targetView;
        pictureFitHelper.fitType = fitType;
        pictureFitHelper.durationFadeAnimation = durationFadeAnimation;
        pictureFitHelper.context = targetView.getContext();
        pictureFitHelper.listener = listener;
        initiateFitPictureInImageView(pictureFitHelper);
    }

    public static synchronized void fitPictureInImageView(int pictureResource,
                                                          ImageView targetView,
                                                          FitType fitType,
                                                          long durationFadeAnimation,
                                                          OnPictureFitFinishedListener listener) {
        PictureFitHelper pictureFitHelper = new PictureFitHelper();
        pictureFitHelper.pictureResource = pictureResource;
        pictureFitHelper.targetView = targetView;
        pictureFitHelper.fitType = fitType;
        pictureFitHelper.durationFadeAnimation = durationFadeAnimation;
        pictureFitHelper.context = targetView.getContext();
        pictureFitHelper.listener = listener;
        initiateFitPictureInImageView(pictureFitHelper);
    }

    public static synchronized void fitPictureInSize(Context context,
                                                     String picturePath,
                                                     int targetWidth,
                                                     int targetHeight,
                                                     FitType fitType,
                                                     OnPictureFitFinishedListener listener) {
        PictureFitHelper pictureFitHelper = new PictureFitHelper();
        pictureFitHelper.picturePath = picturePath;
        pictureFitHelper.fitType = fitType;
        pictureFitHelper.targetWidth = targetWidth;
        pictureFitHelper.targetHeight = targetHeight;
        pictureFitHelper.context = context;
        pictureFitHelper.listener = listener;
        initiateFitPictureInImageView(pictureFitHelper);
    }

    public static synchronized void fitPictureInSize(Context context,
                                                     int pictureResource,
                                                     int targetWidth,
                                                     int targetHeight,
                                                     FitType fitType,
                                                     OnPictureFitFinishedListener listener) {
        PictureFitHelper pictureFitHelper = new PictureFitHelper();
        pictureFitHelper.pictureResource = pictureResource;
        pictureFitHelper.fitType = fitType;
        pictureFitHelper.targetWidth = targetWidth;
        pictureFitHelper.targetHeight = targetHeight;
        pictureFitHelper.context = context;
        pictureFitHelper.listener = listener;
        initiateFitPictureInImageView(pictureFitHelper);
    }

    public static synchronized void stopFitPictureInImageView(ImageView targetView) {
        stopFitPictureInImageView(targetView, null);
    }

    /**
     * We remove only the first occurrence of a PictureFitHelper associated with the ImageView,
     * because there should only be only one old occurrence. Otherwise, we risk removing the new and
     * good occurrence, if any.
     * @param targetView
     * @param excludedPictureFitHelper {@link ImageView} objects assignd to
     *                                                  this {@link PictureFitHelper} will not be
     *                                                  stopped.
     */
    public static synchronized void stopFitPictureInImageView(ImageView targetView,
                                                              PictureFitHelper
                                                                      excludedPictureFitHelper) {
        PictureFitHelper pictureFitHelper;
        List<WorkerThread.Task> allTasks = threadPool.getAllTasks();
        for (WorkerThread.Task task: allTasks) {
            if (task instanceof PictureFitHelper) {
                pictureFitHelper = (PictureFitHelper) task;
                if (!pictureFitHelper.equals(excludedPictureFitHelper)
                        && pictureFitHelper.targetView == targetView) {
                    pictureFitHelper.stopped = true;
                    if (pictureFitHelper.valueAnimator != null) {
                        pictureFitHelper.valueAnimator.cancel();
                    }
                    threadPool.removeTask(pictureFitHelper);
                    break;
                }
            }
        }
    }

    private static synchronized void initiateFitPictureInImageView(
            final PictureFitHelper pictureFitHelper) {
        if (!pictureFitHelper.stopped) {
            if (pictureFitHelper.targetView != null) {
                stopFitPictureInImageView(pictureFitHelper.targetView, pictureFitHelper);
                pictureFitHelper.targetView.addOnAttachStateChangeListener(
                        new View.OnAttachStateChangeListener() {
                            @Override
                            public void onViewAttachedToWindow(View v) {

                            }

                            @Override
                            public void onViewDetachedFromWindow(View v) {
                                pictureFitHelper.targetView.removeOnAttachStateChangeListener(this);
                                stopFitPictureInImageView(
                                        pictureFitHelper.targetView,
                                        pictureFitHelper
                                );
                            }
                        }
                );
            }
            pictureFitHelper.stopped = false;
            pictureFitHelper.fitPictureInImageView();
        }
    }

    private void fitPictureInImageView() {
        if (!stopped) {
            if (targetView != null) {
                targetWidth = targetView.getWidth();
                targetHeight = targetView.getHeight();
                if (targetWidth != 0 && targetHeight != 0) {
                    registerForExecution();
                } else {
                    targetView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v,
                                                   int left,
                                                   int top,
                                                   int right,
                                                   int bottom,
                                                   int oldLeft,
                                                   int oldTop,
                                                   int oldRight,
                                                   int oldBottom) {
                            if (!stopped) {
                                targetWidth = targetView.getWidth();
                                targetHeight = targetView.getHeight();
                                if (targetWidth != 0 && targetHeight != 0) {
                                    targetView.removeOnLayoutChangeListener(this);
                                    registerForExecution();
                                }
                            }
                        }
                    });
                }
            } else {
                registerForExecution();
            }
        }
    }

    private void registerForExecution() {
        if (!stopped) {
            threadPool.addTask(this);
        }
    }

    @Override
    public void execute() {
        prepareBitmap();
        if (bitmap != null) {
            manipulateMatrix();
        }
    }

    @Override
    public void onFinished() {
        applyBitmapAndMatrixToImageView();
    }

    private void prepareBitmap() {
        if (!stopped) {
            if (!picturePath.equals("")) {
                bitmap = BitmapUtils.decodeSampledBitmapFromPath(
                        picturePath, targetWidth, targetHeight);
            } else if (pictureResource != 0) {
                bitmap = BitmapUtils.decodeSampledBitmapFromResource(
                        context.getResources(), pictureResource,
                        targetWidth, targetHeight);
            }
        }
    }

    private void manipulateMatrix() {
        if (!stopped) {
            matrix.reset();

            bitmap.setHasAlpha(true);

            // Rotate image captured by camera.
            rotateStoredCameraPhoto(bitmap.getWidth(), bitmap.getHeight());

            int width;
            int height;
            if (!widthAndHeightSwitched) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            } else {
                width = bitmap.getHeight();
                height = bitmap.getWidth();
            }

            // Scale image.
            scaleMatrixToFitView(width, height);

            // Translate image to be in center of ImageView
            int scaledWidth = Math.round(width * scale);
            int scaledHeight = Math.round(height * scale);
            translateMatrixToFitView(scaledWidth, scaledHeight);
        }
    }

    private void applyBitmapAndMatrixToImageView() {
        if (!stopped) {
            if (targetView != null && bitmap != null) {
                targetView.setImageBitmap(bitmap);
                targetView.setScaleType(ImageView.ScaleType.MATRIX);
                targetView.setImageMatrix(matrix);
                if (durationFadeAnimation != 0) {
                    executeFadeIn();
                }
            }
            if (listener != null) {
                listener.onPictureFitFinished(bitmap, matrix);
            }
        }
    }

    private void rotateStoredCameraPhoto(int width, int height) {
        try {
            float halfWidth = width / 2;
            float halfHeight = height / 2;
            ExifInterface exif = new ExifInterface(picturePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == 6) {
                matrix.postRotate(90, halfWidth, halfHeight);
                widthAndHeightSwitched = true;
            } else if (orientation == 3) {
                matrix.postRotate(180, halfWidth, halfHeight);
            } else if (orientation == 8) {
                matrix.postRotate(270, halfWidth, halfHeight);
                widthAndHeightSwitched = true;
            }
            if (widthAndHeightSwitched) {
                float difference = Math.abs(width - height) / 2;
                if (width > height) {
                    matrix.postTranslate(-difference, difference);
                } else {
                    matrix.postTranslate(difference, -difference);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void scaleMatrixToFitView(int bitmapWidth, int bitmapHeight) {
        float aspectRatioBitmap = calculateAspectRatio(bitmapWidth, bitmapHeight);
        float aspectRatioView = calculateAspectRatio(
                targetWidth,
                targetHeight
        );
        if ((bitmapHeight <= bitmapWidth && aspectRatioBitmap <= aspectRatioView)
                || (bitmapHeight >= bitmapWidth && aspectRatioBitmap <= aspectRatioView)) {
            if (fitType == FitType.FIT_XY) {
                scale = (float) targetHeight / bitmapHeight;
            } else if (fitType == FitType.FILL) {
                scale = (float) targetWidth / bitmapWidth;
            }
        } else if ((bitmapHeight >= bitmapWidth && aspectRatioBitmap >= aspectRatioView)
                || (bitmapHeight <= bitmapWidth && aspectRatioBitmap >= aspectRatioView)) {
            if (fitType == FitType.FIT_XY) {
                scale = (float) targetWidth / bitmapWidth;
            } else if (fitType == FitType.FILL) {
                scale = (float) targetHeight / bitmapHeight;
            }
        }
        matrix.postScale(scale, scale);
    }

    protected void translateMatrixToFitView(int bitmapWidth, int bitmapHeight) {
        float newX = targetWidth / 2 - bitmapWidth / 2;
        float newY = targetHeight/ 2 - bitmapHeight / 2;
        matrix.postTranslate(newX, newY);
    }

    private float calculateAspectRatio(int width, int height) {
        if (height == 0) return 0;
        return (float)width / (float)height;
    }

    public static void setMaxFitProcesses(int maxFitProcesses) {
        MAX_FIT_PROCESSES = maxFitProcesses;
    }

    private void executeFadeIn() {
        valueAnimator = ValueAnimator.ofFloat(MIN_ALPHA, MAX_ALPHA);
        valueAnimator.setDuration(durationFadeAnimation);
        valueAnimator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animatedValue = (float) animation.getAnimatedValue();
                        targetView.setAlpha(animatedValue);
                    }
                }
        );
        valueAnimator.start();
    }

    public interface OnPictureFitFinishedListener {
        void onPictureFitFinished(Bitmap bitmap, Matrix matrix);
    }

}