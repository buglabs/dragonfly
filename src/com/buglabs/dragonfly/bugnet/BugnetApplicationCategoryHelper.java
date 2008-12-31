package com.buglabs.dragonfly.bugnet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BugnetApplicationCategoryHelper {
    public static final String ALL_APPLICATIONS = "All Applications";
    public static final String MY_APPLICATIONS  = "My Applications";
    public static final String DEFAULT_CATEGORY = ALL_APPLICATIONS;
    public static final int DEFAULT_CATEGORY_INDEX = 0;
    public static final String BUG_CONNECTION_CATEGORY_PREFIX = "BUG Connection:";
    
    private List categories;
    
    public BugnetApplicationCategoryHelper() {
        resetCategories();
    }
    
    public void resetCategories() {
        categories = new ArrayList();
        categories.add(ALL_APPLICATIONS);
        categories.add(MY_APPLICATIONS);        
    }

    public String[] getCategories() {
        return (String[])categories.toArray(new String[0]);
    }
    
    public void addCategories(List newCategories) {
        categories.addAll(newCategories);
    }
    
    public int getCategoryIndex(String category) {
        Iterator itr = categories.iterator();
        int index = 0;
        while(itr.hasNext()) {
            if (itr.next().equals(category)) {
                return index;
            }
            index++;
        }
        return DEFAULT_CATEGORY_INDEX;
    }
}
