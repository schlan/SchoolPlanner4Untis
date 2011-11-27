package edu.htl3r.schoolplanner.gui.timetable.baactionbar;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.htl3r.schoolplanner.R;
import edu.htl3r.schoolplanner.backend.schoolObjects.ViewType;

public class BastisAwesomeActionBar extends RelativeLayout {

	public final static int REFRESH = 1;
	public final static int MONTH = 2;
	public final static int HOME = 3;
	public final static int DROPDOWN = 4;
	public final static int LIST_CLASS = 41;
	public final static int LIST_TEACHER = 42;
	public final static int LIST_ROOMS = 43;
	public final static int LIST_SUBJECTS = 44;

	private BADropdown dropdown;
	private BAAction month;
	private BAHomeAction home;
	private BAAction dropdown_action;
	private BAAction refresh;
	private TextView title;
	private View everything;

	private ArrayList<BAActoinBarEvent> actionbarevent = new ArrayList<BastisAwesomeActionBar.BAActoinBarEvent>();

	public BastisAwesomeActionBar(Context context) {
		super(context);
	}

	public BastisAwesomeActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BastisAwesomeActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void init(BADropdown dropdown, View everything) {
		this.dropdown = dropdown;
		this.everything = everything;
		title = (TextView) findViewById(R.id.baactionbar_title);
		setHomeIcon();
		setMonthIcon();
		setDropDownIcon();
		setRefreshIcon();
		initActionListenerDropDown();
	}

	private void setHomeIcon() {
		home = (BAHomeAction) findViewById(R.id.baactionbar_home);
		home.setIcon(getResources().getDrawable(R.drawable.logo));
		home.initProgressBar();

		home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDropDown();
				fireActionBarEvent(HOME);
			}
		});
	}

	private void setMonthIcon() {
		month = (BAAction) findViewById(R.id.baactionbar_month);
		month.setIcon(getResources().getDrawable(R.drawable.ic_actionbar_month));

		month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeDropDown();
				fireActionBarEvent(MONTH);
			}
		});
	}

	private void setDropDownIcon() {
		dropdown_action = (BAAction) findViewById(R.id.baactionbar_dropdown_action);
		dropdown_action.setIcon(getResources().getDrawable(R.drawable.ic_actionbar_dropdown_list));
	}

	private void setRefreshIcon() {
		refresh = (BAAction) findViewById(R.id.baactionbar_refresh);
		refresh.setIcon(getResources().getDrawable(R.drawable.ic_actionbar_refresh));

		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDropDown();
				fireActionBarEvent(REFRESH);
			}
		});
	}

	private void initActionListenerDropDown() {
		dropdown_action.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dropdown.setVisibility((dropdown.isShown()) ? View.GONE : View.VISIBLE);
			}
		});

		everything.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					closeDropDown();
				return false;
			}
		});
	}

	public void setText(ViewType vt) {
		title.setText(vt.getName());
	}

	public void setProgress(boolean active) {
		home.startProgressBar(active);
	}

	public void closeDropDown() {
		dropdown.setVisibility((dropdown.isShown()) ? View.GONE : View.GONE);
	}

	public void addBAActionBarEvent(BAActoinBarEvent listener) {
		actionbarevent.add(listener);
	}

	public void removeBAActionBarEvent(BAActoinBarEvent listener) {
		actionbarevent.remove(listener);
	}

	public void fireActionBarEvent(int ID) {
		for (BAActoinBarEvent a : actionbarevent) {
			a.onBAActionbarActionClicked(ID);
		}
	}

	public interface BAActoinBarEvent {
		public void onBAActionbarActionClicked(int ID);
	}
}
