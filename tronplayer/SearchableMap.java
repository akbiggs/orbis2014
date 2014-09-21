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
	private static final int SEARCH_THRESHOLD = 10;
	private static final int EMPTY_SPACE_WEIGHT = 1;
	private static final int POWERUP_WEIGHT = 500;
	private static final float STRAIGHT_FACTOR = 1f;
	private static final float FAR_AWAY_FACTOR = 4f;
	private static final float EMPTY_CORNER_FACTOR = 0.5f;
	private static final int CORNER_SIZE = 4;

	ValueMap map;
	
	TronGameBoard board;
	
	public double percentageOfLevelFilled;
	public int distanceToNearestPowerup = Integer.MAX_VALUE;
	
	public SearchableMap(TronGameBoard board, LightCycle player, LightCycle opponent) {
		this.map = new ValueMap(board);
		this.board = board;
		
		int l = map.length();
		
		int numEmpties = 0;
		int numFilled = 0;

		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				TileTypeEnum posType = board.tileType(i, j);
				
				if (map.valueAt(i, j) != -1)	
					continue;
				
				if ((new Point(i, j).equals(player.getPosition()))) {
					map.setValue(i, j, 0);
				}
			
				if (posType == TileTypeEnum.POWERUP) {
					map.setValue(i, j, POWERUP_WEIGHT);
					numEmpties++;
					
					this.distanceToNearestPowerup = Math.min(this.distanceToNearestPowerup,
							this.manhattenDistance(i, j, player.getPosition().x, player.getPosition().y));
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
					
					numFilled++;
				} else {
					map.setValue(i, j, EMPTY_SPACE_WEIGHT);
					numEmpties++;
				}
			}
		}
		
		this.percentageOfLevelFilled = (double) numEmpties + (numEmpties + numFilled);
		
		List<Point> opponentAdjacents = adjacents(opponent.getPosition().x, opponent.getPosition().y);
		List<Point> playerAdjacents = adjacents(player.getPosition().x, player.getPosition().y);
		
		// keep track of changed map values separately so they don't affect the BFS
		List<Double> newValues = new ArrayList<Double>();

		for (Point p1 : playerAdjacents) {
			// heuristic 1: BFS around adjacent areas of player to get an idea
			// of what's around and how valuable/dangerous it is
			double estimatedValue = estimateValueOf(p1.x, p1.y);
			System.out.println(String.format("Estimated value of %d,%d: %.2f", p1.x, p1.y, estimatedValue));
			newValues.add(estimatedValue);
		}
			
		// now flush changes to adjacents on to map
		for (int i = 0; i < playerAdjacents.size(); i++) {
			Point p1 = playerAdjacents.get(i);

			Double estimatedValue = newValues.get(i);
			
			if (aboutZero(estimatedValue)) {
				map.setValue(p1.x, p1.y, 1);
				continue;
			}

			// heuristic 2: maximize the amount of squares we are closer to
			// compared to our opponent
			float spotsOwned = 0;
			
			for (Point p2 : opponentAdjacents) {
				spotsOwned += getNumberOfBeatableSpotsFrom(p1.x, p1.y, p2.x, p2.y) * FAR_AWAY_FACTOR;
			}
			
			double finalValue = spotsOwned + estimatedValue;
			if ( aboutZero(finalValue) && !blocked(null, p1.x, p1.y) )
				finalValue += 1;
			
			//This is a hack
			if (board.tileType(p1.x, p1.y) == TileTypeEnum.POWERUP) {
				finalValue *= 10;
			}
			
			map.setValue(p1.x, p1.y, finalValue);
		}
		
		// heuristic 3: bias towards going straight, zig-zagging is dangerous
		Direction dir = player.getDirection();
		Point pos = player.getPosition();
		Point straightPos = null;
		if (dir == Direction.UP) {
			straightPos = new Point(pos.x, pos.y - 1);
		} else if (dir == Direction.DOWN) {
			straightPos = new Point(pos.x, pos.y + 1);
		} else if (dir == Direction.LEFT) {
			straightPos = new Point(pos.x - 1, pos.y);
		} else {
			straightPos = new Point(pos.x + 1, pos.y);
		}

		map.setValue(straightPos.x, straightPos.y, 
				(int)(map.valueAt(straightPos.x, straightPos.y) * STRAIGHT_FACTOR));
		
		// heuristic 4: tend towards the corner of the map with the most empty space
//		List<Point> corners = getCorners();
//		for (Point adj : playerAdjacents) {
//			Point closestCorner = null;
//			int closestDistance = 0; //manhattenDistance(adj.x, adj.y, closestCorner.x, closestCorner.y);
//			
//			for (Point corner : corners) {
//				int distance = manhattenDistance(adj.x, adj.y, corner.x, corner.y);
//				if (closestCorner == null || distance < closestDistance) {
//					closestCorner = corner;
//					closestDistance = distance;
//				}
//			}
//			
//			double amountOfFreeSpace = sumAround(closestCorner.x, closestCorner.y, CORNER_SIZE-1, new ArrayList<Point>());
//			map.setValue(adj.x, adj.y, amountOfFreeSpace * EMPTY_CORNER_FACTOR);
//		}
	}
	
	private List<Point> getCorners() {
		List<Point> corners = new ArrayList<Point>();

		corners.add(new Point(CORNER_SIZE, CORNER_SIZE));
		corners.add(new Point(map.grid.length - CORNER_SIZE, CORNER_SIZE));
		corners.add(new Point(CORNER_SIZE, map.grid.length - CORNER_SIZE));
		corners.add(new Point(map.grid.length - CORNER_SIZE, map.grid.length - CORNER_SIZE));
		
		return corners;
	}
	
	private double sumAround(int x, int y, int threshold, List<Point> searched) {
		Point p = new Point(x, y);
		for (Point s : searched) {
			if (s.equals(p)) {
				return 0;
			}
		}
		
		double sum = map.valueAt(x, y);
		if (threshold == 0) {
			return sum;
		}
		
		List<Point> newSearched = new ArrayList<Point>(searched);
		newSearched.add(p);
		
		List<Point> adjs = adjacents(x, y, false);
		for (Point p1 : adjs) {
			sum += sumAround(p1.x, p1.y, threshold-1, newSearched);
		}
		
		return sum;
	}
		
		//Going straight should be worth more
