package com.buglabs.dragonfly.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.buglabs.dragonfly.ui.actions.LaunchWelcomeEditorAction;

/**
 * A handler for help.sdk_welcome command
 * 
 * @author akravets
 * 
 */
public class WelcomeHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		LaunchWelcomeEditorAction action = new LaunchWelcomeEditorAction();
		action.run();
		return event;
	}

}
