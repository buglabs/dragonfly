package com.buglabs.dragonfly.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * Transfer class to support DND operations with
 * {@link BUGNetProgramReferenceNode} objects.
 * 
 * @author Angel Roman
 * 
 */
public class BugNETProgramReferenceNodeTransfer extends ByteArrayTransfer {

	private static final BugNETProgramReferenceNodeTransfer INSTANCE = new BugNETProgramReferenceNodeTransfer();

	private static final String TYPE_NAME = "bugprogramreferencenode-transfer-format:" + System.currentTimeMillis() + ":"
			+ INSTANCE.hashCode();

	private static final int TYPEID = registerType(TYPE_NAME);

	public static BugNETProgramReferenceNodeTransfer getInstance() {
		return INSTANCE;
	}

	public BugNETProgramReferenceNodeTransfer() {
		super();
	}

	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	protected void javaToNative(Object object, TransferData transferData) {
		if (!(object instanceof BUGNetProgramReferenceNode[])) {
			return;
		}

		BUGNetProgramReferenceNode[] programs = (BUGNetProgramReferenceNode[]) object;

		super.javaToNative(toByteArray(programs), transferData);

	}

	public byte[] toByteArray(BUGNetProgramReferenceNode[] programs) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(out);

		try {
			dataOut.writeInt(programs.length);

			for (int i = 0; i < programs.length; ++i) {
				BUGNetProgramReferenceNode prog = programs[i];
				dataOut.writeUTF(prog.getLabel());
				dataOut.writeUTF(prog.getUserName());
			}

			dataOut.close();
			out.close();

		} catch (IOException e) {
			UIUtils.handleVisualError("Unable to convert program to DND type", e);
			return null;
		}

		return out.toByteArray();
	}

	protected Object nativeToJava(TransferData transferData) {

		byte[] bytes = (byte[]) super.nativeToJava(transferData);

		if (bytes == null) {
			return null;
		}

		return fromByteArray(bytes);

	}

	public BUGNetProgramReferenceNode[] fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		List programs = new ArrayList();

		try {
			int numberofprogs = in.readInt();

			for (int i = 0; i < numberofprogs; ++i) {
				String label = in.readUTF();
				String userName = in.readUTF();
				BUGNetProgramReferenceNode node = new BUGNetProgramReferenceNode(null, label, userName, null, null, null, null, null);
				programs.add(node);
			}
		} catch (IOException e) {
			UIUtils.handleVisualError("Unable to create program from DND operation", e);
			return null;
		}

		return (BUGNetProgramReferenceNode[]) programs.toArray(new BUGNetProgramReferenceNode[programs.size()]);
	}
}
