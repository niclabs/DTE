#!/bin/sh
# ----------------------------------------------------------------------------
# Copyright [2009] [NIC Labs]
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of the 	License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or 
# agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
# 
# ----------------------------------------------------------------------------


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
SCRIPTPATH=`dirname $0`

for i in $SCRIPTPATH/../lib/*.jar; do
    CLASSPATH=$i:$CLASSPATH
done; 

CLASSPATH=$SCRIPTPATH/../build/OpenLibsDte.jar:$CLASSPATH

JAVACMD="$JAVA_HOME/bin/java -cp $CLASSPATH"

export JAVACMD
