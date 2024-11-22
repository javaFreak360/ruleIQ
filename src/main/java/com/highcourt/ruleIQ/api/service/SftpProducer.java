package com.highcourt.ruleIQ.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

@Service("sftpProducer")
public class SftpProducer implements IFileProducer{

    private static final Logger logger = LoggerFactory.getLogger(SftpProducer.class);

    private IEventProducer eventProducer;

    private DefaultSftpSessionFactory sftpSessionFactory;

    private ObjectMapper mapper;


    @Autowired
    public SftpProducer(IEventProducer eventProducer, DefaultSftpSessionFactory sftpSessionFactory, ObjectMapper mapper){
        this.eventProducer = eventProducer;
        this.sftpSessionFactory = sftpSessionFactory;
        this.mapper =  mapper;
    }

    @Override
    public void download(String remoteFilePath, String localFilePath) {
        SftpSession session = null;
        try {
            session = sftpSessionFactory.getSession();
            try (OutputStream os = new FileOutputStream(new File(localFilePath))) {
                session.read(remoteFilePath, os);
                logger.info("File downloaded successfully to {}", localFilePath);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from SFTP server", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


}
