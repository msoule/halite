package com.nerdery.msoule.mybot;

import com.nerdery.halite.starter.Location;

import java.util.Comparator;

public class LocationComparator implements Comparator<Location> {

  @Override
  public int compare(Location o1, Location o2) {
    return o1.getSite().strength - o2.getSite().strength;
  }
}
