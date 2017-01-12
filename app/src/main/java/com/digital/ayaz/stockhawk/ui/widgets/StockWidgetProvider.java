package com.digital.ayaz.stockhawk.ui.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.data.Constants;

public class StockWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = StockWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int widgetId : appWidgetIds) {
            Intent intent = new Intent(context, StockWidgetRemoteViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_info_widget);
            views.setRemoteAdapter(R.id.widget_list_view, intent);

    /*        // Open Graph on List Item click
			Intent intentStockGraph = new Intent(context, GraphActivity.class);
			PendingIntent pendingIntent = TaskStackBuilder.create(context)
					.addNextIntentWithParentStack(intentStockGraph)
					.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
		views.setPendingIntentTemplate(R.id.widget_list_view,pendingIntent);*/

            // Update Widget on HomeScreen
            appWidgetManager.updateAppWidget(widgetId, views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);
        if (intent.getAction().equals(Constants.ACTION_STOCK_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        }
    }
}
