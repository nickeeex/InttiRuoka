package fi.nickeee.inttiruoka;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

class BackGroundTask extends AsyncTask<Void, Void, String> {

	private static final String LOG = "fi.nickeee.inttiruoka";
    private RemoteViews views;
    private Context context;
    private AppWidgetManager appWidgetManager;
    private int[] allWidgetIds;
    private  int widgetId;
    
	public BackGroundTask(RemoteViews views, Context context, AppWidgetManager appWidgetManager, int[] allWidgetIds, int widgetId ) {
		this.views = views;
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		this.allWidgetIds = allWidgetIds;
		this.widgetId = widgetId;
	}
    @Override
    protected void onPreExecute() {
		Log.i(LOG, "BGTask PreExecute");
    	//Show spinning progress bar
        views.setViewVisibility(R.id.progressBar1, View.VISIBLE);
        
        //Hide text
        views.setTextViewText(R.id.update,"");
        
        //Update widget view
      	appWidgetManager.updateAppWidget(widgetId, views);  
        super.onPreExecute();
    }

    private boolean IsDateInString(String date)
    {
 	        Log.w(LOG,"IsDateInString "+ date);
    	    String[] finalDaySplit = date.split("-");
    	   // [1].trim().split(".");
            String fin = finalDaySplit[1].trim();
            String[] secondSplit = fin.split("\\.");
 	        DateTime dateLast = new DateTime(Integer.parseInt(secondSplit[2]),Integer.parseInt(secondSplit[1]),Integer.parseInt(secondSplit[0]), 0,0);
    	    dateLast.plusDays(7 - dateLast.dayOfWeek().get());
    	    DateTime dateFirst = dateLast.minusDays(6);
    	   
    	    DateTime now = DateTime.now();
    	    if(now.isAfter(dateFirst) && now.isBefore(dateLast))
    	      return true;
    	    else 
    	      return false;
    }
    
    @Override
    protected String doInBackground(Void... params) {
        try {
        	String result;
            String url= "http://www.leijonacatering.fi/ruokalista_varuskunta.php";
            Document doc =  Jsoup.parse(new URL(url).openStream(), "ASCII", url);
           
           if(IsDateInString(doc.childNode(1).childNode(2).childNode(1).childNode(1).childNode(1).childNode(3).childNode(0).childNode(0).childNode(0).childNode(0).toString())){
        	   Log.w(LOG,"true");
        	  result = ParseTodaysFood(doc);
           }
           else {
        	   Log.w(LOG,"false");
        	   url= "http://www.leijonacatering.fi/ruokalista_varuskunta_seur.php";
               doc =  Jsoup.parse(new URL(url).openStream(), "ASCII", url);
               if(IsDateInString(doc.childNode(1).childNode(2).childNode(1).childNode(1).childNode(1).childNode(3).childNode(0).childNode(0).childNode(0).childNode(0).toString())){
            	   Log.w(LOG,"true");
            
            	 result = ParseTodaysFood(doc);
               } else {
            	   Log.w(LOG,"Date Not found at all");
            	   result = "No food found";
               }
           }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String ParseTodaysFood(Document doc){
    	    DateTime todayTemp = DateTime.now();
    	    DateTime today = new DateTime(todayTemp.getYear(),todayTemp.getMonthOfYear(),todayTemp.getDayOfMonth(),0,0);
    	    int weekDay = ((today.getDayOfWeek()  + 1) * 2) -1;
    	    List<Node> lounasNodes = doc.childNode(1).childNode(2).childNode(1).childNode(1).childNode(7).childNodes();
    	    List<Node> paivallisNodes = doc.childNode(1).childNode(2).childNode(1).childNode(1).childNode(9).childNodes();
    	 
    	    Log.w(LOG,lounasNodes.get(weekDay).outerHtml());
    	    String lunch = lounasNodes.get(weekDay).outerHtml().replaceAll("<[^>]*>", ""); //.replaceAll("<[^>]*>"),"");
    	    lunch = lunch.replaceAll("&nbsp;","")+"\n\n";
    	   
    	    String dinner = paivallisNodes.get(weekDay).outerHtml().replaceAll("<[^>]*>", "");
    	    dinner = dinner.replaceAll("&nbsp;",""); 
    	    return ReplaceChars(lunch + dinner);
    }

    public String ReplaceChars(String input) {
    	
    	String result = input.replace("&auml;","ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö");	
    	return result;
    }
    
    @Override
    protected void onPostExecute(String result) {
     views.setViewVisibility(R.id.progressBar1, View.INVISIBLE);
     views.setTextViewText(R.id.update,
				 result);
     
     Intent clickIntent = new Intent(context.getApplicationContext(),
				MyWidgetProvider.class);

		clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				allWidgetIds);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context.getApplicationContext(), 0, clickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		views.setOnClickPendingIntent(R.id.update, pendingIntent);
		
		//Refresh widget layout from service
		appWidgetManager.updateAppWidget(widgetId, views);  
    }
}