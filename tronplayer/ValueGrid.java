import java.awt.Point;
import java.util.Arrays;

import path.finding.stuff.Mover;
import path.finding.stuff.TileBasedMap;

import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TileTypeEnum;
import com.orbischallenge.tron.client.api.TronGameBoard;

public class ValueGrid implements TileBasedMap {

	int[][] grid;
	boolean[][] visited;
	
	public ValueGrid(TronGameBoard board, LightCycle player, LightCycle opponent) {
		int l = board.length();
		grid = new int[l][l];
		visited = new boolean[l][l];
		
		for (int i = 0; i < l; i++) {
			Arrays.fill(grid[i], -1);
		}
		
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				TileTypeEnum posType = board.tileType(i, j);
				
				if (at(i, j) != -1)	
					continue;
				
				if (posType == TileTypeEnum.WALL || posType == TileTypeEnum.LIGHTCYCLE
						|| posType == TileTypeEnum.TRAIL) {
					set(i, j, 0);
					
					// mark all adjacent squares to opponent as dangerous
					// since we don't know which way they'll move
					if (!player.getPosition().equals(new Point(i, j)) && posType == TileTypeEnum.LIGHTCYCLE) {
						set(i-1, j, 0);
						set(i, j-1, 0);
						set(i+1, j, 0);
						set(i, j+1, 0);
					}
				} else {
					set(i, j, 1);
				}
			}
		}
	}
	
	public int at(int col, int row) {
		if (row < 0 || col < 0 || row >= grid.length || col >= grid.length) {
			return 0;
		}
		
		return grid[row][col];
	}
	
	public void set(int col, int row, int value) {
		if (row < 0 || col < 0 || row >= grid.length || col >= grid.length) {
			return;
		}
		
		grid[row][col] = value;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				result.append(at(j, i));
			}
			
			result.append("\n");
		}
		
		return result.toString();
	}

	@Override
	public int getWidthInTiles() {
		return grid.length;
	}

	@Override
	public int getHeightInTiles() {
		// TODO Auto-generated method stub
		return grid.length;
	}

	@Override
	public void pathFinderVisited(int x, int y) {
		// TODO Auto-generated method stub
		visited[x][y] = true;
	}

	@Override
	public boolean blocked(Mover mover, int x, int y) {
		return at(x, y) == 0;
	}

	@Override
	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		// TODO Auto-generated method stub
		return 1;
	}
}
