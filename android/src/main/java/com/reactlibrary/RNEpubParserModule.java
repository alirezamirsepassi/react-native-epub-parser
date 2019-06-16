package com.reactlibrary;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.github.mertakdut.BookSection;
import com.github.mertakdut.NavPoint;
import com.github.mertakdut.Package;
import com.github.mertakdut.Reader;
import com.github.mertakdut.Toc;
import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNEpubParserModule extends ReactContextBaseJavaModule {

    private static final String READING_ERROR = "READING_ERROR";
    private static final String OUTOFPAGES_ERROR = "OUTOFPAGES_ERROR";
    private static final String IO_ERROR = "IO_ERROR";
    private final ReactApplicationContext reactContext;
    public Toc bookToc;
    public Reader reader;
    public byte[] coverImage;
    public Package infoPackage;

    /**
     * Constructor
     *
     * @param reactContext
     */
    public RNEpubParserModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reader = new Reader();
    }

    @Override
    public String getName() {
        return "RNEpubParser";
    }

    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();
        constants.put("MODULE_NAME", this.getName());

        return constants;
    }

    @ReactMethod
    public void setMaxContentPerSection(Integer contentSize) {
        reader.setMaxContentPerSection(contentSize);
    }

    @ReactMethod
    public void setIsIncludingTextContent(Boolean isIncludingTextContent) {
        reader.setIsIncludingTextContent(isIncludingTextContent);
    }

    @ReactMethod
    public void setIsOmittingTitleTag(Boolean isOmittingTitleTag) {
        reader.setIsOmittingTitleTag(isOmittingTitleTag);
    }

    @ReactMethod
    public void setFullContentPath(String source) {
        try {
            reader.setFullContent(source);

            bookToc = reader.getToc();
            coverImage = reader.getCoverImage();
            infoPackage = reader.getInfoPackage();
        } catch (ReadingException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void setFullContentPath(String source, Boolean withProgress) {
        try {
            reader.setFullContentWithProgress(source);

            bookToc = reader.getToc();
            coverImage = reader.getCoverImage();
            infoPackage = reader.getInfoPackage();
        } catch (ReadingException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void setFullContentURL(String source, Boolean withProgress, Promise promise) {
        final Boolean isWithProgress = withProgress;
        final Promise methodPromise = promise;

        try {
            File downloadedFile = File.createTempFile("epub", ".epub", reactContext.getCacheDir());
            new RNEpubParserDownload(source, downloadedFile, new RNEpubParserDownload.Listener() {
                @Override
                public void onStarted() {
                    reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("epubParser:download:started", "");
                }

                @Override
                public void onComplete(File file) {
                    setFullContentPath(file.getAbsolutePath(), isWithProgress);
                    methodPromise.resolve(file.getAbsolutePath());
                }

                @Override
                public void onProgress(Integer progress) {
                    reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("epubParser:download:progress", progress);
                }
            }).execute();
        } catch (IOException e) {
            methodPromise.reject(IO_ERROR, e);
        }
    }

    @ReactMethod
    public void getBookSection(Integer pageIndex, Promise promise) {
        WritableMap response = Arguments.createMap();
        try {
            BookSection bookSection = reader.readSection(pageIndex);
            response.putString("label", bookSection.getLabel());
            response.putString("extension", bookSection.getExtension());
            response.putString("sectionContent", bookSection.getSectionContent());
            response.putString("mediaType", bookSection.getMediaType());
            response.putString("sectionTextContent", bookSection.getSectionTextContent());

            promise.resolve(response);
        } catch (ReadingException e) {
            promise.reject(READING_ERROR, e);
        } catch (OutOfPagesException e) {
            promise.reject(OUTOFPAGES_ERROR, e);
        }
    }

    @ReactMethod
    public void getHead(Promise promise) {
        WritableMap response = Arguments.createMap();
        response.putString("uid", bookToc.getHead().getUid());
        response.putString("depth", bookToc.getHead().getDepth());
        response.putString("maxPageNumber", bookToc.getHead().getMaxPageNumber());
        response.putString("totalPageCount", bookToc.getHead().getTotalPageCount());

        promise.resolve(response);
    }

    @ReactMethod
    public void loadProgress(Promise promise) {
        try {
            reader.loadProgress();
            promise.resolve(true);
        } catch (ReadingException e) {
            promise.reject(READING_ERROR, e);
        }
    }

    @ReactMethod
    public void saveProgress(Promise promise) {
        try {
            reader.saveProgress();
            promise.resolve(true);
        } catch (ReadingException e) {
            promise.reject(READING_ERROR, e);
        }
    }

    @ReactMethod
    public void saveProgress(Integer lastPageIndex, Promise promise) {
        try {
            reader.saveProgress(lastPageIndex);
        } catch (ReadingException e) {
            promise.reject(READING_ERROR, e);
        } catch (OutOfPagesException e) {
            promise.reject(OUTOFPAGES_ERROR, e);
        }
    }

    @ReactMethod
    public void getInfo(Promise promise) {
        WritableMap response = Arguments.createMap();
        WritableMap metadataObject = Arguments.createMap();

        metadataObject.putString("contributor", infoPackage.getMetadata().getContributor());
        metadataObject.putString("coverage", infoPackage.getMetadata().getCoverage());
        metadataObject.putString("coverImageId", infoPackage.getMetadata().getCoverImageId());
        metadataObject.putString("creator", infoPackage.getMetadata().getCreator());
        metadataObject.putString("date", infoPackage.getMetadata().getDate());
        metadataObject.putString("description", infoPackage.getMetadata().getDescription());
        metadataObject.putString("format", infoPackage.getMetadata().getFormat());
        metadataObject.putString("identifier", infoPackage.getMetadata().getIdentifier());
        metadataObject.putString("language", infoPackage.getMetadata().getLanguage());
        metadataObject.putString("publisher", infoPackage.getMetadata().getPublisher());
        metadataObject.putString("relation", infoPackage.getMetadata().getRelation());
        metadataObject.putString("rights", infoPackage.getMetadata().getRights());
        metadataObject.putString("source", infoPackage.getMetadata().getSource());
        metadataObject.putString("title", infoPackage.getMetadata().getTitle());
        metadataObject.putString("type", infoPackage.getMetadata().getType());
        if (infoPackage.getMetadata().getSubjects() == null) {
            metadataObject.putArray("subjects", Arguments.createArray());
        } else {
            metadataObject.putArray("subjects", Arguments.fromArray(infoPackage.getMetadata().getSubjects()));
        }

        response.putMap("metadata", metadataObject);

        promise.resolve(response);
    }

    @ReactMethod
    public void getNavigation(Promise promise) {
        WritableArray response = Arguments.createArray();
        List<NavPoint> navPoints = bookToc.getNavMap().getNavPoints();
        for (NavPoint navPoint : navPoints) {
            WritableMap navPointMap = Arguments.createMap();
            navPointMap.putString("label", navPoint.getNavLabel());
            navPointMap.putString("contentSource", navPoint.getContentSrc());
            response.pushMap(navPointMap);
        }

        promise.resolve(response);
    }
}