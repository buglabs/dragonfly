package com.buglabs.dragonfly.ui.actions;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.DragonflyActivator;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.ModelNodeChangeEvent;
import com.buglabs.dragonfly.model.VirtualBUGConnection;
import com.buglabs.dragonfly.ui.launch.VirtualBugLaunchShortCut;
import com.buglabs.dragonfly.ui.views.mybugs.MyBugsView;
import com.buglabs.dragonfly.util.BugListener;
import com.buglabs.dragonfly.util.UIUtils;

public class LaunchVirtualBugAction implements IWorkbenchWindowActionDelegate, IDebugEventSetListener {

	private static final String TYPE = "type";
	private static final String VIRTUAL_BUG = "VIRTUAL_BUG";
	private IAction action;
	private Object lock = new Object();

	public void init(IWorkbenchWindow window) {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void run(final IAction action) {
		try {
			this.action = action;
			
			DragonflyActivator.getDefault().setVirtualBugRemovedByTerminate(false);
			// about to launch a Virtual BUG, disable action
			ServerSocket socket = new ServerSocket(Integer.parseInt(DragonflyActivator.getDefault().getHttpPort()));
			socket.close();

			VirtualBugLaunchShortCut launchSC = new VirtualBugLaunchShortCut();
			ILaunch launch = launchSC.launch(ILaunchManager.DEBUG_MODE);
			
			IProcess[] launchedProcesses = launch.getProcesses();
			launchedProcesses[0].setAttribute(TYPE,VIRTUAL_BUG);
			
			// disable launch button only if processes were started
			if(launchedProcesses.length != 0)
				action.setEnabled(false);

			// we need to find out when virtual bug has launched so we can
			// enabled vb button, once we enable
			// and user tries to launch again, connecting to ServerSocket will
			// throw an exception.
			if(launchedProcesses != null && launchedProcesses.length > 0) {
				launchedProcesses[0].getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {
					int cnt = 0;

					public void streamAppended(String text, IStreamMonitor monitor) {
						if (text.indexOf("com.buglabs.bug.emulator.awt") != -1) {
							cnt++;
							if (cnt == 2){
								action.setEnabled(true);
							}
						}
					}

				});
			}
		} catch (CoreException e) {
			action.setEnabled(true);
			UIUtils.handleVisualError("Unable to launch Virtual BUG", e);
		} catch (IOException e) {
			action.setEnabled(true);
			UIUtils.handleNonvisualError("Virtual BUG is already running", e);
			MessageDialog.openInformation(new Shell(), "Virtual BUG Launch", "A Virtual BUG is already running. Please close it and launch again.");
		} 
		catch(Exception e){
			action.setEnabled(true);
			UIUtils.handleNonvisualError("Unable to launch Virtual BUG", e);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void handleDebugEvents(DebugEvent[] events) {
		for(int i = 0; i < events.length; i++){
			if(events[i].getKind() == DebugEvent.TERMINATE){
				if(events[i].getSource() instanceof RuntimeProcess){
					RuntimeProcess rp = (RuntimeProcess) events[i].getSource();
					String type = rp.getAttribute(TYPE);
					if(type != null){
						if(type.equals(VIRTUAL_BUG)){
							removeVBFromBugsView();
							if(!action.isEnabled()){
								action.setEnabled(true);
							}
						}
					}
				}
			}
		}
	}

	private void removeVBFromBugsView() {
		Collection children = MyBugsView.getRoot().getChildren();
		Object[] array = children.toArray();
		
		for(int i = 0; i < array.length; i++){
			Object bugConnection = array[i];
			if(bugConnection instanceof VirtualBUGConnection){
				DragonflyActivator.getDefault().setVirtualBugRemovedByTerminate(true);
				MyBugsView.getRoot().removeChild((IModelNode) bugConnection);
				DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, BugListener.REMOVE_BUG, null, bugConnection));
			}	
		}
		/*Iterator iterator = children.iterator();
		while(iterator.hasNext()){
			synchronized (lock ) {
				Object bugConnection = iterator.next();
				if(bugConnection instanceof VirtualBUGConnection){
					DragonflyActivator.getDefault().setVirtualBugRemovedByTerminate(true);
					MyBugsView.getRoot().removeChild((IModelNode) bugConnection);
					DragonflyActivator.getDefault().fireModelChangeEvent(new PropertyChangeEvent(this, BugListener.REMOVE_BUG, null, bugConnection));
				}	
			}
		}*/
	}
}
