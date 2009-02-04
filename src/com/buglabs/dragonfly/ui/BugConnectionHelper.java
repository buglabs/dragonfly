package com.buglabs.dragonfly.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.model.ITreeNode;
import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.dragonfly.util.UIUtils;

public class BugConnectionHelper {

    /**
     * Gets a list of bug connections via the Activator
     * 
     * @return
     */
    public static List<BugConnection> getBugConnections() {
        ITreeNode bugsRoot = Activator.getDefault().getBugsViewRoot();
        return (List<BugConnection>) bugsRoot.getChildren();
    }
    
    /**
     * 
     * @param connection
     * @return
     */
    public static List<String> getPackagesForBugConnection(BugConnection connection) {
        List<String> packages = new ArrayList<String>();
        try {
            packages = BugWSHelper.getPackages(connection.getPackageURL());
        } catch (IOException e) {
            UIUtils.handleNonvisualError("Unable to connect to BUG to get packages", e);
        }
        return packages;
    }
    
    public static BugConnection getBugConnectionByName(String connectionName) {
        // get out bug connection by matching against something in list
        BugConnection connection = null;
        Iterator<BugConnection> connectionItr = getBugConnections().iterator();
        while(connectionItr.hasNext()) {
            connection = connectionItr.next();
            if (connection == null || !connection.getName().equals(connectionName)) {
                connection = null;
            } else {
                break;
            }
        }
        return connection;
    }
    
}
