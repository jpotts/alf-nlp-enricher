package com.metaversant.alfresco.nlp.enricher.service;

import com.metaversant.alfresco.nlp.enricher.exceptions.AlfrescoServiceException;
import com.metaversant.alfresco.nlp.enricher.model.Node;
import com.metaversant.alfresco.nlp.enricher.transformers.CmisObjectToNode;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jpotts, Metaversant on 8/17/18.
 */
@Service
public class AlfrescoService {
    private static final Logger logger = LogManager.getLogger(AlfrescoService.class);

    @Value("${alfresco.base.url}")
    private String baseUrl;

    @Value("${alfresco.user}")
    private String user;

    @Value("${alfresco.password}")
    private String password;

    public Node getNode(String id) throws AlfrescoServiceException {
        logger.debug("Inside getNode");
        CmisObject obj;
        if (id == null || id.length() == 0) {
            throw new AlfrescoServiceException("ID is required");
        } else {
            obj = getSession().getObject(id);
        }

        // Fetch the metadata values
        Node node = CmisObjectToNode.transform(obj);
        node.setMetadata(getMetadata(obj));
        return node;
    }

    public void updateNode(String id, HashMap<String, Serializable> properties) throws AlfrescoServiceException {
        CmisObject obj;
        if (id == null || id.length() == 0) {
            throw new AlfrescoServiceException("ID is required");
        } else {
            obj = getSession().getObject(id);
        }

        obj.updateProperties(properties);
    }

    public InputStream getContent(String id) throws AlfrescoServiceException {
        logger.debug("Inside getContent");
        CmisObject obj;
        if (id == null || id.length() == 0) {
            throw new AlfrescoServiceException("ID is required");
        } else {
            obj = getSession().getObject(id);
        }

        if (obj == null) {
            logger.debug("Object is null, returning");
            return null;
        }

        Document doc = null;
        logger.debug("BaseTypeId: " + obj.getBaseTypeId().value());
        if (obj.getBaseTypeId().value().equals("cmis:document")) {
            doc = (Document) obj;
        }

        if (doc == null) {
            logger.debug("Doc is null, returning");
            return null;
        }

        logger.debug("Returning stream");
        return doc.getContentStream().getStream();
    }

    private HashMap<String, Serializable> getMetadata(CmisObject object) {
        HashMap<String, Serializable> metadata = new HashMap<>();
        List<Property<?>> propertyList = object.getProperties();
        for (Property<?> property : propertyList) {
            Serializable propertyValue = property.getValue();
            if (propertyValue != null) {
                metadata.put(property.getLocalName(), propertyValue);
            }
        }
        return metadata;
    }

    protected Session getSession() {
        // default factory implementation of client runtime
        SessionFactory f = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        // user credentials
        parameter.put(SessionParameter.USER, user);
        parameter.put(SessionParameter.PASSWORD, password);

        // connection settings
        parameter.put(SessionParameter.BROWSER_URL, getServiceUrl());
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
        parameter.put(SessionParameter.REPOSITORY_ID, "-default-");

        // create session
        return f.createSession(parameter);
    }

    private String getServiceUrl() {
        return baseUrl + "/api/-default-/public/cmis/versions/1.1/browser";
    }
}
