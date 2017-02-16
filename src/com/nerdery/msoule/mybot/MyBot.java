package com.nerdery.msoule.mybot;

import com.nerdery.logging.BotLogger;
import com.nerdery.halite.starter.*;

import java.util.ArrayList;
import java.util.List;

public class MyBot {

  private static BotLogger logger = new BotLogger(MyBot.class.getSimpleName(), true);

  public static void main(String[] args) throws java.io.IOException {

    final InitPackage iPackage = Networking.getInit();
    final int myId = iPackage.myID;
    final GameMap gameMap = iPackage.map;
    final Brain brain = new Brain(logger, myId, gameMap);

    Networking.sendInit("msouleV2");

    while (true) {
      List<Move> moves = new ArrayList<Move>();

      Networking.updateFrame(gameMap);

      for (int y = 0; y < gameMap.height; y++) {
        for (int x = 0; x < gameMap.width; x++) {
          final Location location = gameMap.getLocation(x, y);
          final Site site = location.getSite();
          if (site.owner == myId) {
            moves.add(brain.assignMove(location));
          }
        }
      }
      Networking.sendFrame(moves);
    }
  }
}
