package fi.nls.betakarttakuva;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.*;


public class SearchResults extends ListActivity {

    final List<String> titles = new ArrayList<>();
    final List<double[]> points = new ArrayList<>();
    ArrayAdapter<String> adapter;
    RequestQueue queue;
    GeocodingResponseListener responseListener;
    private String geocoding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        geocoding = getString(R.string.bk_geocoding);
        queue = Volley.newRequestQueue(this);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, titles);
        responseListener = new GeocodingResponseListener(adapter, titles, points);

        setListAdapter(adapter);
        handleIntent(getIntent());
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        double[] posArr = points.get(position);

        if( posArr == null) {
            return;
        }

        final String payload = String.format("{ \"title\": \"%s\", \"coordinates\" : [%s,%s]}",
                item, Double.toString(posArr[0]), Double.toString(posArr[1]));

        Intent sendIntent = new Intent();
        sendIntent.setAction(Map.FLYTO);
        sendIntent.setType("application/json");
        sendIntent.putExtra(Map.FLYTO, payload);

        startActivityFromChild(this, sendIntent, -1);

    }

    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            geocode(query);

        }
    }

    private void clearTitles() {
        titles.clear();
        points.clear();
        adapter.notifyDataSetChanged();
    }

    private void geocode(String text) {

        clearTitles();

        final String url;
        try {
            url = geocoding + URLEncoder.encode(text, UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
           return;
        }

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        responseListener, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                });

        queue.add(jsonObjectRequest);

    }


}