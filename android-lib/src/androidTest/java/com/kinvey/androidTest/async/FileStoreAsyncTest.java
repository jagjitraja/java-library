package com.kinvey.androidTest.async;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.TimeUtils;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreAsyncTest {

    Client client = null;
    boolean success;
    FileMetaData fileMetaDataResult;

    @Before
    public void setup() {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
/*        client.userStore().login("test", "test", new KinveyClientCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user = result;
            }

            @Override
            public void onFailure(Throwable error) {

            }
        });*/
    }

    @Test
    public void testUploadFileNetworkNullCheck() throws InterruptedException, IOException {
        uploadFileWithMetaData(StoreType.NETWORK, false);
    }

    @Test
    public void testUploadFileCacheNullCheck() throws InterruptedException, IOException {
        uploadFileWithMetaData(StoreType.CACHE, false);
    }

    @Test
    public void testUploadFileSyncNullCheck() throws InterruptedException, IOException {
        uploadFileWithMetaData(StoreType.SYNC, false);
    }

    public FileMetaData uploadFileWithMetaData(final StoreType storeType, final boolean isPreload) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {

                    final File file = new File(client.getContext().getFilesDir(), "test.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    final FileMetaData fileMetaData = new FileMetaData();
                    fileMetaData.setFileName("test.xml");
                    client.getFileStore(storeType).upload(file, fileMetaData, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            fileMetaDataResult = result;
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, new UploaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpUploader uploader) throws IOException {

                        }

                        @Override
                        public void onSuccess(FileMetaData result) {
                            fileMetaDataResult = result;
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        if (isPreload) {
            return fileMetaDataResult;
        } else {
            assertTrue(success);
            return null;
        }
    }

    @Test
    public void testDownloadFileNetworkNullCheck() throws InterruptedException, IOException {
        downloadFile(StoreType.NETWORK, null);
    }

    @Test
    public void testDownloadFileNetwork() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.NETWORK, true);
        downloadFile(StoreType.NETWORK, fileMetaData);
    }

    @Test
    public void testDownloadFileCacheNullCheck() throws InterruptedException, IOException {
        downloadFile(StoreType.CACHE, null);
    }

    @Test
    public void testDownloadFileCache() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.CACHE, true);
        downloadFile(StoreType.CACHE, fileMetaData);
    }

    @Test
    public void testDownloadFileSyncNullCheck() throws InterruptedException, IOException {
        downloadFile(StoreType.SYNC, null);
    }

    @Test
    public void testDownloadFileSync() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.SYNC, true);
        downloadFile(StoreType.SYNC, fileMetaData);
    }

    public void downloadFile(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    final FileOutputStream fos = new FileOutputStream(file);
                    client.getFileStore(storeType).download(fileMetaData, fos, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            if (fileMetaData == null) {
                                if (error.getCause().getMessage().contains("metadata must not be null")) {
                                    finish(true);
                                }
                            } else {
                                finish(false);
                            }
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, new DownloaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }

                        @Override
                        public void onSuccess(Void result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {

                            if (fileMetaData == null) {
                                if (error.getMessage().contains("Missing FileMetaData in cache")) {
                                    finish(true);
                                }
                            } else {
                                finish(false);
                            }
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        assertTrue(success);
    }

    @Test
    public void testRemoveFileNetwork() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.NETWORK, true);
        removeFile(StoreType.NETWORK, fileMetaData);
    }

    @Test
    public void testRemoveFileSync() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.SYNC, true);
        removeFile(StoreType.SYNC, fileMetaData);
    }

    @Test
    public void testRemoveFileCache() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.CACHE, true);
        removeFile(StoreType.CACHE, fileMetaData);
    }

    public void removeFile(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    File file = new File(client.getContext().getFilesDir(), "testDownload.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    client.getFileStore(storeType).remove(fileMetaData, new KinveyDeleteCallback() {
                        @Override
                        public void onSuccess(Integer result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            if (fileMetaData == null) {
                                if (error.getCause().getMessage().contains("metadata must not be null")) {
                                    finish(true);
                                }
                            } else {
                                finish(false);
                            }
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        assertTrue(success);
    }

    @Test
    public void testCachedFileCache() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.CACHE, true);
        isCachedFile(StoreType.CACHE, fileMetaData);
    }

    @Test
    public void testCachedFileSync() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFileWithMetaData(StoreType.SYNC, true);
        isCachedFile(StoreType.SYNC, fileMetaData);
    }

    public void isCachedFile(final StoreType storeType, final FileMetaData fileMetaData) throws InterruptedException, IOException {
        FileMetaData metaData;
        metaData = client.getFileStore(storeType).cachedFile(fileMetaData);
        success = metaData != null;
        assertTrue(success);
    }

    @Test
    public void testUploadFileNetwork() throws InterruptedException, IOException {
        uploadFile(StoreType.NETWORK, false);
    }

    @Test
    public void testUploadFileSync() throws InterruptedException, IOException {
        uploadFile(StoreType.SYNC, false);
    }

    @Test
    public void testUploadFileCache() throws InterruptedException, IOException {
        uploadFile(StoreType.CACHE, false);
    }

    public FileMetaData uploadFile(final StoreType storeType, final boolean isPreload) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {

                    final File file = new File(client.getContext().getFilesDir(), + Calendar.getInstance().getTime().getTime() + "test.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    client.getFileStore(storeType).upload(file, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            fileMetaDataResult = result;
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, new UploaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpUploader uploader) throws IOException {

                        }

                        @Override
                        public void onSuccess(FileMetaData result) {
                            fileMetaDataResult = result;
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        if (isPreload) {
            return fileMetaDataResult;
        } else {
            assertTrue(success);
            return null;
        }
    }

    @Test
    public void testDownloadFileQueryNetwork() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.NETWORK, true);
        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", fileMetaData.getId());
        downloadFileQuery(StoreType.NETWORK, query);
    }

    @Test
    public void testDownloadFileQuerySync() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.SYNC, true);
        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", fileMetaData.getId());
        downloadFileQuery(StoreType.SYNC, query);
    }

    @Test
    public void testDownloadFileQueryCache() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.CACHE, true);
        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder()).equals("_id", fileMetaData.getId());
        downloadFileQuery(StoreType.CACHE, query);
    }

    public void downloadFileQuery(final StoreType storeType, final Query query) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    String dst = client.getContext().getFilesDir() + "test.xml";
                    client.getFileStore(storeType).download(query, dst, new KinveyClientCallback<FileMetaData[]>() {
                        @Override
                        public void onSuccess(FileMetaData[] result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, new DownloaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }

                        @Override
                        public void onSuccess(Void result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        assertTrue(success);
    }

    @Test
    public void testDownloadFileFileNameNetwork() throws InterruptedException, IOException {
        FileMetaData fileMetaData = uploadFile(StoreType.NETWORK, true);
        downloadFileFileName(StoreType.NETWORK, fileMetaData.getFileName());
    }

    public void downloadFileFileName(final StoreType storeType, final String fileName) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    String dst = client.getContext().getFilesDir() +"/"+ fileName;
                    client.getFileStore(storeType).download(fileName, dst, new KinveyClientCallback<FileMetaData>() {
                        @Override
                        public void onSuccess(FileMetaData result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    }, new DownloaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }

                        @Override
                        public void onSuccess(Void result) {
                            finish(true);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            finish(false);
                        }

                        public void finish(boolean result) {
                            success = result;
                            latch.countDown();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        assertTrue(success);
    }

}
