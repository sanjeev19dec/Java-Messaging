UTISOFT_HOME=$HOME
ES_BASE=$UTISOFT_HOME/eservices
ES_HOME=$ES_BASE/instance1
ARCHIVE_BASE=uti_eservices

INSTANCES=`cd $ES_BASE; ls -d instance*`

for INSTANCE in $INSTANCES; do
  if [ "$INSTANCE" != "instance1" ]; then
    rm -rf $ES_BASE/$INSTANCE
  fi
done

if [ -e $UTISOFT_HOME/$ARCHIVE_BASE.zip ]; then
  ARCHIVE_NAME=$ARCHIVE_BASE.zip
  ARCHIVE_CMD=unzip;
elif [ -e $UTISOFT_HOME/$ARCHIVE_BASE.tar.gz ]; then
  ARCHIVE_NAME=$ARCHIVE_BASE.tar
  ARCHIVE_CMD="tar xfv"
  gunzip $UTISOFT_HOME/$ARCHIVE_NAME.gz;
fi
    
cd /opt; $ARCHIVE_CMD $UTISOFT_HOME/$ARCHIVE_NAME

echo "Copy configs...."
cp $ES_HOME/etc/adapter.xml.bak $ES_HOME/etc/adapter.xml
cp $ES_HOME/etc/logging.properties.bak $ES_HOME/etc/logging.properties

echo "*******************************************************"
echo "* NOTE: Recreate all instances previously configured! *"
echo "*******************************************************"

rm $UTISOFT_HOME/$ARCHIVE_NAME
