import java.awt.Point;
import java.util.Random;

import path.finding.stuff.AStarPathFinder;
import path.finding.stuff.Path;
import path.finding.stuff.Path.Step;

import com.orbischallenge.tron.api.PlayerAction;
import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TronGameBoard;
import com.orbischallenge.tron.client.api.TileTypeEnum;
import com.orbischallenge.tron.protocol.TronProtocol;
import com.orbischallenge.tron.protocol.TronProtocol.PowerUpType;
import com.orbischallenge.tron.protocol.TronProtocol.Direction;

public class PlayerAI implements Player {
	Point lastPlayerPosition;

	@Override
	public void newGame(TronGameBoard map,  
			LightCycle playerCycle, LightCycle opponentCycle) {
		
		return;
		
	}
	
	@Override
	public PlayerAction getMove(TronGameBoard map,
			LightCycle playerCycle, LightCycle opponentCycle, int moveNumber) {
		
//		SearchableMap searchMap = new SearchableMap(map, playerCycle, opponentCycle);
		SearchableMap grid = new SearchableMap(map, playerCycle, opponentCycle);
		SearchablePlayer searchPlayer = new SearchablePlayer();
		Point dest = grid.getDestination();//opponentIntersect(grid, playerCycle, opponentCycle);
		//dest = (dest != null && !grid.blocked(null, dest.x, dest.y) && grid.percentageOfLevelFilled < 0.5) ? 
		//		dest : grid.getDestination();
		
		System.out.println("Grid " + grid);
		
		AStarPathFinder pathFinder = new AStarPathFinder(grid, 100, false);
		Path path = pathFinder.findPath(searchPlayer, playerCycle.getPosition().x,
							playerCycle.getPosition().y, dest.x, dest.y);
		
		
		//Where we want to move to
		final int movetoX, movetoY;
		
		if (path == null)
		{
			Point bestDest = grid.getBestAdjacantPosTo(playerCycle);
			movetoX = bestDest.x;
			movetoY = bestDest.y;
		}
		else
		{
			Step firstStep = path.getStep(1);
			movetoX = firstStep.getX();
			movetoY = firstStep.getY();
		}
		
		//Choose the action based on where we want to move to
		PlayerAction actionChosen = PlayerAction.SAME_DIRECTION;

		int xDiff = movetoX - playerCycle.getPosition().x;
		int yDiff = movetoY - playerCycle.getPosition().y;
		
		if (xDiff > 0) {
			actionChosen = PlayerAction.MOVE_RIGHT;
		} else if(yDiff < 0) {
			actionChosen = PlayerAction.MOVE_UP;
		} else if (xDiff < 0) {
			actionChosen = PlayerAction.MOVE_LEFT;
		} else  if (yDiff > 0) {
			actionChosen = PlayerAction.MOVE_DOWN;
		}
		
		System.out.println(actionChosen);
		return actionChosen;
	}
	
	private Point opponentIntersect(SearchableMap map,
			LightCycle playerCycle, LightCycle opponentCycle)
	{
		if (oppositeDirections(playerCycle, opponentCycle))
		{
			Point p = opponentCycle.getPosition();
			
			Direction od = opponentCycle.getDirection();
			switch (od)
			{
			case DOWN:
				p.y += 1;
				break;
			case LEFT:
				p.x -= 1;
				break;
			case RIGHT:
				p.x += 1;
				break;
			case UP:
				p.y -= 1;
				break;
			}
			
			return p;
		} else {
			return null;
		}
	}
	
	private boolean oppositeDirections(LightCycle playerCycle, LightCycle opponentCycle)
	{
		Direction pd = playerCycle.getDirection();
		Direction od = opponentCycle.getDirection();
		boolean a = (pd == Direction.DOWN || pd == Direction.UP) && (od == Direction.LEFT || od == Direction.RIGHT);
		boolean b = (od == Direction.DOWN || od == Direction.UP) && (pd == Direction.LEFT || pd == Direction.RIGHT);
		
		return a || b;
	}
}

/**

8888888 8888888888 8 888888888o.      ,o888888o.     b.             8 
      8 8888       8 8888    `88.  . 8888     `88.   888o.          8 
      8 8888       8 8888     `88 ,8 8888       `8b  Y88888o.       8 
      8 8888       8 8888     ,88 88 8888        `8b .`Y888888o.    8 
      8 8888       8 8888.   ,88' 88 8888         88 8o. `Y888888o. 8 
      8 8888       8 888888888P'  88 8888         88 8`Y8o. `Y88888o8 
      8 8888       8 8888`8b      88 8888        ,8P 8   `Y8o. `Y8888 
      8 8888       8 8888 `8b.    `8 8888       ,8P  8      `Y8o. `Y8 
      8 8888       8 8888   `8b.   ` 8888     ,88'   8         `Y8o.` 
      8 8888       8 8888     `88.    `8888888P'     8            `Yo
      
                                Quick Guide
                --------------------------------------------

        1. THIS IS THE ONLY .JAVA FILE YOU SHOULD EDIT THAT CAME FROM THE ZIPPED STARTER KIT
        
        2. Any external files should be accessible from this directory

        3. newGame is called once at the start of the game if you wish to initialize any values
       
        4. getMove is called for each turn the game goes on

        5. map represents the game field. map.isOccupied(2, 2) returns whether or not something is at position (2, 2)
        								  map.tileType(2, 2) will tell you what is at (2, 2). A TileTypeEnum is returned.
        
        6. playerCycle is your lightcycle and is what the turn you respond with will be applied to.
                playerCycle.getPosition() is a Point object representing the (x, y) position
                playerCycle.getDirection() is the direction you are travelling in. can be compared with Direction.DIR where DIR is one of UP, RIGHT, DOWN, or LEFT
                playerCycle.hasPowerup() is a boolean representing whether or not you have a powerup
                playerCycle.isInvincible() is a boolean representing whether or not you are invincible
                playerCycle.getPowerupType() is what, if any, powerup you have
        
        7. opponentCycle is your opponent's lightcycle.

        8. You ultimately are required to return one of the following:
                                                PlayerAction.SAME_DIRECTION
                                                PlayerAction.MOVE_UP
                                                PlayerAction.MOVE_DOWN
                                                PlayerAction.MOVE_LEFT
                                                PlayerAction.MOVE_RIGHT
                                                PlayerAction.ACTIVATE_POWERUP
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_UP
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_DOWN
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_LEFT
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_RIGHT
      	
     
        9. If you have any questions, contact challenge@orbis.com
        
        10. Good luck! Submissions are due Sunday, September 21 at noon. 
            You can submit multiple times and your most recent submission will be the one graded.
 */