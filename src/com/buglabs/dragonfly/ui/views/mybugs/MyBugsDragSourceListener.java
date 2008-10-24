package com.buglabs.dragonfly.ui.views.mybugs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.ui.actions.MyBugsViewToIDEActionDelegate;
import com.buglabs.dragonfly.ui.dnd.MyBugsViewProgramNodeTransfer;

public class MyBugsDragSourceListener implements DragSourceListener {

	private TreeViewer viewer;

	private TransferData dataType;

	public MyBugsDragSourceListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public void dragFinished(DragSourceEvent event) {
		// TODO Auto-generated method stub

	}

	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		ProgramNode[] programs = (ProgramNode[]) selection.toList().toArray(new ProgramNode[selection.size()]);
		dataType = event.dataType;
		if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			byte[] programBytes = MyBugsViewProgramNodeTransfer.getInstance().toByteArray(programs);
			event.data = new PluginTransferData(MyBugsViewToIDEActionDelegate.ACTION_ID, programBytes);
		}
	}

	public void dragStart(DragSourceEvent event) {
		// only allow program nodes to be dragged
		Object selection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (!(selection instanceof ProgramNode)) {
			event.doit = false;
		}
	}

}
