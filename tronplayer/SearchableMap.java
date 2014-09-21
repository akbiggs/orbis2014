import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import path.finding.stuff.Mover;
import path.finding.stuff.TileBasedMap;

import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TileTypeEnum;
import com.orbischallenge.tron.client.api.TronGameBoard;
import com.orbischallenge.tron.protocol.TronProtocol.Direction;

public class SearchableMap implements TileBasedMap {
	private static final int SEARCH_THRESHOLD = 6;
	private static final int EMPTY_SPACE_WEIGHT = 5;
	private static final int POWERUP_WEIGHT = 20;

	ValueMap map;
	
	public SearchableMap(TronGameBoard board, LightCycle player, LightCycle opponent) {
		map = new ValueMap(board);
		
		int l = map.length();
		
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				TileTypeEnum posType = board.tileType(i, j);
				
				if (map.valueAt(i, j) != -1)	
					continue;
			
				if (posType == TileTypeEnum.POWERUP) {
					map.setValue(i, j, POWERUP_WEIGHT);
				}
				else if (posType == TileTypeEnum.WALL || posType == TileTypeEnum.LIGHTCYCLE
						|| posType == TileTypeEnum.TRAIL) {
					map.setValue(i, j, 0);
					
					// mark all adjacent squares to opponent as dangerous
					// since we don't know which way they'll move
					if (!player.getPosition().equals(new Point(i, j)) && posType == TileTypeEnum.LIGHTCYCLE) {
						map.setValue(i-1, j, 0);
						map.setValue(i, j-1, 0);
						map.setValue(i+1, j, 0);
						map.setValue(i, j+1, 0);
					}
				} else {
					map.setValue(i, j, EMPTY_SPACE_WEIGHT);
				}
			}
		}
		
		List<Point> opponentAdjacents = adjacents(opponent.getPosition().x, opponent.getPosition().y);
		List<Point> playerAdjacents = adjacents(player.getPosition().x, player.getPosition().y);

		for (Point p1 : playerAdjacents) {
			int estimatedValue = estimateValueOf(p1.x, p1.y);
			if (estimatedValue == 0) {
				map.setValue(p1.x, p1.y, 0);
				continue;
			}
			
			int worstSpotsOwned = l*l+1;
			
			for (Point p2 : opponentAdjacents) {
				int spotsOwned = getNumberOfBeatableSpotsFrom(p1.x, p1.y, p2.x, p2.y);
				
				if (spotsOwned < worstSpotsOwned) {
					worstSpotsOwned = spotsOwned;
				}
			}
			
			map.setValue(p1.x, p1.y, worstSpotsOwned + estimatedValue);
		}
		
		//Going straight should be worth more
		Direction dir = player.getDirection();
		final double multiplier = 1.1;
		switch (dir)
		{
		case DOWN:
			for (int y = player.getPosition().y; y < map.length(); y++)
				map.multiplyValue(player.getPosition().x, y, multiplier);
			break;
		case UP:
			for (int y = player.getPosition().y; y >= 0; y--)
				map.multiplyValue(player.getPosition().x, y, multiplier);
			break;
		case LEFT:
			for (int x = player.getPosition().x; x < map.length(); x++)
				map.multiplyValue(x, player.getPosition().y, multiplier);
			break;
		case RIGHT:
			for (int x = player.getPosition().x; x >= 0; x--)
				map.multiplyValue(x, player.getPosition().y, multiplier);
			break;
		default:
			break;
		}
	}
	
	private List<Point> adjacents(int x, int y) {
		List<Point> results = new ArrayList<Point>();
		
		if (map.valueAt(x-1, y) != 0) {
			results.add(new Point(x-1,y));
		}
		
		if (map.valueAt(x+1, y) != 0) {
			results.add(new Point(x+1,y));
		}
		
		if (map.valueAt(x, y-1) != 0) {
			results.add(new Point(x,y-1));
		}
		
		if (map.valueAt(x, y+1) != 0) {
			results.add(new Point(x,y+1));
		}
		
		return results;
	}
	
	private int manhattenDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}
	
	public int estimateValueOf(int x, int y) {
		return estimateValueOf(x, y, SEARCH_THRESHOLD, new ArrayList<Point>());
	}
	
	public int estimateValueOf(int x, int y, int threshold, List<Point> searched) {
		
		if (map.valueAt(x, y) == 0) {
			return 0;
		}
		
		if (threshold == 0) {
			return map.valueAt(x, y);
		}
		
		List<Point> newSearched = new ArrayList<Point>(searched);
		newSearched.add(new Point(x, y));
		
		List<Point> adjs = adjacents(x, y);
		int estimatedValue = 0;
		
		for (Point p : adjs) {
			if (searched.contains(p)) {
				continue;
			}
			
			estimatedValue += estimateValueOf(p.x, p.y, threshold-1, newSearched);
		}
		
		return estimatedValue;
	}
	
	private int getNumberOfBeatableSpotsFrom(int x, int y, int opponentX, int opponentY) {
		int numBeatable = 0;
		
		for (int i = 0; i < map.length(); i++) {
			for (int j = 0; j < map.length(); j++) {
				if (map.valueAt(i, j) == 0)
					continue;
				
				int playerDistance = manhattenDistance(x, y, i, j);
				int opponentDistance = manhattenDistance(opponentX, opponentY, i, j);
				
				if (playerDistance < opponentDistance) {
					numBeatable++;
				}
			}
		}
		
		return numBeatable;
	}
	
	public Point getDestination() {
		return getBestPosition();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < map.length(); i++) {
			for (int j = 0; j < map.length(); j++) {
				result.append(map.valueAt(j, i));
				result.append(",");
			}
			
			result.append("\n");
		}
		
		return result.toString();
	}
	
	public Point getBestPosition() {
		double bestScore = -1;
		int bestX = 0;
		int bestY = 0;
		
		for (int i = 0; i < map.length(); i++) {
			for (int j = 0; j < map.length(); j++) {
				if (map.valueAt(i, j) > bestScore) {
					bestX = i;
					bestY = j;
					bestScore = map.valueAt(i, j);
				}
			}
		}
		
		return new Point(bestX, bestY);
	}

	@Override
	public int getWidthInTiles() {
		return map.length();
	}

	@Override
	public int getHeightInTiles() {
		return map.length();
	}

	@Override
	public void pathFinderVisited(int x, int y) {
		map.visited[x][y] = true;
	}

	@Override
	public boolean blocked(Mover mover, int x, int y) {
		return map.valueAt(x, y) == 0;
	}

	@Override
	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		return (new Double(map.valueAt(tx, ty))).floatValue();
	}
	
	private class ValueMap
	{
		double[][] grid;
		boolean[][] visited;
		
		public ValueMap(TronGameBoard board)
		{
			int l = board.length();
			grid = new double[l][l];
			visited = new boolean[l][l];
			
			for (int i = 0; i < l; i++) {
				Arrays.fill(grid[i], -1);
			}
		}
		
		public double valueAt(int col, int row) {
			if (row < 0 || col < 0 || row >= grid.length || col >= grid.length) {
				return 0;
			}
			
			return grid[row][col];
		}
		
		public int length()
		{
			return grid.length;
		}
		
		public void multiplyValue(int col, int row, double multiplier) {
			if (row < 0 || col < 0 || row >= grid.length || col >= grid.length) {
				return;
			}
			
			grid[row][col] = grid[row][col] * multiplier;
		}
		
		public void setValue(int col, int row, double d) {
			if (row < 0 || col < 0 || row >= grid.length || col >= grid.length) {
				return;
			}
			
			grid[row][col] = d;
		}
	}
}
