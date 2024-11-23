package com.highcourt.ruleIQ.api.pojo;

public class ConsumerFileRequest {
    String topicName;
    String fileName;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
