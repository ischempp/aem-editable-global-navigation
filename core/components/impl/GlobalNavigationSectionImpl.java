package org.fhcrc.www.core.components.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import com.day.cq.wcm.api.Page;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.fhcrc.www.core.Utils;
import org.fhcrc.www.core.components.GlobalNavigationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(
    adaptables = {Resource.class},
    adapters = {GlobalNavigationSection.class},
    resourceType = GlobalNavigationSectionImpl.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class GlobalNavigationSectionImpl implements GlobalNavigationSection {

    protected static final String RESOURCE_TYPE = "www/components/structure/globalNavigationSection";
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalNavigationSectionImpl.class);

    private static final String FIRST_COLUMN_TOPICS = "column1/topics";
    private static final String SECOND_COLUMN_TOPICS = "column2/topics";
    private static final String THIRD_COLUMN_TOPICS = "column3/topics";
    private static final String PN_TOPIC_HEAD = "sectionHead";
    private static final String PN_TOPIC_ITEMS = "sectionItems";
    private static final String PN_PATH = "path";
    private static final String PN_TITLE = "title";

    @Self
    private Resource resource;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String overviewText;

    @ValueMapValue
    private String overviewURL;

    private ArrayList<ArrayList<Topic>> columns;

    @PostConstruct
    protected void initModel() {

        setColumns();

    }


    public String getTitle() {

        return title;

    }

    public String getOverviewText() {

        return overviewText;

    }

    public String getOverviewURL() {

        return Utils.cleanLink(overviewURL);

    }

    public ArrayList<ArrayList<Topic>> getColumns() {

        return columns;

    }

    private Boolean hasColumn(String columnName) {

        String columnPath = resource.getPath() + "/" + columnName;
        Resource column = resourceResolver.resolve(columnPath);
        LOGGER.debug("GNAV has column " + columnName + " = " + column != null ? "true" : "false");

        return column != null;

    }

    private void setColumns() {

        columns = new ArrayList<ArrayList<Topic>>();
        ArrayList<Topic> columnTopics;

        if (hasColumn(FIRST_COLUMN_TOPICS)) {
            columnTopics = setTopicsList(FIRST_COLUMN_TOPICS);
            columns.add(columnTopics);
        }

        if (hasColumn(SECOND_COLUMN_TOPICS)) {
            columnTopics = setTopicsList(SECOND_COLUMN_TOPICS);
            columns.add(columnTopics);
        }

        if (hasColumn(THIRD_COLUMN_TOPICS)) {
            columnTopics = setTopicsList(THIRD_COLUMN_TOPICS);
            columns.add(columnTopics);
        }

    }

    private ArrayList<Topic> setTopicsList(String topicsPath) {

        ArrayList<Topic> topics = new ArrayList<Topic>();

        try {

            Resource topicsResource = resource.getChild(topicsPath);
            if (topicsResource != null) {

                LOGGER.debug("GNAV setting topics list: " + topicsPath);

                Node topicsNode = topicsResource.adaptTo(Node.class);
                NodeIterator topicsIterator = topicsNode.getNodes();

                while(topicsIterator.hasNext()) {

                    String topicHead = "";
                    HashMap<String,String> headMap = new HashMap<String,String>();
                    ArrayList<Map<String,String>> topicItems = new ArrayList<Map<String,String>>();
                    Node child = topicsIterator.nextNode();
                    
                    if (child.hasProperty(PN_TOPIC_HEAD)) {

                        try {

                            topicHead = child.getProperty(PN_TOPIC_HEAD).getString();

                            Page p = resourceResolver.resolve(topicHead).adaptTo(Page.class);
                            headMap.put(PN_TITLE, Utils.getTitle(p, Utils.NAV_TITLE_LEVEL));
                            headMap.put(PN_PATH, Utils.cleanLink(p.getPath()));

                            LOGGER.debug("GNAV adding title = " + headMap.get(PN_TITLE));
                            LOGGER.debug("GNAV adding path = " + headMap.get(PN_PATH));

                        } catch (IllegalArgumentException e) {

                            LOGGER.error("Topic head page adapted from path was null");

                        }

                    }

                    if (child.hasProperty(PN_TOPIC_ITEMS)) {

                        LOGGER.debug("GNAV adding subitems: " + child.getPath());

                        // If there is only one value, it doesn't count as multi-value and getValues will throw an exception so check first
                        if (child.getProperty(PN_TOPIC_ITEMS).isMultiple()) {

                            Value[] values = child.getProperty(PN_TOPIC_ITEMS).getValues();

                            for (int i = 0; i < values.length; i++) {

                                try {

                                    HashMap<String,String> itemMap = new HashMap<String,String>();

                                    String itemPath = values[i].getString();
                                    Page p = resourceResolver.resolve(itemPath).adaptTo(Page.class);
                                    itemMap.put(PN_TITLE, Utils.getTitle(p, Utils.NAV_TITLE_LEVEL));
                                    itemMap.put(PN_PATH, Utils.cleanLink(p.getPath()));

                                    LOGGER.debug("GNAV adding sub title = " + itemMap.get(PN_TITLE));
                                    LOGGER.debug("GNAV adding sub path = " + itemMap.get(PN_PATH));

                                    topicItems.add(itemMap);

                                } catch (IllegalArgumentException e) {

                                    LOGGER.error("Topic item page adapted from path array was null; page not added to topic items.");

                                }                                

                            }

                        } else {

                            try {

                                HashMap<String,String> itemMap = new HashMap<String,String>();

                                String itemPath = child.getProperty(PN_TOPIC_ITEMS).getString();
                                Page p = resourceResolver.resolve(itemPath).adaptTo(Page.class);
                                itemMap.put(PN_TITLE, Utils.getTitle(p, Utils.NAV_TITLE_LEVEL));
                                itemMap.put(PN_PATH, Utils.cleanLink(p.getPath()));

                                topicItems.add(itemMap);

                            } catch (IllegalArgumentException e) {

                                LOGGER.error("Topic item page adapted from single path was null; page not added to topic items.");

                            }

                        }

                    }

                    Topic t = new Topic(headMap, topicItems);
                    topics.add(t);
                    
                }

            }

        } catch (ValueFormatException e) {

            // Change me to debug later
            LOGGER.debug("Topic item was not a string - {}", e.getMessage());
            
        } catch (RepositoryException e) {

            // Change me to debug later
            LOGGER.debug("There were no topics in this column - ", e.getMessage());

        } 

        return topics;

    }

    public class Topic {

        private Map<String,String> headPage;
        private ArrayList<Map<String,String>> items;

        public Topic(Map<String,String> headPage, ArrayList<Map<String, String>> items) {

            this.headPage = headPage;
            this.items = items;

        }

        public Map<String,String> getHeadPage()  {

            return headPage;

        }

        public ArrayList<Map<String,String>> getItems() {

            return items;

        }

    }

}
