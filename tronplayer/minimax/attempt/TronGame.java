package minimax.attempt;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import aima.core.search.adversarial.Game;

import com.orbischallenge.tron.api.PlayerAction;
import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TileTypeEnum;
import com.orbischallenge.tron.client.api.TronGameBoard;
import com.orbischallenge.tron.protocol.TronProtocol.Direction;

public class TronGame implements Game<GameState, PlayerAction, LightCycle>{
	private GameState initialState;

	private boolean meIsCur;

	public TronGame(GameState initialState)
	{
		this.initialState = initialState;

		this.meIsCur = true;
	}
	@Override
	public GameState getInitialState() {
		return this.initialState;
	}

	@Override
	public LightCycle[] getPlayers() {
		return new LightCycle[] {this.initialState.playerCycle, this.initialState.opponentCycle};
	}

	@Override
	public LightCycle getPlayer(GameState state) {
		//Don't know what goes here
		return meIsCur ? state.playerCycle : state.opponentCycle;
	}

	@Override
	public List<PlayerAction> getActions(GameState state) {
		return validMoves(state.map, this.getPlayer(state).getPosition().x, this.getPlayer(state).getPosition().y);
	}

	@Override
	public GameState getResult(GameState state, PlayerAction action) {
		GameState newState = new GameState(state.map, state.playerCycle, state.opponentCycle);
		if (meIsCur)
		{
			//TODO
			Point newPos = addActionToPoint(newState.playerCycle.getPosition(), action);
			newState.playerCycle = new LightCycle(newPos.x, newPos.y, 0, Direction.DOWN, false);
			newState.opponentCycle = new LightCycle(newState.opponentCycle.getPosition().x, newState.opponentCycle.getPosition().y, 1, newState.opponentCycle.getDirection(), false);
			//newState.map = new Tron
		}
		else
		{
			//TODO
			Point newPos = addActionToPoint(newState.opponentCycle.getPosition(), action);
			newState.opponentCycle = new LightCycle(newPos.x, newPos.y, 0, Direction.DOWN, false);
			newState.playerCycle = new LightCycle(newState.playerCycle.getPosition().x, newState.playerCycle.getPosition().y, 1, newState.playerCycle.getDirection(), false);
		}
		
		return state;
	}

	@Override
	public boolean isTerminal(GameState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getUtility(GameState state, LightCycle player) {
		// TODO Auto-generated method stub
		return 0;
	}

	private List<PlayerAction> validMoves(TronGameBoard st, int x, int y) {
		List<PlayerAction> moves = new ArrayList<PlayerAction>();
				
		if (x != 0 && isSafeTileType(st.tileType(x+1, y))) {
			moves.add(PlayerAction.MOVE_RIGHT);
		}
		if (x != (st.length()-1) && isSafeTileType(st.tileType(x-1, y))) {
			moves.add(PlayerAction.MOVE_LEFT);
		}
		if (y != 0 && isSafeTileType(st.tileType(x, y-1))) {
			moves.add(PlayerAction.MOVE_UP);
		}
		if (y != (st.length()-1) && isSafeTileType(st.tileType(x, y+1))) {
			moves.add(PlayerAction.MOVE_DOWN);
		}
		
		return moves;
	}
	
	private static boolean isSafeTileType(TileTypeEnum tile)
	{
		return tile == TileTypeEnum.EMPTY ||
			tile == TileTypeEnum.POWERUP;
	}
	
	private static Point addActionToPoint(Point p, PlayerAction action)
	{
		switch (action)
		{
		case ACTIVATE_POWERUP_MOVE_DOWN:
		case MOVE_DOWN:
			return new Point(p.x, p.y + 1);
		case ACTIVATE_POWERUP_MOVE_UP:
		case MOVE_UP:
			return new Point(p.x, p.y - 1);
		case ACTIVATE_POWERUP_MOVE_LEFT:
		case MOVE_LEFT:
			return new Point(p.x - 1, p.y);
		case ACTIVATE_POWERUP_MOVE_RIGHT:
		case MOVE_RIGHT:
			return new Point(p.x + 1, p.y);
		default:
			return p;
		}
	}
}
