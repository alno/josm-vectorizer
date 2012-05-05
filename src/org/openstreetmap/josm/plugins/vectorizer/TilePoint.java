package org.openstreetmap.josm.plugins.vectorizer;

import java.awt.Point;

public class TilePoint {

	public final int tileX, tileY;
	public final int offsetX, offsetY;

	public TilePoint( int tileX, int tileY, int offsetX, int offsetY ) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public TilePoint( Point tile, Point offset ) {
		this.tileX = tile.x;
		this.tileY = tile.y;
		this.offsetX = offset.x;
		this.offsetY = offset.y;
	}

	@Override
	public int hashCode() {
		return 31 * offsetX + 13 * offsetY + 17 * tileX + 11 * tileY;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;

		TilePoint other = (TilePoint) obj;

		if ( offsetX != other.offsetX )
			return false;
		if ( offsetY != other.offsetY )
			return false;
		if ( tileX != other.tileX )
			return false;
		if ( tileY != other.tileY )
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TilePoint{" + tileX + ", " + tileY + " | " + offsetX + ", " + offsetY + "}";
	}

}
