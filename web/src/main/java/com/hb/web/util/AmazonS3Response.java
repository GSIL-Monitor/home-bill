package com.hb.web.util;

import java.io.Serializable;

public class AmazonS3Response implements Serializable {
    private static final long serialVersionUID = -2339812169889593051L;
    private String bucketName;
    private String fileName;
    private String path;

    public AmazonS3Response(String bucketName, String fileName, String path) {
        this.bucketName = bucketName;
        this.fileName = fileName;
        this.path = path;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "AmazonS3Response{" +
                "bucketName='" + bucketName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AmazonS3Response that = (AmazonS3Response) o;

        if (bucketName != null ? !bucketName.equals(that.bucketName) : that.bucketName != null) return false;
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bucketName != null ? bucketName.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
