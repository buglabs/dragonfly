package com.buglabs.dragonfly.ui.actions;

import org.eclipse.ui.part.IDropActionDelegate;

import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.ui.dnd.BugNETProgramReferenceNodeTransfer;
import com.buglabs.dragonfly.util.UIUtils;

public class BugNetToIDEActionDelegate implements IDropActionDelegate {
	public static final String ACTION_ID = "com.buglabs.dragonfly.ui.actions.BugNetToIDEAction"; //$NON-NLS-1$

	public boolean run(Object source, Object target) {

		if (source instanceof byte[]) {

			BUGNetProgramReferenceNode[] progs = BugNETProgramReferenceNodeTransfer.getInstance().fromByteArray((byte[]) source);

			if (progs.length > 0) {
				BUGNetProgramReferenceNode prog = progs[0];

				String name = prog.getLabel();
				String userName = prog.getUserName();

				if (name != null && userName != null) {
					if (!name.equals("") && !userName.equals("")) { //$NON-NLS-1$
						try {
							// AuthenticationData auth =
							// BUGNetAuthenticationHelper.getAuthenticationData(true);

							// InputStream jarContents =
							// BugnetWSHelper.getProgram(userName, name);
							ImportBundleFromStreamAction action = new ImportBundleFromStreamAction(name);
							action.run();
							// jarContents.close();

						} catch (Exception e) {
							UIUtils.handleVisualError(Messages.getString("BugNetToIDEActionDelegate.3"), e);
						}
					}
					return true;
				}
			}
		}

		// /ImportBundleAction action = new ImportBundleAction();
		// TODO Auto-generated method stub
		return false;
	}

}
