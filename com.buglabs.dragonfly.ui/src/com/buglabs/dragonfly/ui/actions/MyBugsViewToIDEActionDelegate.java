package com.buglabs.dragonfly.ui.actions;

import org.eclipse.ui.part.IDropActionDelegate;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.dnd.MyBugsViewProgramNodeTransfer;

/**
 * Delegate for importing program bundle into workspace. To use this delegate
 * org.eclipse.ui.dropActions extension point needs to exist in plugin.xml
 * 
 * @author Alex
 * 
 */
public class MyBugsViewToIDEActionDelegate implements IDropActionDelegate {
	public static final String ACTION_ID = "com.buglabs.dragonfly.ui.actions.MyBugsViewToIDEAction"; //$NON-NLS-1$

	public boolean run(Object source, Object target) {
		if (source instanceof byte[]) {
			ProgramNode[] programs = MyBugsViewProgramNodeTransfer.getInstance().fromByteArray((byte[]) source);
			if (programs.length == 0)
				return false;

			ImportBundleAction importAction = new ImportBundleAction(programs[0]);
			importAction.run();
		}
		return false;
	}

}
