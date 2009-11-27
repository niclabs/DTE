#!/bin/sh
# ----------------------------------------------------------------------------
#

JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]
then
echo
echo "La variable de ambiente JAVA_HOME debe ser asignada con la ubicacion"
echo "de una distribucion de Sun Java 1.6 (o superior) instalada en su sistema"
echo "Ejecute "
echo "    export JAVA_HOME=<directorio donde está el JDK>"
exit 127
fi

# seteando el classpath
SCRIPT=$(readlink -f $0)
SCRIPTPATH=`dirname $SCRIPT`

for i in $SCRIPTPATH/../lib/*.jar; do
    CLASSPATH=$i:$CLASSPATH
done; 

CLASSPATH=$SCRIPTPATH/../build/OpenLibsDte.jar:$CLASSPATH

JAVACMD="$JAVA_HOME/bin/java -cp $CLASSPATH"

export JAVACMD
