#!/bin/sh
# ----------------------------------------------------------------------------
#

# seteando el classpath
SCRIPT=$(readlink -f $0)
SCRIPTPATH=`dirname $SCRIPT`
. $SCRIPTPATH/env.sh

#echo CLASSPATH=$CLASSPATH
$JAVACMD cl.nic.dte.examples.GeneraFactura $@
