#!/bin/sh


. /export/home/asp/wn/server/bin/ospenv

RUN_PROJECT=$1

RUN_SRC=$2


for target in `ls $RUN_SRC*`
do
	FILE=`basename $target`

	echo " $OSP_BASE/bin/run_load.sh $RUN_PROJECT $target  "	
	
	$OSP_BASE/bin/run_load.sh $RUN_PROJECT $target 

done

