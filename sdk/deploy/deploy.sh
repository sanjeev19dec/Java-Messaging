UTISOFT_HOME=$HOME
<AD>_BASE=$UTISOFT_HOME/<adapter>
<AD>_HOME=$<AD>_BASE/instance1
ARCHIVE_BASE=uti_<adapter>

INSTANCES=`cd $<AD>_BASE; ls -d instance*`

for INSTANCE in $INSTANCES; do
  if [ "$INSTANCE" != "instance1" ]; then
    rm -rf $<AD>_BASE/$INSTANCE
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
cp $<AD>_HOME/etc/adapter.xml.bak $<AD>_HOME/etc/adapter.xml
cp $<AD>_HOME/etc/logging.properties.bak $<AD>_HOME/etc/logging.properties

echo "*******************************************************"
echo "* NOTE: Recreate all instances previously configured! *"
echo "*******************************************************"

rm $UTISOFT_HOME/$ARCHIVE_NAME
