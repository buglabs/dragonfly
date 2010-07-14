package com.buglabs.bug.base;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.buglabs.bug.base.pub.IShellService;
import com.buglabs.bug.base.pub.IShellSession;

/**
 * Default implementation of IShellService.
 * 
 * @author kgilmer
 */
public class ShellService implements IShellService {

	private static final String UNIX_TMP_DIR = "/tmp";
	private static final String WINDOWS_TEMP_DIR = "C:\\temp";

	public IShellSession createShellSession() throws IOException {
		if (isWindows()) {
			return new ShellSession(new File(WINDOWS_TEMP_DIR), null);
		}

		return new ShellSession(new File(UNIX_TMP_DIR), null);
	}

	public IShellSession createShellSession(File directory, Writer output) throws IOException {
		return new ShellSession(directory, output);
	}

	/**
	 * @return true if we are running on Windows OS. This is only possible with
	 *         Virtual BUG.
	 */
	private boolean isWindows() {
		return System.getProperty("os.name").indexOf("Win") != -1;
	}
}
