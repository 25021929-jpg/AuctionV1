@echo off
chcp 65001 > nul
echo Khoi dong Auction Client...
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar Auction-client/target/Auction-client.jar
