package fi.nls.betakarttakuva;

import android.content.SearchRecentSuggestionsProvider;

public class GeocodingSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "fi.nls.betakarttakuva.GeocodingSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public GeocodingSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}