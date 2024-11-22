package com.highcourt.ruleIQ.api.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IFileProducer {
    void download(String remoteFilePath, String localFilePath);


}
