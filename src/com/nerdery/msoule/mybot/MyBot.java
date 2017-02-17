package com.nerdery.msoule.mybot;

import com.nerdery.logging.BotLogger;
import com.nerdery.halite.starter.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyBot {

  private static BotLogger logger = new BotLogger(MyBot.class.getSimpleName(), true);
  private static final ProductionComparator PRODUCTION_SORTER = new ProductionComparator();
  private static final double targetPercentage = 0.1;

  public static void main(String[] args) throws java.io.IOException {

    final InitPackage iPackage = Networking.getInit();
    final int myId = iPackage.myID;
    final GameMap gameMap = iPackage.map;

    ArrayList<Location> allLocations = new ArrayList<Location>();
    Arrays.stream(gameMap.getLocations()).forEach(ls -> allLocations.addAll(Arrays.asList(ls)));
    allLocations.sort(PRODUCTION_SORTER);

    int priorityCount = (int)(allLocations.size() * targetPercentage);
    Location[] arrayInit = new Location[priorityCount];
    Location[] priorityLocations = allLocations.subList(0, priorityCount).toArray(arrayInit);

    final Brain brain = new Brain(logger, myId, gameMap, priorityLocations);

    Networking.sendInit("msoule");

    while (true) {
      List<Move> moves = new ArrayList<Move>();

      Networking.updateFrame(gameMap);

      for (int y = 0; y < gameMap.height; y++) {
        for (int x = 0; x < gameMap.width; x++) {
          final Location location = gameMap.getLocation(x, y);
          final Site site = location.getSite();
          if (site.owner == myId) {
            Move[] moveInit = new Move[moves.size()];
            moves.add(brain.assignMove(location, moves.toArray(moveInit)));
          }
        }
      }
      Networking.sendFrame(moves);
    }
  }
}
