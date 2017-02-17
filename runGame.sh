#!/bin/bash

rm MyBot.jar
rm RandomBot.jar
rm -rf target
mkdir target

javac src/com/nerdery/halite/starter/*.java -d target
javac -cp target src/com/nerdery/logging/*.java -d target
javac -cp target src/com/nerdery/msoule/mybot/*.java -d target
javac -cp target src/com/nerdery/halite/example/RandomBot.java -d target

cd target
jar -cvfm MyBot.jar ../mybot.mf .
jar -cvfm RandomBot.jar ../random.mf .

mv MyBot.jar ..
mv RandomBot.jar ..
cd ..

~/halite/halite -d "25 25" "sh MyBot.sh" "sh MyBot.sh"
