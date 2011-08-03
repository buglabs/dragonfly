package com.buglabs.dragonfly.ui.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.buglabs.dragonfly.launch.BUGSimulatorLaunchConfigurationDelegate;
import com.buglabs.dragonfly.util.UIUtils;

public class BUGSimulatorLaunchShortCut implements ILaunchShortcut {

	protected IProgressMonitor progressMonitor;
	private boolean discoveryMode = false;

	public void launch(ISelection selection, String mode) {
		try {
			launch(mode);
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to launch BUG Simulator", e);
		}
	}

	public void launch(IEditorPart editor, String mode) {
		try {
			launch(mode);
		} catch (CoreException e) {
			UIUtils.handleVisualError("Unable to launch BUG Simulator", e);
		}
	}

	public ILaunch launch(String mode) throws CoreException {
		ILaunchConfiguration configuration = null;
		ILaunchConfiguration[] configs = getLaunchConfigurations();

		if (configs.length == 0) {
			configuration = createNewConfiguration(mode);
		} else if (configs.length == 1) {
			configuration = configs[0];
		} else {
			configuration = chooseConfiguration(configs, mode);
		}

		final ProgressMonitorDialog pmd = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			pmd.run(false, false, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					progressMonitor = pmd.getProgressMonitor();
				}
			});
		} catch (InvocationTargetException e) {
			UIUtils.handleNonvisualError("Error launching BUG Simulator", e);
		} catch (InterruptedException e) {
			UIUtils.handleNonvisualError("Error launching BUG Simulator", e);
		}

		if (configuration != null) {
			return configuration.launch(mode, progressMonitor);			
		}

		return null;
	}
	
	public void setDiscoveryMode(boolean discoveryMode) {
		this.discoveryMode = discoveryMode;
	}
	
	public boolean getDiscoveryMode() {
		return discoveryMode;
	}

	private ILaunchConfiguration[] getLaunchConfigurations() {
		ArrayList result = new ArrayList();
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfiguration[] configs = manager.getLaunchConfigurations(manager.getLaunchConfigurationType(BUGSimulatorLaunchConfigurationDelegate.ID));
			for (int i = 0; i < configs.length; i++) {
				if (!DebugUITools.isPrivate(configs[i])) {
					result.add(configs[i]);
				}
			}
		} catch (CoreException e) {
		}
		return (ILaunchConfiguration[]) result.toArray(new ILaunchConfiguration[result.size()]);
	}

	private ILaunchConfiguration createNewConfiguration(String mode) throws CoreException {
		ILaunchConfiguration config = null;

		ILaunchConfigurationType configType = getLaunchConfigurationType();
		String computedName = getComputedName("BUG Simulator"); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, computedName);
		SimulatorLaunchConfigurationInitializer.initializeSystemProperties(wc, discoveryMode);
		
		config = wc.doSave();

		return config;
	}

	private ILaunchConfigurationType getLaunchConfigurationType() {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		return manager.getLaunchConfigurationType(BUGSimulatorLaunchConfigurationDelegate.ID);
	}

	protected ILaunchConfiguration chooseConfiguration(ILaunchConfiguration[] configs, String mode) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), labelProvider);
		dialog.setElements(configs);
		dialog.setTitle("Launch BUG Simulator");
		dialog.setMessage("Please select a launch configuration.");
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	private String getComputedName(String prefix) {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		return lm.generateUniqueLaunchConfigurationNameFrom(prefix);
	}
}
