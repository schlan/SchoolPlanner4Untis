package edu.htl3r.schoolplanner.gui.basti.Eyecandy;

import edu.htl3r.schoolplanner.R;
import edu.htl3r.schoolplanner.gui.basti.GUIData.GUILessonContainer;
import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class WeekOverlay extends Dialog{

	
	private GUILessonContainer lessons;
	private TextView t;
	public WeekOverlay(Context context) {
		super(context);
		setContentView(R.layout.week_overlay);
		setCanceledOnTouchOutside(true);
		
		t = new TextView(this.getContext());
		addContentView(t, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

		setTitle(context.getString(R.string.timetable_overlay_title));
	}
	
	public void setLessons(GUILessonContainer gl){
		lessons = gl;
		t.setText(lessons.getStart() + " - " + lessons.getEnd() + 
				"\nAnzahl der Stunden regulaeren Stunden: " + lessons.getStandardLessons().size()+
				"\nAnzahl der Stunden irregulaeren Stunden: " + lessons.getSpecialLessons().size()+
				"\nAnzahl der Stunden regulaeren langen Stunden: " + lessons.getExtraLongLessons().size()+
				"\nAnzahl der Stunden itregulaeren langen Stunden: " + lessons.getExtraLongSpecialLessons().size());
	}

}
