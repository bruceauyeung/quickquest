#!/bin/sh

export LD_LIBRARY_PATH=./lib:/usr/lib/qtjambi/:$LD_LIBRARY_PATH
JAVA_CLASSPATH="/usr/lib/jni/qtjambi.jar:quickquest.jar";
for f in `ls ./lib/*.jar`; do
  JAVA_CLASSPATH=$JAVA_CLASSPATH:$f;
done

export QUICKQUEST_PROG_DIR=`pwd`

java -Dquickquest_prog_dir=$QUICKQUEST_PROG_DIR -cp $JAVA_CLASSPATH net.ubuntudaily.quickquest.Main