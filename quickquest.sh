#!/bin/sh
LONG_BIT=`getconf LONG_BIT`
if [ "$LONG_BIT" == "32" ];then
  USR_LIB_PATH="/usr/lib";
else
  USR_LIB_PATH="/usr/lib64";
fi
QUICKQUEST_PROG_DIR=$(dirname $0);
export LD_LIBRARY_PATH=$QUICKQUEST_PROG_DIR/jnotify-linux$LONG_BIT/:$QUICKQUEST_PROG_DIR/qtjambi-linux$LONG_BIT/:$LD_LIBRARY_PATH;
JAVA_CLASSPATH="$QUICKQUEST_PROG_DIR/quickquest-linux.jar";
for f in `ls $QUICKQUEST_PROG_DIR/lib/*.jar`; do
  JAVA_CLASSPATH=$JAVA_CLASSPATH:$f;
done

echo "QuickQuest Program Path:" $QUICKQUEST_PROG_DIR;
echo "Class Path:" $JAVA_CLASSPATH;

java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9898 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1 -Dquickquest.prog.dir="$QUICKQUEST_PROG_DIR" -cp $JAVA_CLASSPATH us.suselinks.quickquest.Main