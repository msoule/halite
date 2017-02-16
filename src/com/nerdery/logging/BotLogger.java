package com.nerdery.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BotLogger {

  private Logger log;
  private boolean enabled;

  public BotLogger(String botName, Boolean logsOn) {
    enabled = logsOn;
    if (enabled) {
      log = Logger.getLogger(botName);
      FileHandler logFile;
      try {
        logFile = new FileHandler(botName + "Log.log");
        log.addHandler(logFile);
        log.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();
        logFile.setFormatter(formatter);
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void info(String message) {
    if (enabled) {
      log.info(message);
    }
  }

  public void debug(String message) {
    if (enabled) {
      log.fine(message);
    }
  }

  public void error(String message) {
    if (enabled) {
      log.info("ERROR: " + message);
    }
  }
}
