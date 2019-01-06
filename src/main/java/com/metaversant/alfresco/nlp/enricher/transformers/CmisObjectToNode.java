package com.metaversant.alfresco.nlp.enricher.transformers;

import com.metaversant.alfresco.nlp.enricher.model.Node;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.GregorianCalendar;

import static com.metaversant.alfresco.nlp.enricher.common.Constants.PROP_CM_DESCRIPTION;
import static com.metaversant.alfresco.nlp.enricher.common.Constants.PROP_CM_TITLE;

/**
 * Converts a CMIS Object into a content node.
 *
 * Created by jpotts, Metaversant on 6/29/18.
 */
public class CmisObjectToNode {
    private static final Logger logger = LogManager.getLogger(CmisObjectToNode.class);

    public static Node transform(CmisObject cmisObject) {
        logger.debug("Inside transform");
        Node node = new Node();
        node.setId(cmisObject.getId());
        node.setName(cmisObject.getName());
        node.setType(cmisObject.getType().getId());

        String title = cmisObject.getPropertyValue(PROP_CM_TITLE);
        if (title != null) {
            node.setTitle(title);
        }

        String description = cmisObject.getPropertyValue(PROP_CM_DESCRIPTION);
        if (description != null) {
            node.setDescription(description);
        }

        GregorianCalendar created = cmisObject.getCreationDate();
        if (created != null) {
            node.setCreated(created.getTime());
        }

        GregorianCalendar modified = cmisObject.getLastModificationDate();
        if (modified != null) {
            logger.debug("modified not null");
            node.setModified(modified.getTime());
        }

        if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
            Document doc = (Document) cmisObject;
            node.setMimetype(doc.getContentStreamMimeType());
            node.setSize(doc.getContentStreamLength());
        }

        return node;
    }
}
