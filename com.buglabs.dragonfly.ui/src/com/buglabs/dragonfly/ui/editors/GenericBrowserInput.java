package com.buglabs.dragonfly.ui.editors;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Input for GenericBrowserEditor
 * 
 * @author akravets
 * 
 */
public class GenericBrowserInput implements IEditorInput {

	private URL url;

	private String name = "editor";

	private String toolTip = "editor";

	public GenericBrowserInput(URL url) {
		this.url = url;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		return toolTip;
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		return url.toExternalForm();
	}

	// overriding equals so there is only one instance of the browser
	public boolean equals(Object obj) {
		if (obj instanceof GenericBrowserInput) {
			GenericBrowserInput input = (GenericBrowserInput) obj;

			if (input.getName().equals(this.getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Sets the name of this editor
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns name of this editor
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets tool tip value of this editor
	 * 
	 * @param toolTip
	 */
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	/**
	 * @return Returns tool tip value of this editor
	 */
	public String getToolTip() {
		return toolTip;
	}
}
