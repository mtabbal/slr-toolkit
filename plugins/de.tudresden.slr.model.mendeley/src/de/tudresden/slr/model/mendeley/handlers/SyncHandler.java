package de.tudresden.slr.model.mendeley.handlers;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;

import de.tudresden.slr.model.mendeley.api.client.MendeleyClient;
import de.tudresden.slr.model.mendeley.synchronisation.WorkspaceBibTexEntry;
import de.tudresden.slr.model.mendeley.synchronisation.WorkspaceManager;
import de.tudresden.slr.model.mendeley.ui.MSyncWizard;
import de.tudresden.slr.model.mendeley.util.MutexRule;

/**
 * This Class implements the command handler for Updating a specific Bib-File that is connected to
 * a Mendeley Folder. Will be triggered by clicking the 'Update Folders' entry in the main menu
 * 'Mendeley -> Update Folders'. This command downloads the latest state of all connected Mendeley 
 * Folder and synchronizes their Bib-File content with the Folder content.
 * 
 * @author Johannes Pflugmacher
 * @version 1.0
 * @see org.eclipse.core.commands.AbstractHandler
 * @see org.eclipse.core.commands.IHandler
 */
public class SyncHandler  extends AbstractHandler{
	
	private final String ID = "de.tudresden.slr.model.mendeley.SyncFolders";
	
	private WorkspaceManager wm = WorkspaceManager.getInstance();
	
	private MendeleyClient mc = MendeleyClient.getInstance();
	
	/**
	 * A flag that returns if the user is logged into his Mendeley Profile
	 */
	private boolean loggedIn;
	private boolean isReady;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		/*
		 * To update everything automatically use
		 * 	updateAutomatically()
		 */
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		ArrayList<WorkspaceBibTexEntry> workspaceBibTexEntries = wm.getWorkspaceEntries();
		
		ArrayList jobs = new ArrayList();
		for(WorkspaceBibTexEntry entry : workspaceBibTexEntries) {
			jobs.addAll(this.getUpdateJobsWorkspaceBibTexEntries(entry, window));
			//this.updateWorkspaceBibTexEntries(entry, window);
		}
		
		MutexRule rule = new MutexRule();
       
        for(int i = 0; i < jobs.size(); i++) {
			if(jobs.get(i) instanceof Job) {
				((Job)jobs.get(i)).setRule(rule);
			}
			if(jobs.get(i) instanceof UIJob) {
				((UIJob)jobs.get(i)).setRule(rule);
			}
		}
        
		for(int i = 0; i < jobs.size(); i++) {
			if(jobs.get(i) instanceof Job) {
				((Job)jobs.get(i)).schedule();
			}
			if(jobs.get(i) instanceof UIJob) {
				((UIJob)jobs.get(i)).schedule();
			}
		}
		
		
		
		
		return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		
		/*
		 * set the popup menu entry to enabled if there is a MendeleyFolder for any File
		 * This is done in 3 steps:
		 * 	1.	Iterate over all WorkspaceBibTexEntry
		 * 	2.	Check if there is a MendeleyFolder assigned to one or more WorkspaceBibTexEntries
		 * 	3.	Enable command if there is any MendeleyFolder
		 */
		boolean activeLinks = false;
		for(WorkspaceBibTexEntry entry : wm.getWorkspaceEntries()) {
			if(entry != null) {
				if(entry.getMendeleyFolder() != null)
					activeLinks = true;
			}
		}
		
		if(activeLinks)
			setBaseEnabled(true);
		else
			setBaseEnabled(false);
		
