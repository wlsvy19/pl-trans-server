#!/bin/sh


BASE=/export/home/asp/kcisa/parser

RUN_SRC=$1
RUN_DEST=$2
RUN_GREP=$BASE/bin/grep_exclude.txt


for target in `ls $RUN_SRC*`
do
	echo $target
	file=`basename $target`

	echo " $file --> $RUN_DEST/$file.new "

	grep -v -i -f $RUN_GREP $target > $RUN_DEST/$file.new

done

