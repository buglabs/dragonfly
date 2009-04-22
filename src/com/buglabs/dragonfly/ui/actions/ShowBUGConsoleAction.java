package com.buglabs.dragonfly.ui.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;

import com.buglabs.dragonfly.model.Bug;
import com.buglabs.dragonfly.model.BugConnection;
import com.buglabs.dragonfly.util.UIUtils;

/**
 * This action is associated with a BUG connection.  The intent
 * is to present the OSGi console for the selected BUG to the user.
 * If the selected connection is a VirtualBUG, the existing console view
 * (which is created when the VB is launched automatically) will be shown.
 * If the selected connection is a real BUG, an existing view (if the action
 * has already been executed in the past for this particular connection) or a new 
 * view will be created for the BUG.  Basic socket I/O is used to connect to the 
 * BUG, similar to telnet.
 * 
 * @author kgilmer
 *
 */
public class ShowBUGConsoleAction extends Action {

	private static final int DEFAULT_BUG_CONSOLE_PORT = 8090;
	private static final String BUG_CONSOLE_NAME = "BUG Console"; //$NON-NLS-1$
	private static final String ACTION_LABEL = "Show OSGi Console"; //$NON-NLS-1$
	private static final Object LOOPBACK_ADDRESS = "127.0.0.1"; //$NON-NLS-1$
	private static final String VIRTUAL_BUG_CONSOLE_TITLE = "Virtual BUG"; //$NON-NLS-1$
	private final TreeViewer viewer;
	private Bug bug;

	public ShowBUGConsoleAction(TreeViewer viewer) {
		this.viewer = viewer;
		setText(ACTION_LABEL);
		setImageDescriptor(ConsolePlugin.getImageDescriptor(IConsoleConstants.IMG_VIEW_CONSOLE));
	}

	@Override
	public void run() {
		bug = (Bug) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		try {
			IConsole c = null;
			
			//If the action was selected on a Virtual BUG, simply show the pre-existing launch configuration.
			//If there are multiple VBs running, this will show the first one found.
			//If the address is not loopback, we assume a "real" bug and try to connect to the default
			//OSGi console port.
			if (bug.getUrl().getHost().equals(LOOPBACK_ADDRESS)) {
				// This is a virtual bug, look for that.
				c = findConsole(VIRTUAL_BUG_CONSOLE_TITLE);
			} else {
				c = findConsole(BUG_CONSOLE_NAME + " [" + bug.getUrl().getHost() + ":" + DEFAULT_BUG_CONSOLE_PORT + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			((IConsoleView)page.showView(IConsoleConstants.ID_CONSOLE_VIEW)).display(c);
		} catch (Exception e) {
			UIUtils.handleVisualError("Unable to display BUG OSGi console.", e); //$NON-NLS-1$
		} 
	}

	// http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F
	private IOConsole findConsole(String name) throws IOException {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			// Look for an existing console that begins with name.
			// this is done rather than a full match, because the console name
			// varies due to connection details present in the string.
			if (existing[i].getName().startsWith(name)) {
				return (IOConsole) existing[i];
			}
		}

		return createConsole(conMan, name);
	}

	/**
	 * @param conMan
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private IOConsole createConsole(IConsoleManager conMan, String name) throws IOException {
		// no console found, so create a new one
		IOConsole myConsole = new IOConsole(name, null);

		Socket s = new Socket();

		// A limitation is that only consoles on default port can be displayed.
		// Perhaps this should go into a preference setting.
		s.connect(new InetSocketAddress(bug.getUrl().getHost(), DEFAULT_BUG_CONSOLE_PORT));

		ConsoleWriter cw = new ConsoleWriter(myConsole, conMan, s);
		ConsoleReader cr = new ConsoleReader(myConsole, s);
		cw.start();
		cr.start();

		conMan.addConsoles(new IConsole[] { myConsole });

		return myConsole;
	}

	@Override
	public boolean isEnabled() {
		if (((IStructuredSelection) viewer.getSelection()).getFirstElement() instanceof BugConnection) {
			return true;
		}

		return false;
	}

	/**
	 * A thread for writing to the Eclipse console UI.
	 * @author kgilmer
	 *
	 */
	private class ConsoleWriter extends Thread {

		private final IOConsole console;
		private final IConsoleManager conMan;
		private Socket socket;

		public ConsoleWriter(IOConsole console, IConsoleManager conMan, Socket s) {
			this.console = console;
			this.conMan = conMan;
			this.socket = s;
		}

		@Override
		public void run() {
			OutputStream consoleOS = console.newOutputStream();

			try {
				InputStream bugIS = socket.getInputStream();
				byte[] response = new byte[1024 * 8];

				while (true) {
					int c = bugIS.read(response);
					if (c > 0) {
						consoleOS.write(response, 0, c);
						conMan.warnOfContentChange(console);
					}
				}
			} catch (Exception e) {
				UIUtils.handleNonvisualWarning("Exception was generated during OSGi console I/O.", e); //$NON-NLS-1$
			}
		}

	}

	/**
	 * A thread for getting input from the user for OSGi console.
	 * 
	 * @author kgilmer
	 *
	 */
	private class ConsoleReader extends Thread {

		private final IOConsole console;
		private Socket socket;

		public ConsoleReader(IOConsole console, Socket s) {
			this.console = console;
			this.socket = s;
		}

		@Override
		public void run() {
			InputStream is = console.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			try {
				OutputStream bugOS = socket.getOutputStream();
				String line = new String();

				while ((line = br.readLine()) != null) {
					bugOS.write(line.toString().getBytes());
					bugOS.write('\n');
				}
			} catch (Exception e) {
				UIUtils.handleNonvisualWarning("Exception was generated during OSGi console I/O.", e); //$NON-NLS-1$
			}
		}
	}
}
