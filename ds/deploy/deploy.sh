UTISOFT_HOME=$HOME
DS_BASE=$UTISOFT_HOME/ds
DS_HOME=$DS_BASE/instance1
ARCHIVE_BASE=uti_ds

INSTANCES=`cd $DS_BASE; ls -d instance*`

for INSTANCE in $INSTANCES; do
  if [ "$INSTANCE" != "instance1" ]; then
    rm -rf $DS_BASE/$INSTANCE
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
cp $DS_HOME/etc/adapter.xml.bak $DS_HOME/etc/adapter.xml
cp $DS_HOME/etc/lprovision.xml.bak $DS_HOME/etc/lprovision.xml
cp $DS_HOME/etc/logging.properties.bak $DS_HOME/etc/logging.properties

echo "*******************************************************"
echo "* NOTE: Recreate all instances previously configured! *"
echo "*******************************************************"

rm $UTISOFT_HOME/$ARCHIVE_NAME
