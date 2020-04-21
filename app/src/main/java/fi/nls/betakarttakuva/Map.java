package fi.nls.betakarttakuva;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.HillshadeLayer;
import com.mapbox.mapboxsdk.style.sources.RasterDemSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.hillshadeHighlightColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.hillshadeShadowColor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;


public class Map extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    public static final String FLYTO = "fi.nls.betakarttakuva.Map.FLYTO";

    private final java.util.Map<String, String> styleUrls = new HashMap<>();
    private String styleUrl ;

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private MapView mapView;
    private MapboxMap mapboxMap;

    private Toolbar toolbar;

    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private boolean hasLocation = false;
    private boolean isLocationEnabled = false;


    private static final String LAYER_ID = "hillshade-layer";
    private static final String SOURCE_ID = "hillshade-source";
    private static final String SOURCE_URL = "mapbox://mapbox.terrain-rgb";
    private static final String HILLSHADE_HIGHLIGHT_COLOR = "#008924";



    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle, int mode) {
        if (isLocationEnabled) {
            return;
        }
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponentOptions customLocationComponentOptions =
                    LocationComponentOptions.builder(this)
                            .elevation(5)
                            .accuracyAlpha(.6f)
                            .accuracyColor(Color.RED)
                            .build();

            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    this, loadedMapStyle, customLocationComponentOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(mode);
            locationComponent.setRenderMode(RenderMode.COMPASS);


            isLocationEnabled = true;
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            hasLocation = granted;
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style, CameraMode.TRACKING);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        styleUrl = getString(R.string.bk_defaultStyleUrl);

        styleUrls.put("hobby", styleUrl);
        styleUrls.put("next", getString(R.string.bk_nextStyleUrl));
        styleUrls.put("current", getString(R.string.bk_currentStyleUrl));

        Mapbox.getInstance(this,
                getString(R.string.mb_accesstoken));
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        return true;
    }


    @Override
    @SuppressWarnings({"MissingPermission"})
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                handleLocation();
                return true;
            case R.id.action_style_hobby:
                changeStyle(styleUrls.get("hobby"));
                return true;

            case R.id.action_style_next:
                changeStyle(styleUrls.get("next"));
                return true;

            case R.id.action_style_current:
                changeStyle(styleUrls.get("current"));
                return true;

            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void handleLocation() {
        if (hasLocation) {

            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setLocationComponentEnabled(true);

            Location loc = locationComponent.getLastKnownLocation();

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude())) // Sets the new camera position
                    .zoom(14) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 1500);
        } else {
            enableLocationComponent(
                    mapboxMap.getStyle(), CameraMode.TRACKING);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Map.this.mapboxMap = mapboxMap;
        changeStyle(styleUrl);
    }

    public void changeStyle(String styleUrl) {
        if (mapboxMap == null) {
            return;
        }
        if (styleUrl == null) {
            return;
        }

        mapboxMap.setStyle(
                styleUrl,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style, CameraMode.TRACKING);

                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (!FLYTO.equals(intent.getAction())) {
            return;
        }

        final String json = intent.getStringExtra(FLYTO);

        try {
            JSONObject loc = new JSONObject(json);
            JSONArray posArr = loc.getJSONArray("coordinates");

            locationComponent.setLocationComponentEnabled(false);
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(posArr.getDouble(1), posArr.getDouble(0))) // Sets the new camera position
                    .zoom(14) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(position));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}