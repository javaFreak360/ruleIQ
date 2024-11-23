package com.highcourt.ruleIQ.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service("sftpProducer")
public class SftpService implements IFileService {

    private static final Logger logger = LoggerFactory.getLogger(SftpService.class);

    private IEventProducer eventProducer;

    private DefaultSftpSessionFactory sftpSessionFactory;

    private ObjectMapper mapper;


    @Autowired
    public SftpService(IEventProducer eventProducer, DefaultSftpSessionFactory sftpSessionFactory, ObjectMapper mapper){
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
            }catch(Exception e){
                logger.error("Error while downloading file {} {}", remoteFilePath, e.getMessage());
            }

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void upload(String localFilePath, String remoteFilePath) {
        SftpSession session = null;
        try {
            // Get a new SFTP session
            session = sftpSessionFactory.getSession();

            // Open an InputStream for the local file
            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                logger.error("Local file does not exist: {}", localFilePath);
                return;
            }
            try (InputStream inputStream = new FileInputStream(localFile)) {
                // Upload the file to the remote path
                session.write(inputStream, remoteFilePath);
                logger.info("File uploaded successfully from {} to {}", localFilePath, remoteFilePath);
            } catch (Exception e) {
                logger.error("Error while uploading file from {} to {}: {}", localFilePath, remoteFilePath, e.getMessage());
            }

        } finally {
            // Ensure the session is closed properly
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


}
