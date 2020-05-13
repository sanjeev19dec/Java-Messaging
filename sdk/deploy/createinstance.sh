#!/bin/sh

AD_HOME=$HOME/<adapter>
INSTANCE_NUM=$1
ACTIVE=$2

if [ -z "$INSTANCE_NUM" -o -z "$ACTIVE" ]; then
  echo "createinstance.sh <instance_num> <1|0>"
  echo "0 = inactive"
  echo "1 = active"
  exit
fi

NEW_INSTANCE_DIR=$AD_HOME/instance$INSTANCE_NUM

mkdir $NEW_INSTANCE_DIR

cp -r $AD_HOME/instance1/bin $NEW_INSTANCE_DIR
cp -r $AD_HOME/instance1/etc $NEW_INSTANCE_DIR
cp -r $AD_HOME/instance1/lib $NEW_INSTANCE_DIR
mkdir $NEW_INSTANCE_DIR/log
cp -r $AD_HOME/instance1/xml $NEW_INSTANCE_DIR

echo $ACTIVE > $NEW_INSTANCE_DIR/log/active.txt
