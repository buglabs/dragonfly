package com.buglabs.dragonfly.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.buglabs.dragonfly.DragonflyActivator;

public class BaseNodeResouceAdapter implements IResource {

	private final BaseNode baseNode;

	public BaseNodeResouceAdapter(BaseNode baseNode) {
		this.baseNode = baseNode;		
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean contains(ISchedulingRule rule) {	
		return false;
	}

	public boolean isConflicting(ISchedulingRule rule) {
		return false;
	}

	public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {	
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void accept(IResourceVisitor visitor) throws CoreException {		
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {		
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public IMarker createMarker(String type) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public IResourceProxy createProxy() {		
		return null;
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DragonflyActivator.PLUGIN_ID, "Unimplemented."));
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		// TODO Auto-generated method stub

	}

	public boolean exists() {		
		return true;
	}

	public IMarker findMarker(long id) throws CoreException {
		return null;
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		return null;
	}

	public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
		return 0;
	}

	public String getFileExtension() {
		return "bug";
	}

	public IPath getFullPath() {
		return null;
	}

	public long getLocalTimeStamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IPath getLocation() {
	
		return null;
	}

	public URI getLocationURI() {
	
		try {
			return new URI("bug://" + baseNode.getName());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public IMarker getMarker(long id) {

		return null;
	}

	public long getModificationStamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getName() {
		return baseNode.getName();
	}

	public IPathVariableManager getPathVariableManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public IContainer getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getPersistentProperties() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath getProjectRelativePath() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath getRawLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getRawLocationURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceAttributes getResourceAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getSessionProperties() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IWorkspace getWorkspace() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAccessible() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDerived() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isDerived(int options) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isHidden() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isHidden(int options) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isLinked() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isLinked(int options) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLocal(int depth) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPhantom() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isSynchronized(int depth) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTeamPrivateMember() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTeamPrivateMember(int options) {
		// TODO Auto-generated method stub
		return false;
	}

	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void revertModificationStamp(long value) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setDerived(boolean isDerived) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setHidden(boolean isHidden) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public long setLocalTimeStamp(long value) throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub

	}

	public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

}
