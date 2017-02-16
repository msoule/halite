package com.nerdery.halite.starter;

public class Site {

  public static final int NEUTRAL_OWNER = 0;
  public static final int MAX_PRODUCTION = 15;
  public static final int MAX_STRENGTH = 255;
  public final int production;
  public int owner, strength;

  public Site(int production) {
    this.production = production;
  }
}
