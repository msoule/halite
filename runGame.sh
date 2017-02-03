#!/bin/bash

rm -rf target
mkdir target

javac src/com/nerdery/halite/starter/*.java -d target
javac -cp target src/com/nerdery/msoule/mybot/MyBot.java -d target
javac -cp target src/com/nerdery/halite/example/RandomBot.java -d target

~/halite/halite -d "25 25" "java -cp target com.nerdery.msoule.mybot.MyBot" "java -cp target com.nerdery.halite.example.RandomBot"
