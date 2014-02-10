package fi.nickeee.inttiruoka;



import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService  extends Service {
	private static final String LOG = "fi.nickeee.inttiruoka";
	@Override
	public int onStartCommand(Intent intent,int flags, int startId) {

		Log.i(LOG, "onStart called");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int widgetId : allWidgetIds) {
	
			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_layout);
 
			new BackGroundTask(remoteViews,this.getApplicationContext(),appWidgetManager,allWidgetIds,widgetId).execute();
		} 
		stopSelf();
	   return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


}
