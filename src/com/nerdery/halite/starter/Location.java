package com.nerdery.halite.starter;

public class Location {

  // Public for backward compability
  public final int x, y;
  private final Site site;

  public Location(int x, int y, Site site) {
    this.x = x;
    this.y = y;
    this.site = site;
  }

  /**
   * Get the direction of another location relative to this one. Prefering the x axis.
   *
   * @param otherLocation Another location on the map.
   * @return The Direction relative to this.
   */
  public Direction getDirection(Location otherLocation) {
    if (x < otherLocation.getX()) {
      return Direction.EAST;
    } else if (x > otherLocation.getX()) {
      return Direction.WEST;
    } else if (y < otherLocation.getY()) {
      return Direction.SOUTH;
    } else if (y > otherLocation.getY()) {
      return Direction.NORTH;
    } else {
      return Direction.STILL;
    }
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Site getSite() {
    return site;
  }

  public boolean equals(Location otherLocation) {
    return x == otherLocation.getX() && y == otherLocation.getY();
  }
}
