#!/bin/sh
long_bit=`getconf LONG_BIT`
if [ "$long_bit" == "32" ];then
  USR_LIB_PATH="/usr/lib/";
else
  USR_LIB_PATH="/usr/lib64/";
fi
export LD_LIBRARY_PATH=./lib:$USR_LIB_PATH/qtjambi/:$LD_LIBRARY_PATH
JAVA_CLASSPATH="$USR_LIB_PATH/jni/qtjambi.jar:quickquest.jar";
for f in `ls ./lib/*.jar`; do
  JAVA_CLASSPATH=$JAVA_CLASSPATH:$f;
done

export QUICKQUEST_PROG_DIR=`pwd`

java -Dquickquest_prog_dir=$QUICKQUEST_PROG_DIR -cp $JAVA_CLASSPATH net.ubuntudaily.quickquest.Main