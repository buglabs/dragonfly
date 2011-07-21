package com.buglabs.dragonfly.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.buglabs.dragonfly.model.IPackage;
import com.buglabs.dragonfly.model.PackageImpl;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.util.UIUtils;

public class MyBugsViewProgramNodeTransfer extends ByteArrayTransfer {
	private final static MyBugsViewProgramNodeTransfer INSTANCE = new MyBugsViewProgramNodeTransfer();

	private static final String TYPE_NAME = "bugprogramreferencenode-transfer-format:" + System.currentTimeMillis() + ":" + INSTANCE.hashCode(); //$NON-NLS-1$

	private static final int TYPEID = registerType(TYPE_NAME);

	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	public static MyBugsViewProgramNodeTransfer getInstance() {
		return INSTANCE;
	}

	protected void javaToNative(Object object, TransferData transferData) {
		if (!(object instanceof ProgramNode[])) {
			return;
		}
		ProgramNode[] programs = (ProgramNode[]) object;

		super.javaToNative(toByteArray(programs), transferData);
	}

	public byte[] toByteArray(ProgramNode[] programs) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(out);

		try {
			dataOut.writeInt(programs.length);

			for (int i = 0; i < programs.length; ++i) {
				ProgramNode program = programs[i];
				dataOut.writeUTF(program.getPackage().getName().toString());
				dataOut.writeUTF(program.getPackageUrl().toString());
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

	public ProgramNode[] fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		List programs = new ArrayList();

		try {
			int numberofprogs = in.readInt();

			for (int i = 0; i < numberofprogs; ++i) {
				IPackage packageName = new PackageImpl(in.readUTF());
				URL packageURL = new URL(in.readUTF());
				ProgramNode node = new ProgramNode(packageName, packageURL);
				programs.add(node);
			}
		} catch (IOException e) {
			UIUtils.handleVisualError("Unable to create program from DND operation", e);
			return null;
		}
		return (ProgramNode[]) programs.toArray(new ProgramNode[programs.size()]);
	}
}
