package org.openstreetmap.josm.plugins.vectorizer;

import org.openstreetmap.josm.data.preferences.IntegerProperty;

public interface Vectorizer {

	IntegerProperty PROP_IMAGE_FILTER_MEDIAN_SIZE = new IntegerProperty( "vectorizer.image.filter.median.size", 1 );
	IntegerProperty PROP_COLOR_AREA_SIZE = new IntegerProperty( "vectorizer.color.area.size", 3 );
	IntegerProperty PROP_COLOR_ELLIPSOID_EXPAND = new IntegerProperty( "vectorizer.color.ellipsoid.expand", 3 );

}
