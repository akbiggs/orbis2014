package minimax.attempt;

import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TronGameBoard;

public class GameState {
	public final TronGameBoard map;
	public LightCycle playerCycle;
	public LightCycle opponentCycle;

	public GameState(TronGameBoard map,  
			LightCycle playerCycle, LightCycle opponentCycle)
	{
		this.map = map;
		this.playerCycle = opponentCycle;
		this.opponentCycle = opponentCycle;
	}
}
