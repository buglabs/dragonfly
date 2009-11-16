/*******************************************************************************
 * Copyright (c) 2006, 2007 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/

package com.buglabs.dragonfly.ui.views.mybugs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.buglabs.dragonfly.model.DiscoveredBugConnection;
import com.buglabs.dragonfly.model.FolderNode;
import com.buglabs.dragonfly.model.IModelNode;
import com.buglabs.dragonfly.model.Module;
import com.buglabs.dragonfly.model.ProgramNode;
import com.buglabs.dragonfly.model.ServiceNode;
import com.buglabs.dragonfly.model.StaticBugConnection;
import com.buglabs.dragonfly.model.VirtualBUGConnection;
import com.buglabs.dragonfly.ui.Activator;

public class BugLabelProvider implements ILabelProvider {

	private ISharedImages shared;

	public BugLabelProvider() {
		shared = PlatformUI.getWorkbench().getSharedImages();
	}

	public Image getImage(Object element) {
		if (element instanceof FolderNode) {
			return shared.getImage(ISharedImages.IMG_OBJ_FOLDER);
		}

		if (element instanceof Module) {
			String name = ((Module) element).getName().toLowerCase();

			if (name.equals("camera")) {
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_CAMERA);
			} 
			else if (name.equals("gps")) {
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_GPS);
			} 
			else if (name.equals("lcd")) {
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_LCD);
			}
			else if(name.equals("motion")){
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_MOTION);
			}
			else if(name.equals("audio")){
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_SOUND);
			}
			else if(name.equals("vonhippel")){
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_VH);
			} 
			else if (name.equals("wifi-bluetooth")) {
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_WIFI);
			}
			else if (name.equals("bugbee")) {
				return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_M_BUGBEE);
			}
			
			// return shared.getImage(ISharedImages.IMG_OBJ_ELEMENT);
			// return generic modules icon instead of generic eclipse icon
			return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_MODULE);
		}

		if (element instanceof ProgramNode) {
			return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_BUGVIEW_APPLICATION);
		}
		else if (element instanceof ServiceNode) {
			return Activator.getDefault().getImageRegistry().get(Activator.IMAGE_COLOR_SERVICES);
		}
		else if (element instanceof VirtualBUGConnection) {
			return Activator.getDefault().getImageRegistry().get(Activator.ICON_VIRTUAL_BUG);
		}
		else if(element instanceof StaticBugConnection){
			return Activator.getDefault().getImageRegistry().get(Activator.ICON_STATIC_BUG);
		}
		else if(element instanceof DiscoveredBugConnection){
			return Activator.getDefault().getImageRegistry().get(Activator.ICON_DISCOVERED_BUG);
		}

		return null;
	}

	public String getText(Object element) {
		if (element instanceof IModelNode) {
			return ((IModelNode) element).getName();
		}
		return ""; //$NON-NLS-1$
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
