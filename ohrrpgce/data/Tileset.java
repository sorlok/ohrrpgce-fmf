package ohrrpgce.data;

import ohrrpgce.data.loader.MapParser;
import ohrrpgce.data.loader.TilesetParser;

public class Tileset {
	public int id;
	public /*int[][]*/ TileData tsData; //[tileID][pixY*pixX]
        private TileAnimation[] tileAnims;

	public Tileset(int id) {
		this.id = id;
                this.tileAnims = new TileAnimation[MapParser.TILE_ANIM_OFFSETS.length];
	}
        
        public void setTileAnimation(int id, TileAnimation anim) {
            tileAnims[id] = anim;
        }
        
        public TileAnimation getTileAnimation(int id) {
            return tileAnims[id];
        }
        
        
        /**
         * Move a tile within the tileset
         * @param id The id of the tile to move
         * @param dX, dY The number of tiles to move this right and down, respectively. A tile cannot be moved  beyond the boundary of the tileset.
         * @return The new id of this tile.
         */
        public static int moveTile(int id, int dX, int dY) {
            int res = id + dY*TilesetParser.TILE_COLS + dX;
            if (res >= TilesetParser.TILE_COLS*TilesetParser.TILE_ROWS)
                return TilesetParser.TILE_COLS*TilesetParser.TILE_ROWS;
            if (res < 0)
                return 0;
            return res;
        }
        

}
