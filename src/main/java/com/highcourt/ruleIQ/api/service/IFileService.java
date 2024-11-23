package com.highcourt.ruleIQ.api.service;

public interface IFileService {
    void download(String remoteFilePath, String localFilePath);
    void upload(String localFilePath, String remoteFilePath);

}