//		Direction dir = player.getDirection();
//		final double multiplier = 2;
//		switch (dir)
//		{
//		case LEFT:
//		case RIGHT:
//			for (int y = player.getPosition().y; y < map.length(); y++)
//				map.multiplyValue(player.getPosition().x, y, multiplier);
//			for (int y = player.getPosition().y; y >= 0; y--)
//				map.multiplyValue(player.getPosition().x, y, multiplier);
//			break;
//		case DOWN:
//		case UP:
//			for (int x = player.getPosition().x; x < map.length(); x++)
//				map.multiplyValue(x, player.getPosition().y, multiplier);
//			for (int x = player.getPosition().x; x >= 0; x--)
//				map.multiplyValue(x, player.getPosition().y, multiplier);
//			break;
//		default:
//			break;
//		}
	
	private List<Point> adjacents(int x, int y) {
		return adjacents(x, y, true);
	}
	
	private List<Point> adjacents(int x, int y, boolean filter) {
		List<Point> results = new ArrayList<Point>();
		
		if (!filter || (x > 0 && !blocked(null, x-1, y))) {
			results.add(new Point(x-1,y));
		}
		
		if (!filter || (x < board.length() - 1 && !blocked(null, x+1, y))) {
			results.add(new Point(x+1,y));
		}
		
		if (!filter || (y > 0 && !blocked(null, x, y - 1))) {
			results.add(new Point(x,y-1));
		}
		
		if (!filter || (y < board.length() - 1 && !blocked(null, x, y+1))) {
			results.add(new Point(x,y+1));
		}
		
		return results;
	}
	
	private int manhattenDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}
	
	public double estimateValueOf(int x, int y) {
		double e = estimateValueOf(x, y, SEARCH_THRESHOLD, new ArrayList<Point>());
		//System.out.println("For (" + x + ", " + y + ") : " + e);
		
		return e;
	}
	
	public double estimateValueOf(int x, int y, int threshold, List<Point> searched) {
		
		if (aboutZero(map.valueAt(x, y))) {
			return 0;
		}
		
		if (threshold == 0) {
			return map.valueAt(x, y);
		}
		
		List<Point> newSearched = new ArrayList<Point>(searched);
		newSearched.add(new Point(x, y));
		
		List<Point> adjs = adjacents(x, y);
		
		double initialValue = map.valueAt(x, y);
		double estimatedValue = initialValue;
		
		for (Point p : adjs) {
			boolean skip = false;
			for (Point s : searched)
				if (s.equals(p))
					skip = true;

			if (skip)
				continue;
			
			estimatedValue += estimateValueOf(p.x, p.y, threshold-1, newSearched);
		}
		
		if (estimatedValue == initialValue) {
			return 0;
		}
		
		return estimatedValue;
	}
	
	private int getNumberOfBeatableSpotsFrom(int x, int y, int opponentX, int opponentY) {
		int numBeatable = 0;
		
		for (int i = 0; i < map.length(); i++) {
			for (int j = 0; j < map.length(); j++) {
				if (aboutZero(map.valueAt(i, j)))
					continue;
				
				int playerDistance = manhattenDistance(x, y, i, j);
				int opponentDistance = manhattenDistance(opponentX, opponentY, i, j);
				
				if (playerDistance < opponentDistance) {
					numBeatable += playerDistance;
				}
			}
		}
		
		return numBeatable;
	}
	
	public Point getDestination() {
		if (true) {
			System.out.println("Finding emptiest corner");
			
			double emptiestAmount = 10000000;
			Point emptiestCorner = null;
			
			for (Point p : getCorners()) {
				double emptySpace = sumAround(p.x, p.y, CORNER_SIZE-1, new ArrayList<Point>());
				if (emptySpace < emptiestAmount) {
					emptiestAmount = emptySpace;
					emptiestCorner = p;
				}
			}
		
			System.out.println(String.format("Emptiest corner is %d, %d", emptiestCorner.x, emptiestCorner.y));
			return emptiestCorner;
		}
		return getBestPosition();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < map.length(); i++) {
			for (int j = 0; j < map.length(); j++) {
				result.append((int)map.valueAt(j, i));
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

	public Point getBestAdjacantPosTo(LightCycle player)
	{
		List<Point> adj = this.adjacents(player.getPosition().x, player.getPosition().y);
		if (adj.isEmpty())
			return player.getPosition();
		
		Point bestP = adj.get(0);
		for (Point p : adj)
		{
			if (map.valueAt(p.x, p.y) > map.valueAt(bestP.x, bestP.y))
			{
				bestP = p;
			}
		}
		
		return bestP;
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
		TileTypeEnum posType = board.tileType(x, y);
		return posType == TileTypeEnum.WALL || posType == TileTypeEnum.LIGHTCYCLE
				|| posType == TileTypeEnum.TRAIL;
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
	
	private static boolean aboutZero(double n)
	{
		return n < 0.01 && n > -0.01;
	}
}
