package com.metaversant.alfresco.nlp.enricher.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jpotts, Metaversant on 6/29/18.
 */
@Data
public class Node {
    private String id;
    private String name;
    private String title;
    private String description;
    private String type;
    private String mimetype;
    private String siteId;
    private String parent;
    private Date created;
    private Date modified;
    private long size;
    private HashMap<String, Serializable> metadata;
}
