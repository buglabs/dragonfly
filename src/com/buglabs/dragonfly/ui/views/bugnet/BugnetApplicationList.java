package com.buglabs.dragonfly.ui.views.bugnet;

import java.util.ArrayList;
import java.util.List;

import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;

public class BugnetApplicationList {
    private List<BUGNetProgramReferenceNode> applications;
    
    public BugnetApplicationList() {
        initApplicationList();
    }
    
    public void initApplicationList() {
        applications = new ArrayList<BUGNetProgramReferenceNode>();
    }
    
    public void setApplications(List<BUGNetProgramReferenceNode> applications) {
        this.applications = applications;
    }
    
    public void addApplications(List<BUGNetProgramReferenceNode> applications) {
        this.applications.addAll(applications);
    }
    
    public List<BUGNetProgramReferenceNode> getApplications() {
        return applications;
    }
    
}
