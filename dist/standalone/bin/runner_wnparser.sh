#!/bin/sh

. /export/home/asp/wn/server/bin/ospenv


CLASSPATH=$OSP_BASE/lib/eBrotherTrans_20140727.jar:$OSP_BASE/lib/log4j-1.2.16.jar
export CLASSPATH


PARSER_SERVER=$OSP_BASE/lib/parser_server.txt
PARSER_PATTERN=$OSP_BASE/lib/parser_pattern.txt

RUN_CLASS=com.eBrother.app.impl.ParserWorker

RUN_IN=$1
RUN_OUT=$2
RUN_FILTER=$3

$JAVA $RUN_CLASS $PARSER_SERVER $PARSER_PATTERN $RUN_IN $RUN_OUT $RUN_FILTER
