package com.applozic.mobicomkit.api.attachment;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicomkit.stego.Cipher;
import com.applozic.mobicomkit.stego.ImageHelper;
import com.applozic.mobicomkit.stego.StegoProcessor;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by devashish on 26/12/14.
 */
public class FileClientService extends MobiComKitClientService {

    //Todo: Make the base folder configurable using either strings.xml or properties file
    public static final String MOBI_COM_IMAGES_FOLDER = "/image";
    public static final String MOBI_COM_VIDEOS_FOLDER = "/video";
    public static final String MOBI_COM_CONTACT_FOLDER = "/contact";
    public static final String MOBI_COM_OTHER_FILES_FOLDER = "/other";
    public static final String MOBI_COM_THUMBNAIL_SUFIX = "/.Thumbnail";
    public static final String FILE_UPLOAD_URL = "/rest/ws/aws/file/url";
    public static final String IMAGE_DIR = "image";
    public static final String AL_UPLOAD_FILE_URL = "/rest/ws/upload/file";
    public static final String CUSTOM_STORAGE_SERVICE_END_POINT = "/rest/ws/upload/image";
    public static final String THUMBNAIL_URL = "/files/";
    private static final int MARK = 1024;
    private static final String TAG = "FileClientService";
    private static final String MAIN_FOLDER_META_DATA = "main_folder_name";
    private HttpRequestUtils httpRequestUtils;

