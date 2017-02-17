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

  private final NeighborComparator NEIGHBOR_STRENGTH_SORTER = new NeighborComparator();
  private final NeighborPriorityComparator NEIGHBOR_PRIORITY_SORTER = new NeighborPriorityComparator();
  private final EnemyDistanceComparator DISTANCE_SORTER = new EnemyDistanceComparator();
  private final int DISTANCE_FACTOR = 3;

  private BotLogger logger;
  private int myId;
  private GameMap map;
  private Location[] priorityTargets;

  public Brain(BotLogger botLogger, int id, GameMap gameMap, Location[] priorityLocations) {
    logger = botLogger;
    myId = id;
    map = gameMap;
    priorityTargets = priorityLocations;
  }

  public Move assignMove(Location square, Move[] selectedMoves) {
    Move selectMove = assignMove(square);

    // check to make sure we are not about to waste strength on our move.
    return Arrays.stream(selectedMoves)
        .filter(move -> map.getLocation(move.loc, move.dir).equals(map.getLocation(selectMove.loc, selectMove.dir)))
        .findFirst()
        .filter(move -> (move.loc.getSite().strength + square.getSite().strength) > Site.MAX_STRENGTH)
        .map(badMove -> new Move(square, Direction.STILL))
        .orElse(selectMove);
  }

  private Move assignMove(Location square) {
    Stream<Location> enemyNeighbors = Arrays.stream(Direction.CARDINALS)
        .map(dir -> map.getLocation(square, dir))
        .filter(otherSquare -> otherSquare.getSite().owner != myId && otherSquare.getSite().strength > 0);

    // If we have no enemy neighbors then move towards priority targets or a border.
    if (!enemyNeighbors.findFirst().isPresent()) {
      Direction directionToMove = directionOfClosestPriorityTarget(square)
          .orElse(directionOfNearestEnemy(square));
      Location targetLocation = map.getLocation(square, directionToMove);

      if(targetLocation.getSite().strength + square.getSite().strength > Site.MAX_STRENGTH) {
        return new Move(square, Direction.STILL);
      } else if (square.getSite().strength >= (Site.MAX_STRENGTH / (square.getSite().strength + 1))) {
        return new Move(square, directionToMove);
      } else {
        return new Move(square, Direction.STILL);
      }
    // Else attack one of the neighbors.
    } else {
      return selectBestOverkillTarget(square).orElse(selectWeakestTarget(square));
    }
  }

  private Optional<Direction> directionOfClosestPriorityTarget(Location square) {
    Stream<EnemyDistance> sameColumn = Arrays.stream(priorityTargets).filter(loc -> loc.getSite().owner != myId)
        .filter(loc -> loc.getX() == square.getX())
        .map(loc -> new EnemyDistance(map.getDirection(square, loc), map.getDistance(square, loc)));

    Stream<EnemyDistance> sameRow = Arrays.stream(priorityTargets).filter(loc -> loc.getSite().owner != myId)
        .filter(loc -> loc.getY() == square.getY())
        .map(loc -> new EnemyDistance(map.getDirection(square, loc), map.getDistance(square, loc)));

    return Stream.concat(sameColumn, sameRow)
        .sorted(DISTANCE_SORTER)
        .findFirst()
        .filter(enemyDistance -> enemyDistance.distance < ((map.height + map.width) / 2) / DISTANCE_FACTOR)
        .map(enemyDistance -> enemyDistance.direction);
  }

  private Optional<Move> selectBestOverkillTarget(Location square) {
    return Arrays.stream(Direction.CARDINALS)
      .map(dir -> getNeighbor(square, dir))
      .filter(neighbor -> neighbor.location.getSite().owner != myId && neighbor.location.getSite().owner != Site.NEUTRAL_OWNER)
      .filter(enemy -> onlyWeakerTarget(square, enemy.location))
      .sorted(NEIGHBOR_PRIORITY_SORTER)
      .findFirst()
      .map(neighbor -> new Move(square, neighbor.direction));
  }

  private Move selectWeakestTarget(Location square) {
    return Arrays.stream(Direction.CARDINALS)
        .map(dir -> new Neighbor(map.getLocation(square, dir), dir, 0))
        .filter(neighbor -> neighbor.location.getSite().owner != myId)
        .filter(enemy -> onlyWeakerTarget(square, enemy.location))
        .sorted(NEIGHBOR_STRENGTH_SORTER)
        .findFirst()
        .map(neighbor -> new Move(square, neighbor.direction))
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

  private Neighbor getNeighbor(Location square, Direction dir) {
    Location neighboringLocation = map.getLocation(square, dir);
    return new Neighbor(neighboringLocation, dir, getOverkillValue(neighboringLocation));
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
