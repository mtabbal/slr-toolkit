package de.tudresden.slr.model.mendeley.ui;

import org.eclipse.swt.widgets.Label;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;

import de.tudresden.slr.model.mendeley.Activator;
import de.tudresden.slr.model.mendeley.api.client.MendeleyClient;

/**
 * This class implements a Dialog used for displaying the Mendeley
 * authorization screen.
 * 
 * @author Johannes Pflugmacher
 * @version 1.0
 * @see org.eclipse.jface.dialogs.Dialog
 */
public class MendeleyOAuthDialog extends Dialog {
	MendeleyClient mendeley_client = MendeleyClient.getInstance();

	/**
	 * Preference Store to update latest login events
	 */
	private IPreferenceStore store;
	
    public MendeleyOAuthDialog(Shell parentShell) {
        super(parentShell);
        this.store = Activator.getDefault().getPreferenceStore();
        
    }

    @Override
    protected Control createDialogArea(Composite parent) {
    	try {
    		Composite container = (Composite) super.createDialogArea(parent);
        	GridLayout gridLayout = (GridLayout) container.getLayout();
        	gridLayout.marginWidth = 0;
        	gridLayout.marginHeight = 0;
        	Browser browser;
    		browser = new Browser(container, SWT.NONE);
    		GridData gd_browser = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
            gd_browser.heightHint = 439;
            gd_browser.widthHint = 644;
            browser.setLayoutData(gd_browser);
            
            browser.addLocationListener(new LocationListener() {
    			
    			@Override
    			public void changing(LocationEvent event) {
    				
    			}
    			
    			@Override
    			public void changed(LocationEvent event) {
    				URL aURL;
    				
    				try {
    					if(event.location != "") {
    						aURL = new URL(event.location);
        					// if login was successful the redirection will change the location to 'localhost'
        					if(aURL.getHost().equals("localhost")){
        						// Capture the authorization code
        						String auth_code = aURL.getQuery().replaceAll("code=", "");
        						mendeley_client.setAuth_code(auth_code);
        						mendeley_client.requestAccessToken(auth_code);
        					        
        					    close();
        					}
    					}
    					else {
    						browser.setUrl(mendeley_client.getAuthURL());
    					}
    				} catch (MalformedURLException e) {
    					e.printStackTrace();
    				} catch (IOException e) {
    					e.printStackTrace();
    				} catch (TokenMgrException e) {
    					e.printStackTrace();
    				} catch (ParseException e) {
    					e.printStackTrace();
    				}
    			}
    		});
    		
    		//browser.setUrl("https://api.mendeley.com/oauth/authorize?client_id=6710&redirect_uri=https:%2F%2Flocalhost&response_type=code&scope=all");
            browser.setUrl(mendeley_client.getAuthURL());
            return container;
    	}catch(SWTError e) {
    		Composite container = (Composite) super.createDialogArea(parent);
    		GridLayout gl_container = new GridLayout(1, false);
    		gl_container.horizontalSpacing = 3;
    		container.setLayout(gl_container);
    		
    		final Color myColor = new Color(container.getDisplay(), 255, 0, 0);
    		
    		Label error_title = new Label(container, SWT.NONE);
    		error_title.setForeground(myColor);
    		error_title.setText("Error: Something went wrong with your browser widget!\n\n");
    		
    		Label lblNewLabel = new Label(container, SWT.NONE);
    		lblNewLabel.setText("Please follow these steps to login into your Mendeley Account:\n\n" + 
    				"1. Copy and paste the link below to your browser's address bar\n" + 
    				"2. Put in your login credentials\n" + 
    				"3. After successfull authentication: replace link below with the \n    redirected URL from your browser's adress bar\n" + 
    				"4. Press ok to finish\n");
    		
    		Text text = new Text(container, SWT.BORDER);
    		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    		text.setText("https://api.mendeley.com/oauth/authorize?client_id=4335&redirect_uri=https:%2F%2Flocalhost&response_type=code&scope=all");
    		Button btnNewButton = new Button(container, SWT.NONE);
    		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    		btnNewButton.setText("ok");
    		
    		Label input_error = new Label(container, SWT.NONE);
    		input_error.setForeground(myColor);
    		input_error.setText("");

    		
    		btnNewButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					URL aURL;
					
					try {
						aURL = new URL(text.getText());
						// if login was successful the redirection will change the location to 'localhost'
						if(aURL.getHost().equals("localhost")){
							// Capture the authorization code
							String auth_code = aURL.getQuery().replaceAll("code=", "");
							mendeley_client.setAuth_code(auth_code);
							mendeley_client.requestAccessToken(auth_code);
						        
						    close();
						}
						else {
							input_error.setText("Error: Input Link must look like this https://localhost/?code=xyz");
							container.layout();
						}
					} catch (TokenMgrException | IOException | ParseException e1) {
						input_error.setText("Error: Input Link must look like this https://localhost/?code=xyz");
						container.layout();
						e1.printStackTrace();
					} 
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});

    		return container;
    		/**
    		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
    	            "Error: Login Dialog can't be displayed", 
    	            "Please follow these steps to login into your Mendeley Account:\n\n"
    	            + "1. Copy and paste the link below to your browser's address bar\n"
    	            + "2. Put in your login credentials\n"
    	            + "3. After successfull authentication: copy redirected URL from your browser's adress bar to the input field below\n"
    	            + "4. Press ok to finish\n", 
    	            "https://api.mendeley.com/oauth/authorize?client_id=4335&redirect_uri=https:%2F%2Flocalhost&response_type=code&scope=all", 
    	            null);
    		
	        if (dlg.open() == Window.OK) {
	          // User clicked OK; update the label with the input
	        	URL aURL;
				
				try {
					aURL = new URL(dlg.getValue());
					// if login was successful the redirection will change the location to 'localhost'
					if(aURL.getHost().equals("localhost")){
						// Capture the authorization code
						String auth_code = aURL.getQuery().replaceAll("code=", "");
						mendeley_client.setAuth_code(auth_code);
						mendeley_client.requestAccessToken(auth_code);
					        
					    close();
					}
				} catch (TokenMgrException | IOException | ParseException e1) {
					e1.printStackTrace();
				}
	        }
	        else {
	        	close();
	        }
	        */
    	}
    	
        
        

    }

    // overriding this methods allows you to set the
    // title of the custom dialog
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Mendeley Login");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(650, 475);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	GridLayout layout = (GridLayout)parent.getLayout();
    	layout.marginHeight = 0;
    }

}