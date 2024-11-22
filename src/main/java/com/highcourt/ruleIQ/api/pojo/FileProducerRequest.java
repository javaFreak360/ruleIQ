package com.highcourt.ruleIQ.api.pojo;

public class FileProducerRequest {

    FileProducerType producerType;
    String entity;
    String remoteFilePath;
    String localFilePath;

    public FileProducerType getProducerType() {
        return producerType;
    }

    public void setProducerType(FileProducerType producerType) {
        this.producerType = producerType;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }
}
