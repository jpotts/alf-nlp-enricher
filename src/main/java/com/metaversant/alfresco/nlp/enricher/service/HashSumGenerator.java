package com.metaversant.alfresco.nlp.enricher.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jpotts, Metaversant on 10/3/18.
 */
@Service
public class HashSumGenerator {
    private static final Logger logger = LogManager.getLogger(HashSumGenerator.class);

    public static String getHash(String text) {
        return DigestUtils.md5Hex(text);
    }

    public static String getHash(InputStream stream) {
        String hash = null;
        try {
            hash = DigestUtils.md5Hex(stream);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {}
        }
        return hash;
    }

}
