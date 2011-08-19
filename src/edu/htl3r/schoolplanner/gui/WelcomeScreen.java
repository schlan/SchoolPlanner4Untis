/* SchoolPlanner4Untis - Android app to manage your Untis timetable
    Copyright (C) 2011  Mathias Kub <mail@makubi.at>
			Sebastian Chlan <sebastian@schoolplanner.at>
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package edu.htl3r.schoolplanner.gui;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.htl3r.schoolplanner.R;
import edu.htl3r.schoolplanner.SchoolPlannerApp;
import edu.htl3r.schoolplanner.backend.preferences.loginSets.LoginSetManager;
import edu.htl3r.schoolplanner.backend.preferences.loginSets.asyncUpdateTasks.LoginSetUpdateAsyncTask;
import edu.htl3r.schoolplanner.constants.LoginSetConstants;
import edu.htl3r.schoolplanner.constants.WelcomeScreenConstants;
import edu.htl3r.schoolplanner.gui.listener.LoginListener;
import edu.htl3r.schoolplanner.gui.welcomeScreen.WelcomeScreenContextMenu;

public class WelcomeScreen extends SchoolPlannerActivity{

	private final String nameKey = LoginSetConstants.nameKey;
	private final String urlKey = LoginSetConstants.serverUrlKey;
	private final String schoolKey = LoginSetConstants.schoolKey;
	private final String userKey = LoginSetConstants.usernameKey;

	private ProgressBar progressWheel;
	private TextView loginProgressText;
	private ListView mainListView;
	
	private LoginSetDialog dialog;
	
	private RelativeLayout mainLayout;
	private TextView emptyListTextView;
	
	private LoginSetManager loginmanager;
	
	private final int CONTEXT_MENU_ID = 1;
	
	private WelcomeScreenContextMenu contextMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_screen);
		
		progressWheel = (ProgressBar) findViewById(R.id.loginProgress);
		loginProgressText = (TextView) findViewById(R.id.loginProgressText);
		mainListView = (ListView) findViewById(R.id.loginList);
		
		buildEmptyListTextView();
		mainLayout = (RelativeLayout) findViewById(R.id.welcome_main_layout);
		
		loginmanager = new LoginSetManager();
		((SchoolPlannerApp) getApplication()).setLoginManager(loginmanager);
				
		// TODO: temporarily for easier login
		if(loginmanager.getAllLoginSets().size() <= 0)
			loginmanager.addLoginSet("WebUntis Testschule", "webuntis.grupet.at:8080", "demo", "user", "", false);
		
		registerForContextMenu(mainListView);
		
		initList();
		mainListView.setOnItemClickListener(new LoginListener(this));
		
		initContextMenu();
	}
	
	private void buildEmptyListTextView() {
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		        ViewGroup.LayoutParams.WRAP_CONTENT);

		// Set parent view
		p.addRule(RelativeLayout.BELOW, R.id.welcome_title);
		
		emptyListTextView = new TextView(this);
		emptyListTextView.setTextSize(14);
		emptyListTextView.setPadding(5, 0, 0, 0);
		emptyListTextView.setTextColor(getResources().getColor(R.color.text));
		emptyListTextView.setText(R.string.login_sets_list_empty);
		emptyListTextView.setLayoutParams(p);
		emptyListTextView.setTag(WelcomeScreenConstants.EMPTY_LIST_TEXTVIEW_ADDED);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.welcome_screen, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.add_login_set:
	    	dialog = new LoginSetDialog(this);
	    	dialog.setParent(this);
	    	dialog.show();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	

	public void editLoginSet(int id) {
		dialog = new LoginSetDialog(this,loginmanager.getLoginSetOnPosition(id));
    	dialog.setParent(this);
    	dialog.show();
	}

	private void initList(){
		
		List<Map<String, String>> allLoginSetsForListAdapter = loginmanager.getAllLoginSetsForListAdapter();
		
		if (allLoginSetsForListAdapter.size() <= 0) {
			emptyListTextView.setTag(WelcomeScreenConstants.EMPTY_LIST_TEXTVIEW_ADDED);
			mainLayout.addView(emptyListTextView);
		}
		else if(WelcomeScreenConstants.EMPTY_LIST_TEXTVIEW_ADDED.equals(emptyListTextView.getTag())) {
				emptyListTextView.setTag(WelcomeScreenConstants.EMPTY_LIST_TEXTVIEW_REMOVED);
				mainLayout.removeView(emptyListTextView);
		}
		
		String [] list_keys = new String[] {nameKey, urlKey, schoolKey, userKey};
		int [] list_ids =  new int[] {R.id.txt_name, R.id.txt_url, R.id.txt_school, R.id.txt_user};
		
		SimpleAdapter aa = new SimpleAdapter(this, allLoginSetsForListAdapter, R.layout.login_table_row, list_keys, list_ids);
		
		mainListView.setAdapter(aa);
	}
	
	public void setInProgress(String message, boolean active) {
		setLoginListEnabled(!active);
		if (active) {
			loginProgressText.setText(message);
			progressWheel.setVisibility(View.VISIBLE);
		} else {
			loginProgressText.setText("");
			progressWheel.setVisibility(View.INVISIBLE);
		}
	}
	
	public void loginSetListUpdated() {
		initList();
		((SimpleAdapter)(mainListView.getAdapter())).notifyDataSetChanged();
	}
	
	public LoginSetManager getLoginManager(){
		return loginmanager;
	}

	private void setLoginListEnabled(boolean enabled) {
		mainListView.setEnabled(enabled);
		if (enabled) {
			mainListView.setBackgroundResource(R.color.element_background);
		}
		else {
			mainListView.setBackgroundResource(R.color.disabled_element_background);
		}
	}

	
	/**
	 * @param name
	 * @param serverUrl
	 * @param school
	 * @param username
	 * @param password
	 * @param sslOnly
	 * @return 'true', wenn das Speichern erfolgreich war, 'false', wenn es zum Namen des Login-Sets schon ein anderes Login-Set gibt.
	 */
	public boolean addLoginSet(final String name, final String serverUrl, final String school,
			final String username, final String password, final boolean sslOnly) {
		LoginSetUpdateAsyncTask task = new LoginSetUpdateAsyncTask(this) {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				return loginmanager.addLoginSet(name, serverUrl, school, username, password, sslOnly);
			}
		};
		task.execute();
		
		try {
			return task.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void removeLoginSet(final int id) {
		LoginSetUpdateAsyncTask task = new LoginSetUpdateAsyncTask(this) {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				loginmanager.removeLoginEntry(id);
				return true;
			}
		};
		  task.execute();
		
	}

	public void editLoginSet(final String name, final String serverUrl, final String school,
			final String username, final String password, final boolean checked) {
		LoginSetUpdateAsyncTask task = new LoginSetUpdateAsyncTask(this) {
			@Override
			protected Boolean doInBackground(Void... params) {
				loginmanager.editLoginSet(name, serverUrl, school, username, password, checked);
				return true;
			}
		};
		
		task.execute();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID) {
			return contextMenu.createMenu(getString(R.string.context_menu_loginsets));
		}
		return super.onCreateDialog(id);
	}
	
	private void initContextMenu() {
		contextMenu = new WelcomeScreenContextMenu(this,CONTEXT_MENU_ID);
		contextMenu.setListView(mainListView);
		contextMenu.init();
	}
	
}