		super.setEnabled(evaluationContext);
	}
	
	private void updateAutomatically() {
		loggedIn = false;
		/*
		 * Synchronizing all Folders/Files is separated in 2 jobs
		 * 	1.	Checking if the current login is valid
		 * 		-	if that's not the case a login screen will be displayed
		 * 	2.	Executing an updateSyncFolders method via the WorkspaceManager
		 */
		
		UIJob job1 = new UIJob("Validating Mendeley Login") {
            @Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
            	SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
            	subMonitor.subTask("Checking if Login is available");
            	if(mc.refreshTokenIfNecessary()) {
            		loggedIn = true;
            		
            	}
            	else {
            		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            		MessageDialog.openError(activeShell, "Error", "Login Failed. Wizard will be closed.");
            		return Status.CANCEL_STATUS;
            	}
            	
            	return Status.OK_STATUS;
            }
        };
		Job job2 = new Job("Updating") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
            	if(loggedIn)
            		wm.updateSyncFolders(monitor);
            	else
            		return Status.CANCEL_STATUS;
               
            	return Status.OK_STATUS;
            }
            

        };
    	// a rule is instantiated to oppress parallel execution of jobs
        MutexRule rule = new MutexRule();
        job1.setRule(rule);
        job2.setRule(rule);
        job2.setUser(true);
        job1.schedule();
        job2.schedule();
	}
	
	private ArrayList getUpdateJobsWorkspaceBibTexEntries(WorkspaceBibTexEntry entry, IWorkbenchWindow window) {
		loggedIn = false;
		isReady = false;
		ArrayList jobs = new ArrayList();
		
		if(entry != null) {
			if(entry.getMendeleyFolder() != null) {
				
				/*
				 * Synchronizing a Folder is separated in 2 jobs
				 * 	1.	Checking if the current login is valid
				 * 		-	if that's not the case a login screen will be displayed
				 * 	2.	Executing an updateSyncFolder method via the WorkspaceManager
				 */
				UIJob job1 = new UIJob("Validating Mendeley Login") {
		            @Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
		            	SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		            	subMonitor.subTask("Checking if Login is available");
		            	if(mc.refreshTokenIfNecessary()) {
		            		loggedIn = true;
		            		
		            	}
		            	else {
		            		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		            		MessageDialog.openError(activeShell, "Error", "Login Failed. Wizard will be closed.");
		            		return Status.CANCEL_STATUS;
		            	}
		            	
		            	return Status.OK_STATUS;
		            }
		        };
				Job job2 = new Job("Updating") {
		            @Override
		            protected IStatus run(IProgressMonitor monitor) {
		            	SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		            	subMonitor.setTaskName("Synchronizing " + entry.getUri().toString() + " with MendeleyFolder: " + entry.getMendeleyFolder().getName());
		            	if(loggedIn) {
		            		/*
		            		 * alternative update method:
		            		 * 
		            		 * wm.updateSyncFolder(entry, monitor);
		            		 * 
		            		 * used to automatically update synchronized Folder
		            		 */
							try {
								mc.updateMendeleyFolders(monitor);
							} catch (TokenMgrException | IOException | ParseException e) {
								e.printStackTrace();
							}
							
							
		            		isReady = true;
		            	}
		            	else
		            		return Status.CANCEL_STATUS;
		            	
		            	return Status.OK_STATUS;
		            }
		            

		        };
		        
		        UIJob job3 = new UIJob("Starting Mendeley UI Wizard") {
		            @Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
		            	// only executes if both previous job were successful
		            	if(loggedIn & isReady) {
		            		WizardDialog wizardDialog = new WizardDialog(window.getShell(),
						            new MSyncWizard(entry.getUri(), entry));
		            		wizardDialog.open();
		            		isReady = false;
		            	}
		            	else {
		            		return Status.CANCEL_STATUS;
		            	}
		            	return Status.OK_STATUS;
		            }
		        };
		        // a rule is instantiated to oppress parallel execution of jobs
		        
		        MutexRule rule = new MutexRule();
		        //job1.setRule(rule);
		        job1.setUser(true);
		        //job2.setRule(rule);
		        job2.setUser(true);
		        
		        //job3.setRule(rule);
		        jobs.add(job1);
		        jobs.add(job2);
		        jobs.add(job3);
		        //job1.schedule();
		        //job2.schedule();
		        //job3.schedule();
			}
		}
		return jobs;
	}
	
	
	

}
