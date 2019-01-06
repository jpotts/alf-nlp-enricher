package com.metaversant.alfresco.nlp.enricher.service;

import com.metaversant.alfresco.nlp.enricher.exceptions.AlfrescoServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Created by jpotts, Metaversant on 10/11/18.
 */
@Service
public class Downloader {
    private static final Logger logger = LogManager.getLogger(Downloader.class);

    @Autowired
    private AlfrescoService alfrescoService;

    public void download(String id, String pathString) {
        InputStream stream = null;
        try {
            stream = alfrescoService.getContent(id);
        } catch (AlfrescoServiceException ase) {
            logger.error(ase.getMessage());
        }

        if (stream != null) {
            try {
                File targetFile = new File(pathString);
                OutputStream outStream = new FileOutputStream(targetFile);
                IOUtils.copy(stream, outStream);
                outStream.close();
                logger.debug("File written to: " + pathString);
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
            } finally {
                try {
                    stream.close();
                } catch (IOException ioe) {}
            }
        }
    }
}
