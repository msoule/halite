package com.nerdery.msoule.mybot;

import com.nerdery.halite.starter.Direction;
import com.nerdery.halite.starter.GameMap;
import com.nerdery.halite.starter.Location;
import com.nerdery.halite.starter.Move;
import com.nerdery.halite.starter.Site;
import com.nerdery.logging.BotLogger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class Brain {

  //private final LocationComparator STRENGTH_SORTER = new LocationComparator();
  private final NeighborComparator NEIGHBOR_STRENGTH_SORTER = new NeighborComparator();
  private final NeighborPriorityComparator NEIGHBOR_PRIORITY_SORTER = new NeighborPriorityComparator();
  private final EnemyDistanceComparator DISTANCE_SORTER = new EnemyDistanceComparator();
  private BotLogger logger;
  private int myId;
  private GameMap map;

  public Brain(BotLogger botLogger, int id, GameMap gameMap) {
    logger = botLogger;
    myId = id;
    map = gameMap;
  }

  public Move assignMove(Location square) {
    //logger.info(String.format("At location: (%d, %d) owned by %d", square.getX(), square.getY(), myId));
    Stream<Location> enemyNeighbors = Arrays.stream(Direction.CARDINALS)
        .map(dir -> getNeighbor(square, dir))
        .filter(otherSquare -> otherSquare.getSite().owner != myId && otherSquare.getSite().strength > 0);
    // If we have no enemy neighbors then move towards them
    if (!enemyNeighbors.findFirst().isPresent()) {
      //logger.info(String.format("(%d, %d) has no enemy neighbors", square.getX(), square.getY()));
      Direction directionToMove = directionOfNearestEnemy(square);
      Location targetLocation = map.getLocation(square, directionToMove);

      if(targetLocation.getSite().strength + square.getSite().strength > Site.MAX_STRENGTH) {
        return new Move(square, Direction.STILL);
      } else if (square.getSite().strength >= (Site.MAX_STRENGTH / (square.getSite().strength + 1))) {
        return new Move(square, directionToMove);
      } else {
        return new Move(square, Direction.STILL);
      }
    // Else consider attacking one
    } else {
      return selectBestOverkillTarget(square).orElse(selectWeakestTarget(square));
    }
  }

  private Optional<Move> selectBestOverkillTarget(Location square) {
    return Arrays.stream(Direction.CARDINALS)
      .map(dir -> getNeighbor2(square, dir))
      .filter(neighbor -> neighbor.location.getSite().owner != myId && neighbor.location.getSite().owner != Site.NEUTRAL_OWNER)
      .filter(enemy -> onlyWeakerTarget(square, enemy.location))
      .sorted(NEIGHBOR_PRIORITY_SORTER)
      .findFirst()
      .map(neighbor -> moveInDirectionOf(square, neighbor));
  }

  private Move selectWeakestTarget(Location square) {
    return Arrays.stream(Direction.CARDINALS)
        .map(dir -> new Neighbor(getNeighbor(square, dir), dir, 0))
        .filter(neighbor -> neighbor.location.getSite().owner != myId)
        .filter(enemy -> onlyWeakerTarget(square, enemy.location))
        .sorted(NEIGHBOR_STRENGTH_SORTER)
        .findFirst()
        .map(neighbor -> moveInDirectionOf(square, neighbor))
        .orElse(new Move(square, Direction.STILL));
  }

  private Integer getOverkillValue(Location target) {
    return Arrays.stream(Direction.CARDINALS)
      .map(dir -> map.getLocation(target, dir).getSite())
      .filter(sq -> sq.owner != Site.NEUTRAL_OWNER && sq.owner != myId)
      .mapToInt(i -> i.strength)
      .sum();
  }

  private boolean onlyWeakerTarget(Location attackingSquare, Location enemySquare) {
    return attackingSquare.getSite().strength > enemySquare.getSite().strength;
  }

  private Neighbor getNeighbor2(Location square, Direction dir) {
    Location neighboringLocation = map.getLocation(square, dir);
    return new Neighbor(neighboringLocation, dir, getOverkillValue(neighboringLocation));
  }

  private Location getNeighbor(Location square, Direction dir) {
    Location neighboringLocation = map.getLocation(square, dir);
    //logger.info(String.format("Neighbor: (%d, %d) owned by %d", neighboringLocation.getX(), neighboringLocation.getY(), neighboringLocation.getSite().owner));
    return neighboringLocation;
  }

  private Move moveInDirectionOf(Location square, Neighbor neighbor) {
    Move newMove = new Move(square, neighbor.direction);
    //logger.info(String.format("Moving towards (%d, %d), which is %s and owned by %d", neighbor.location.getX(), neighbor.location.getY(), newMove.dir.name(), neighbor.location.getSite().owner));
    return newMove;
  }

  private Direction directionOfNearestEnemy(Location square) {
    return Arrays.stream(Direction.CARDINALS)
      .map(direction -> new EnemyDistance(direction, distanceToNearestEnemy(direction, square, map.getLocation(square, direction))))
      .sorted(DISTANCE_SORTER)
      .findFirst()
      .map(enemyDistance -> enemyDistance.direction)
      .orElse(Direction.STILL);
  }

  private double distanceToNearestEnemy(Direction direction, Location homeSquare, Location previousSquareExamined) {
    // move one in that direction
    Location targetLocation = map.getLocation(previousSquareExamined, direction);

    // Oops we went all the way around
    if(homeSquare.equals(targetLocation)) {
      if (direction == Direction.EAST || direction == Direction.WEST) {
        return map.width;
      } else {
        return map.height;
      }
    } else if (targetLocation.getSite().owner != myId && targetLocation.getSite().strength > 0) {
      return map.getDistance(homeSquare, targetLocation);
    } else {
      return distanceToNearestEnemy(direction, homeSquare, targetLocation);
    }
  }

  private class Neighbor {
    Location location;
    Direction direction;
    int priority;

    Neighbor(Location loc, Direction dir, int p) {
      location = loc;
      direction = dir;
      priority = p;
    }
  }

  private class NeighborComparator implements Comparator<Neighbor> {
    @Override
    public int compare(Neighbor o1, Neighbor o2) {
      return o1.location.getSite().strength - o2.location.getSite().strength;
    }
  }

  private class NeighborPriorityComparator implements Comparator<Neighbor> {
    @Override
    public int compare(Neighbor o1, Neighbor o2) {
      return o2.priority - o1.priority;
    }
  }

  private class EnemyDistance {
    Direction direction;
    double distance;

    EnemyDistance(Direction dir, double dis) {
      direction = dir;
      distance = dis;
    }
  }

  private class EnemyDistanceComparator implements Comparator<EnemyDistance> {
    @Override
    public int compare(EnemyDistance o1, EnemyDistance o2) {
      return (int)o1.distance - (int)o2.distance;
    }
  }
}