    public FileClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public static File getFilePath(String fileName, Context context, String contentType, boolean isThumbnail) {
        File filePath;
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_OTHER_FILES_FOLDER;

            if (contentType.startsWith("image")) {
                folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_IMAGES_FOLDER;
            } else if (contentType.startsWith("video")) {
                folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_VIDEOS_FOLDER;
            } else if (contentType.equalsIgnoreCase("text/x-vCard")) {
                folder = "/" + Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_COM_CONTACT_FOLDER;
            }
            if (isThumbnail) {
                folder = folder + MOBI_COM_THUMBNAIL_SUFIX;
            }
            dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            ContextWrapper cw = new ContextWrapper(context);
            // path to /data/data/yourapp/app_data/imageDir
            dir = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        }
        // Create image name
        //String extention = "." + contentType.substring(contentType.indexOf("/") + 1);
        filePath = new File(dir, fileName);
        return filePath;
    }

    public static File getFilePath(String fileName, Context context, String contentType) {
        return getFilePath(fileName, context, contentType, false);
    }

    public String profileImageUploadURL() {
        return getBaseUrl() + AL_UPLOAD_FILE_URL;
    }

    public String getFileUploadUrl() {
        if (ApplozicClient.getInstance(context).isCustomStorageServiceEnabled()) {
            return getBaseUrl() + CUSTOM_STORAGE_SERVICE_END_POINT;
        }

        String fileUploadUrl = Utils.getMetaDataValue(context.getApplicationContext(), FILE_UPLOAD_METADATA_KEY);
        if (!TextUtils.isEmpty(fileUploadUrl)) {
            return getFileBaseUrl() + fileUploadUrl;
        }
        return getFileBaseUrl() + FILE_UPLOAD_URL;
    }

    public Bitmap loadThumbnailImage(Context context, Message message, int reqWidth, int reqHeight) {
        try {
            Bitmap attachedImage = null;
            FileMeta fileMeta = message.getFileMetas();
            String thumbnailUrl = fileMeta.getThumbnailUrl();
            String contentType = fileMeta.getContentType();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // Todo get the file format from server and append
            String imageName = FileUtils.getName(fileMeta.getName()) + message.getCreatedAtTime() + "." + FileUtils.getFileFormat(fileMeta.getName());
            String imageLocalPath = getFilePath(imageName, context, fileMeta.getContentType(), true).getAbsolutePath();
            if (imageLocalPath != null) {
                try {
                    attachedImage = BitmapFactory.decodeFile(imageLocalPath);
                } catch (Exception ex) {
                    Utils.printLog(context, TAG, "File not found on local storage: " + ex.getMessage());
                }
            }
            if (attachedImage == null) {
                HttpURLConnection connection = openHttpConnection(thumbnailUrl);
                if (connection.getResponseCode() == 200) {
                    // attachedImage = BitmapFactory.decodeStream(connection.getInputStream(),null,options);
                    attachedImage = BitmapFactory.decodeStream(connection.getInputStream());
                    File file = FileClientService.getFilePath(imageName, context, contentType, true);
                    imageLocalPath = ImageUtils.saveImageToInternalStorage(file, attachedImage);

                } else {
                    Utils.printLog(context, TAG, "Download is failed response code is ...." + connection.getResponseCode());
                    return null;
                }
            }
            // Calculate inSampleSize
            options.inSampleSize = ImageUtils.calculateInSampleSize(options, 200, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            attachedImage = BitmapFactory.decodeFile(imageLocalPath, options);
            return attachedImage;
        } catch (FileNotFoundException ex) {
            Utils.printLog(context, TAG, "File not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        }

        return null;
    }

    /**
     * @param message
     */

    public void loadContactsvCard(Message message) {
        File file = null;
        try {
            InputStream inputStream = null;
            FileMeta fileMeta = message.getFileMetas();
            String contentType = fileMeta.getContentType();
            HttpURLConnection connection;
            String fileName = fileMeta.getName();
            file = FileClientService.getFilePath(fileName, context.getApplicationContext(), contentType);
            if (!file.exists()) {
                if (ApplozicClient.getInstance(context).isCustomStorageServiceEnabled() && !TextUtils.isEmpty(message.getFileMetas().getUrl())) {
                    connection = openHttpConnection(fileMeta.getUrl());
                } else {
                    connection = openHttpConnection(new MobiComKitClientService(context).getFileUrl() + fileMeta.getBlobKeyString());
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                } else {
                    //TODO: Error Handling...
                    Utils.printLog(context, TAG, "Got Error response while uploading file : " + connection.getResponseCode());
                    return;
                }

                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                inputStream.close();
            }
            //Todo: Fix this, so that attach package can be moved to mobicom mobicom.
            new MessageDatabaseService(context).updateInternalFilePath(message.getKeyString(), file.getAbsolutePath());

            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(file.getAbsolutePath());
            message.setFilePaths(arrayList);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "File not found on server");
        } catch (Exception ex) {
            //If partial file got created delete it, we try to download it again
            if (file != null && file.exists()) {
                Utils.printLog(context, TAG, " Exception occured while downloading :" + file.getAbsolutePath());
                file.delete();
            }
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Exception fetching file from server");
        }
    }


    public Bitmap loadMessageImage(Context context, String url) {
        try {
            Bitmap attachedImage = null;

            if (attachedImage == null) {
                InputStream in = new java.net.URL(url).openStream();
                if (in != null) {
                    attachedImage = BitmapFactory.decodeStream(in);
                }
            }
            return attachedImage;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "File not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        }

        return null;
    }

    public String uploadBlobImage(String path, Handler handler) throws UnsupportedEncodingException {
        try {
            ApplozicMultipartUtility multipart = new ApplozicMultipartUtility(getUploadKey(), "UTF-8", context);
            if (ApplozicClient.getInstance(context).isCustomStorageServiceEnabled()) {
                multipart.addFilePart("file", new File(path), handler);
            } else {
                multipart.addFilePart("files[]", new File(path), handler);
            }
            return multipart.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUploadKey() {
        if (ApplozicClient.getInstance(context).isStorageServiceEnabled() || ApplozicClient.getInstance(context).isCustomStorageServiceEnabled() ) {
            return getFileUploadUrl();
        } else {
            return httpRequestUtils.getResponse(getFileUploadUrl()
                    + "?" + new Date().getTime(), "text/plain", "text/plain", true);
        }
    }

    public Bitmap downloadBitmap(Contact contact, Channel channel) {
        HttpURLConnection connection = null;
        MarkStream inputStream = null;
        try {
            if (contact != null) {
                connection = openHttpConnection(contact.getImageURL());
            } else {
                connection = openHttpConnection(channel.getImageUrl());
            }
            if (connection != null) {
                if (connection.getResponseCode() == 200) {
                    inputStream = new MarkStream(connection.getInputStream());
                    BitmapFactory.Options optionsBitmap = new BitmapFactory.Options();
                    optionsBitmap.inJustDecodeBounds = true;
                    inputStream.allowMarksToExpire(false);
                    long mark = inputStream.setPos(MARK);
                    BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.resetPos(mark);
                    optionsBitmap.inJustDecodeBounds = false;
                    optionsBitmap.inSampleSize = ImageUtils.calculateInSampleSize(optionsBitmap, 100, 50);
                    Bitmap attachedImage = BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.allowMarksToExpire(true);
                    return attachedImage;
                } else {
                    Utils.printLog(context, TAG, "Download is failed response code is ...." + connection.getResponseCode());
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Image not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        } catch (Throwable t) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public Bitmap createAndSaveVideoThumbnail(String filePath) {
        String[] parts = filePath.split("/");
        String videoThumbnailPath = "";
        String thumbnailDir = "";

        String videoFileName = parts[parts.length - 1].split("[.]")[0];
        for (int i = 0; i < parts.length - 1; i++) {
            thumbnailDir += (parts[i] + "/");
        }
        thumbnailDir = thumbnailDir + "Thumbnails/";
        File dir = new File(thumbnailDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        videoThumbnailPath = thumbnailDir + videoFileName + ".jpeg";
        Bitmap videoThumbnail = null;

        if (new File(videoThumbnailPath).exists()) {
            videoThumbnail = BitmapFactory.decodeFile(videoThumbnailPath);
        } else {
            OutputStream fOut = null;
            File file = new File(thumbnailDir, videoFileName + ".jpeg");
            try {
                file.createNewFile();
                fOut = new FileOutputStream(file);
                videoThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return videoThumbnail;

    }

    public String uploadProfileImage(String path) throws UnsupportedEncodingException {
        try {
            ApplozicMultipartUtility multipart = new ApplozicMultipartUtility(profileImageUploadURL(), "UTF-8", context);
            multipart.addFilePart("file", new File(path), null);
            return multipart.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap loadMessageImage(Context context, Conversation conversation) {
        try {
            if (conversation == null) {
                return null;
            }
            Bitmap attachedImage = ImageUtils.getBitMapFromLocalPath(conversation.getTopicLocalImageUri());
            if (attachedImage != null) {
                return attachedImage;
            }
            Bitmap bitmap = downloadProductImage(conversation);
            if (bitmap != null) {
                File file = FileClientService.getFilePath("topic_" + conversation.getId(), context.getApplicationContext(), "image", true);
                String imageLocalPath = ImageUtils.saveImageToInternalStorage(file, bitmap);
                conversation.setTopicLocalImageUri(imageLocalPath);
            }
            if (!TextUtils.isEmpty(conversation.getTopicLocalImageUri())) {
                ConversationService.getInstance(context).updateTopicLocalImageUri(conversation.getTopicLocalImageUri(), conversation.getId());
            }
            return bitmap;

        } catch (Exception e) {

        }
        return null;
    }

    public Bitmap downloadProductImage(Conversation conversation) {
        TopicDetail topicDetail = (TopicDetail) GsonUtils.getObjectFromJson(conversation.getTopicDetail(), TopicDetail.class);
        if (TextUtils.isEmpty(topicDetail.getLink())) {
            return null;
        }
        HttpURLConnection connection = null;
        MarkStream inputStream = null;
        try {
            if (conversation != null) {
                connection = openHttpConnection(topicDetail.getLink());
            }
            if (connection != null) {
                if (connection.getResponseCode() == 200) {
                    inputStream = new MarkStream(connection.getInputStream());
                    BitmapFactory.Options optionsBitmap = new BitmapFactory.Options();
                    optionsBitmap.inJustDecodeBounds = true;
                    inputStream.allowMarksToExpire(false);
                    long mark = inputStream.setPos(MARK);
                    BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.resetPos(mark);
                    optionsBitmap.inJustDecodeBounds = false;
                    optionsBitmap.inSampleSize = ImageUtils.calculateInSampleSize(optionsBitmap, 100, 50);
                    Bitmap attachedImage = BitmapFactory.decodeStream(inputStream, null, optionsBitmap);
                    inputStream.allowMarksToExpire(true);
                    return attachedImage;
                } else {
                    return null;
                }
            }
        } catch (FileNotFoundException ex) {
            Utils.printLog(context, TAG, "Image not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            Utils.printLog(context, TAG, "Exception fetching file from server: " + ex.getMessage());
        } catch (Throwable t) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void writeFile(Uri uri, File file, boolean isStegoOn){
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            out = new FileOutputStream(file);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {


            if (in != null && out != null) {
                try {
                    if(isStegoOn){
                        insertMessage(file, "hello");
                    }
                    out.flush();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void insertMessage(File file, String message) throws Exception {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        List<double[][]> blocks = new ImageHelper().convertToArray(bitmap);
        List<double[][]> blueMatrix = new ImageHelper().getBlocks(blocks.get(2));
        Cipher cipher = new Cipher();
        char[] mess = new StegoProcessor(message).prepareMessage().toCharArray();
        List<double[][]> blokcWithCoeff = new ArrayList<>();

        for(int i = 0; i < mess.length; i++){
            blokcWithCoeff.add(cipher.smartInsert(blueMatrix.get(i), (mess[i])));
        }


        double[][] finalBlue = setBlocks(blocks.get(2), blokcWithCoeff);
        Bitmap newBitmap = new ImageHelper().createImage(blocks.get(0), blocks.get(1), finalBlue, bitmap);

        OutputStream os;
        try {
            os = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

    }




    public static double[][] setBlocks(double[][] array, List<double[][]> list) {
        int rows = array.length / 8;
        int cols = array[0].length / 8;
        int indBlock = 0;

        for (int indRow = 0; indRow < rows; indRow++)
            for (int indCol = 0; indCol < cols; indCol++) {
                double[][] tempBlock = list.get(indBlock);
                for (int i = 0; i < 8; i++)
                    System.arraycopy(tempBlock[i], 0, array[indRow * 8 + i], indCol * 8 + 0, 8);
                indBlock++;
            }
        return array;
    }


    public String getThumbnailUrl(String thumbnailUrl) {
        return (ApplozicClient.getInstance(context).isStorageServiceEnabled() ?
                (getFileBaseUrl() + THUMBNAIL_URL + thumbnailUrl) : thumbnailUrl);

    }
}
