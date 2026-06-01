package org.core.geometry;

import org.core.enums.TileType;
public class WorldGeometry {
    private final TileType[][] tiles;
    private final int widthTiles;
    private final int heightTiles;
    private final float tileSize;

    public WorldGeometry(TileType[][] tiles, float tileSize) {
        this.tiles = tiles;
        this.heightTiles = tiles.length;
        this.widthTiles = tiles[0].length;
        this.tileSize = tileSize;
    }

    public TileType getTile(int tileX, int tileY) {
        if (tileX < 0 || tileX >= widthTiles || tileY < 0 || tileY >= heightTiles) {
            return TileType.WALL;
        }
        return tiles[tileY][tileX];
    }

    public TileType getTileAt(float worldX, float worldY) {
        int tileX = (int) (worldX / tileSize);
        int tileY = (int) (worldY / tileSize);
        return getTile(tileX, tileY);
    }

    public int getWidthTiles() {
        return widthTiles;
    }

    public int getHeightTiles() {
        return heightTiles;
    }

    public float getTileSize() {
        return tileSize;
    }

    public TileType[][] getTiles() {
        return tiles;
    }
}


