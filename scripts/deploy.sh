#!/bin/bash

set -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

cp -pf $DIR/../won-raid-bot/target/raid-bot.jar $DIR/raid-bot/raid-bot.jar

rm -rf $DIR/raid-bot/conf
cp -rfp $DIR/../conf $DIR/raid-bot/conf

$DIR/compose-helper.sh up -d --build