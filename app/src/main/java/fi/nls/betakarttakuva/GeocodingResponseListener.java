package fi.nls.betakarttakuva;

import android.widget.ArrayAdapter;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class GeocodingResponseListener implements Response.Listener<JSONObject> {
    List<String> titles;
    List<double[]> points;
    ArrayAdapter adapter;

    public GeocodingResponseListener(
            ArrayAdapter adapter2,
            List<String> titles, List<double[]> points) {
        this.adapter = adapter2;
        this.titles = titles;
        this.points = points;


    }

    public void notifyResponses() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResponse(JSONObject response) {

        JSONArray features = null;
        try {
            features = response.getJSONArray("features");
            if (features == null) {
                return;
            }


            for (int i = 0; i < features.length(); i++) {
                JSONObject feat = features.getJSONObject(i);
                if (feat == null) {
                    continue;
                }

                JSONObject props = feat.getJSONObject("properties");
                if (props == null) {
                    continue;
                }

                String label = props.getString("label");
                if (label == null) {
                    label = "";
                }


                String municipality = props.getString("label:municipality");
                if (municipality == null) {
                    municipality = "";
                }

                String placeType = props.getString("label:placeType");
                if (placeType == null) {
                    placeType = "";
                }

                String labelText = String.format("%s %s (%s)", label, municipality, placeType);

                double[] posArr = null;

                JSONObject geom = feat.getJSONObject("geometry");
                if (geom != null) {
                    JSONArray posJsonArr = geom.getJSONArray("coordinates");
                    posArr = new double[]{posJsonArr.getDouble(0),
                            posJsonArr.getDouble(1)};
                }

                if (posArr != null) {
                    titles.add(labelText);
                    points.add(posArr);
                }

            }

            notifyResponses();


        } catch (JSONException e) {

        }


    }
}
