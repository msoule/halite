package com.nerdery.msoule.mybot;

import com.nerdery.halite.starter.Location;
import java.util.Comparator;

public class ProductionComparator implements Comparator<Location> {

  @Override
  public int compare(Location o1, Location o2) {
    return o2.getSite().production - o1.getSite().production;
  }
}
