import java.awt.Point;

import path.finding.stuff.Mover;
import path.finding.stuff.TileBasedMap;

import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TileTypeEnum;
import com.orbischallenge.tron.client.api.TronGameBoard;


public class SearchableMap implements TileBasedMap {
	/** Indicator if a given tile has been visited during the search */
	private boolean[][] visited;
	private TronGameBoard gameBoard;
	private LightCycle playerCycle;
	private LightCycle opponentCycle;
	
	public SearchableMap( TronGameBoard gameBoard, LightCycle playerCycle, LightCycle opponentCycle) {	
		this.gameBoard = gameBoard;
		this.playerCycle = playerCycle;
		this.opponentCycle = opponentCycle;
		
		this.visited = new boolean[gameBoard.length()][gameBoard.length()];
	}

	@Override
	public int getWidthInTiles() {
		return this.gameBoard.length();
	}

	@Override
	public int getHeightInTiles() {
		return this.gameBoard.length();
	}

	@Override
	public void pathFinderVisited(int x, int y) {
		this.visited[x][y] = true;
	}

	@Override
	public boolean blocked(Mover mover, int x, int y) {
		TileTypeEnum type = this.gameBoard.tileType(x, y);
		
		boolean isPlayerPosition = x == this.playerCycle.getPosition().x &&
				y == this.playerCycle.getPosition().y;
		Point opPos = this.opponentCycle.getPosition();
		boolean isNearOpponentPosition = Math.abs(opPos.x - x) <= 1 ||
				Math.abs(opPos.y - y) <= 1;

		return (isNearOpponentPosition ||
				type == TileTypeEnum.LIGHTCYCLE ||
				type == TileTypeEnum.TRAIL || 
				type == TileTypeEnum.WALL);
	}

	@Override
	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		// TODO Auto-generated method stub
		return 1;
	}

}
