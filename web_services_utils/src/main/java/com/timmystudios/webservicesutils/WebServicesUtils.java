package com.timmystudios.webservicesutils;


import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.security.ProviderInstaller;
import com.timmystudios.webservicesutils.utils.FileUtils;
import com.timmystudios.webservicesutils.utils.PictureFitHelper;
import com.timmystudios.webservicesutils.interceptors.DefaultQueryParamsInterceptor;
import com.timmystudios.webservicesutils.interceptors.DownloadProgressInterceptor;
import com.timmystudios.webservicesutils.listeners.DownloadProgressListener;
import com.timmystudios.webservicesutils.listeners.FetchCompletedListener;
import com.timmystudios.webservicesutils.listeners.WriteToDiskListener;
import com.timmystudios.webservicesutils.model.DownloadedItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class WebServicesUtils {

    private static WebServicesUtils webServicesUtils;
    private Retrofit retrofit;
    private List<Object> createdServices = new ArrayList<>();
    private static long imageCacheSize = 104857600; // 100 MB
    private static HashMap<ImageView, String> imageViewUrlsHashMap = new HashMap<>();

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, HashMap<String, String> defaultQueryParams) {
        try {
            ProviderInstaller.installIfNeeded(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        RxJavaPlugins.setErrorHandler(Functions.<Throwable>emptyConsumer());
        webServicesUtils = new WebServicesUtils();
        webServicesUtils.retrofit = createRetrofit(defaultQueryParams);
    }

    private static Retrofit createRetrofit(HashMap<String, String> defaultQueryParams) {
        return new Retrofit.Builder()
                .baseUrl("https://no_base_url.com")
                .client(createOkHttpClient(defaultQueryParams))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private static OkHttpClient createOkHttpClient(
            final HashMap<String, String> defaultQueryParams) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Add Logging Interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);
        // End Add Logging Interceptor

        // Add Default Query Params Interceptor
        DefaultQueryParamsInterceptor defaultQueryParamsInterceptor
                = new DefaultQueryParamsInterceptor(defaultQueryParams);
        httpClient.addInterceptor(defaultQueryParamsInterceptor);
        // Add Default Query Params Interceptor

        // Add Progress Interceptor
        DownloadProgressInterceptor downloadProgressInterceptor = new DownloadProgressInterceptor();
        httpClient.addNetworkInterceptor(downloadProgressInterceptor);
        // End Add Progress Interceptor

        // Configure Timeouts
        httpClient.connectTimeout(120000, TimeUnit.MILLISECONDS);
        httpClient.readTimeout(120000, TimeUnit.MILLISECONDS);
        httpClient.writeTimeout(120000, TimeUnit.MILLISECONDS);
        // End Configure Timeouts

        return httpClient.build();
    }

    public static <T> T getService(Class<T> serviceClass) {
        for (Object createdService: webServicesUtils.createdServices) {
            if (createdService.getClass().isInstance(serviceClass)) {
                return serviceClass.cast(createdService);
            }
        }
        T newService = webServicesUtils.retrofit.create(serviceClass);
        webServicesUtils.createdServices.add(newService);
        return newService;
    }

    public static void removeService(Class serviceClass) {
        Iterator<Object> createdServicesIterator = webServicesUtils.createdServices.iterator();
        Object createdService;
        while (createdServicesIterator.hasNext()) {
            createdService = createdServicesIterator.next();
            if (createdService.getClass().isInstance(serviceClass)) {
                createdServicesIterator.remove();
                return;
            }
        }
    }

    public static void removeAllServices() {
        webServicesUtils.createdServices.clear();
    }

    public static Single<DownloadedItem> downloadFile(String url,
                                                      String pathToDirectory,
                                                      String fileName) {
        return DownloadManager.downloadFile(url, url, pathToDirectory, fileName);
    }

    public static void cancelDownloadFile(String url) {
        DownloadManager.cancelDownloadFile(url);
    }

    public static Single<DownloadedItem> cacheImage(String url, Context context) {
        String imageCacheFolderPath = getImageCacheFolderPath(context);
        String cachedFileName = FileUtils.correctFileName(url);
        String cachedFilePath = imageCacheFolderPath + "/" + cachedFileName;
        File file = new File(cachedFilePath);
        if (file.exists()) {
            DownloadedItem downloadedItem = new DownloadedItem(file, url);
            return Single.just(downloadedItem);
        } else {
            return DownloadManager.downloadFile(url, url, imageCacheFolderPath, cachedFileName);
        }
    }

    public static Single<DownloadedItem> downloadFile(String downloadIdentifier,
                                                      String url,
                                                      String pathToDirectory,
                                                      String fileName) {
        return DownloadManager.downloadFile(downloadIdentifier, url, pathToDirectory, fileName);
    }

    public static void addDownloadProgressListener(
            DownloadProgressListener downloadProgressListener) {
        DownloadProgressInterceptor.addProgressListener(downloadProgressListener);
    }

    public static void removeDownloadProgressListener(
            DownloadProgressListener downloadProgressListener) {
        DownloadProgressInterceptor.removeProgressListener(downloadProgressListener);
    }

    public static void removeDownloadAllProgressListeners() {
        DownloadProgressInterceptor.removeAllProgressListeners();
    }

    public static void addWriteToDiskListener(WriteToDiskListener writeToDiskListener) {
        DownloadManager.addWriteToDiskListener(writeToDiskListener);
    }

    public static void removeWriteToDiskListener(WriteToDiskListener writeToDiskListener) {
        DownloadManager.removeWriteToDiskListener(writeToDiskListener);
    }

    public static void clearWriteToDiskListener() {
        DownloadManager.clearWriteToDiskListeners();
    }

    public static void setImage(final String url,
                                final ImageView imageView,
                                final PictureFitHelper.FitType fitType,
                                long durationFadeAnimation) {
        setImage(url, imageView, fitType, durationFadeAnimation, null);
    }

    public static void setImage(final String url,
                                final ImageView imageView,
                                final PictureFitHelper.FitType fitType,
                                final long durationFadeAnimation,
                                final PictureFitHelper.OnPictureFitFinishedListener listener) {
        if (imageViewUrlsHashMap.containsKey(imageView)) {
            imageViewUrlsHashMap.remove(imageView);
        }
        imageViewUrlsHashMap.put(imageView, url);

        final View.OnAttachStateChangeListener onAttachStateChangeListener
                = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (checkIfSetImage(imageView, url)) {
                    imageView.removeOnAttachStateChangeListener(this);
                    DownloadManager.cancelDownloadFile(url);
                    imageViewUrlsHashMap.remove(imageView);
                }
            }
        };
        imageView.addOnAttachStateChangeListener(onAttachStateChangeListener);

        String imageCacheFolderPath = getImageCacheFolderPath(imageView.getContext());
        String cachedFileName = FileUtils.correctFileName(url);

        String cachedFilePath = imageCacheFolderPath + "/" + cachedFileName;
        File file = new File(cachedFilePath);
        if (file.exists()) {
            if (checkIfSetImage(imageView, url)) {
                setImage(file, imageView, fitType, durationFadeAnimation, listener);
            }
        } else {
            makeRoomForImageInCache(url, imageView.getContext());
            DownloadManager
                    .downloadFile(url, url, imageCacheFolderPath, cachedFileName)
                    .subscribe(new SingleObserver<DownloadedItem>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(DownloadedItem downloadedItem) {
                            if (checkIfSetImage(imageView, url)) {
                                imageView.removeOnAttachStateChangeListener(
                                        onAttachStateChangeListener
                                );
                                File downloadedFile = downloadedItem.getFile();
                                setImage(
                                        downloadedFile,
                                        imageView,
                                        fitType,
                                        durationFadeAnimation,
                                        listener
                                );
                                imageViewUrlsHashMap.remove(imageView);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            imageView.removeOnAttachStateChangeListener(
                                    onAttachStateChangeListener
                            );
                            imageViewUrlsHashMap.remove(imageView);
                        }
                    });
        }
    }

    private static boolean checkIfSetImage(ImageView imageView, String url) {
        return imageViewUrlsHashMap.containsKey(imageView)
                && imageViewUrlsHashMap.get(imageView).equals(url);
    }

    private static void setImage(File file,
                                 ImageView imageView,
                                 PictureFitHelper.FitType fitType,
                                 long durationFadeAnimation,
                                 PictureFitHelper.OnPictureFitFinishedListener listener) {
        if (file != null) {
            String downloadedFilePath = file.getPath();
            PictureFitHelper.fitPictureInImageView(
                    downloadedFilePath,
                    imageView,
                    fitType,
                    durationFadeAnimation,
                    listener
            );
        } else if (listener != null) {
            listener.onPictureFitFinished(null, null);
        }
    }

    public static void getImage(final String url,
                                final Context context,
                                final int targetWidth,
                                final int targetHeight,
                                final PictureFitHelper.FitType fitType,
                                final PictureFitHelper.OnPictureFitFinishedListener listener) {
        String imageCacheFolderPath = getImageCacheFolderPath(context);
        String cachedFileName = FileUtils.correctFileName(url);

        String cachedFilePath = imageCacheFolderPath + "/" + cachedFileName;
        File file = new File(cachedFilePath);
        if (file.exists()) {
            getImage(file, context, targetWidth, targetHeight, fitType, listener);
        } else {
            makeRoomForImageInCache(url, context);
            DownloadManager
                    .downloadFile(url, url, imageCacheFolderPath, cachedFileName)
                    .subscribe(new SingleObserver<DownloadedItem>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(DownloadedItem downloadedItem) {
                            File downloadedFile = downloadedItem.getFile();
                            getImage(
                                    downloadedFile,
                                    context,
                                    targetWidth,
                                    targetHeight,
                                    fitType,
                                    listener
                            );
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private static void getImage(File file,
                                 Context context,
                                 int targetWidth,
                                 int targetHeight,
                                 PictureFitHelper.FitType fitType,
                                 PictureFitHelper.OnPictureFitFinishedListener listener) {
        if (file != null) {
            String downloadedFilePath = file.getPath();
            PictureFitHelper.fitPictureInSize(
                    context,
                    downloadedFilePath,
                    targetWidth,
                    targetHeight,
                    fitType,
                    listener
            );
        } else if (listener != null) {
            listener.onPictureFitFinished(null, null);
        }
    }

    public static void makeRoomForImageInCache(final String url, final Context context) {
        DownloadManager
                .addFetchCompletedListener(new FetchCompletedListener() {
                    @Override
                    public void onFetchCompleted(Call<ResponseBody> call,
                                                 Response<ResponseBody> response) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            String imageCacheFolderPath = getImageCacheFolderPath(context);
                            File imageCacheFolderFile = new File(imageCacheFolderPath);
                            if (imageCacheFolderFile.exists()) {
                                long imageCacheFolderSize = imageCacheFolderFile.length();
                                long newImageSize = responseBody.contentLength();
                                long totalSize = imageCacheFolderSize + newImageSize;
                                if (totalSize > imageCacheSize) {
                                    File[] cachedImagesArray = imageCacheFolderFile.listFiles();
                                    List<File> cachedImagesList = new ArrayList<>();
                                    for (File cachedImage:  cachedImagesArray) {
                                        cachedImagesList.add(cachedImage);
                                    }
                                    Collections.sort(cachedImagesList, new Comparator<File>() {
                                        @Override
                                        public int compare(File o1, File o2) {
                                            String path1 = o1.getAbsolutePath();
                                            String path2 = o2.getAbsolutePath();

                                            long time1 = FileUtils.getLastFileAccessTime(path1);
                                            long time2 = FileUtils.getLastFileAccessTime(path2);
                                            return Long.signum(time1 - time2);
                                        }
                                    });
                                    int cachedImageIndex = 0;
                                    File cachedImage;
                                    while (totalSize > imageCacheSize
                                            && cachedImageIndex < cachedImagesList.size()) {
                                        cachedImage = cachedImagesList.get(cachedImageIndex);
                                        cachedImage.delete();
                                        totalSize -= cachedImage.length();
                                        cachedImageIndex++;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public String getDownloadIdentifier() {
                        return url;
                    }
                });
    }

    public static void setImageCacheSize(long imageCacheSize) {
        webServicesUtils.imageCacheSize = imageCacheSize;

    }

    public static String getImageCacheFolderPath(Context context) {
        File cacheDir = context.getCacheDir();
        String cacheDirPath = cacheDir.getAbsolutePath();
        return cacheDirPath + "/image_cache";
    }
}
