#!/bin/sh

. /export/home/asp/wn/server/bin/ospenv

JOB_PROJECT=$1
JOB_DATFILE=$2

FILE=`basename $JOB_DATFILE`

JOB_CTLFILE=wn_log.ctl

JOB_LOGFILE=$OSP_PRO/$JOB_PROJECT/log/imp.$FILE.log
JOB_BADFILE=$OSP_PRO/$JOB_PROJECT/log/imp.$FILE.bad


JOB_USERID=ecladm
JOB_PWD=ebrother
JOB_SID=ebrother

$ORACLE_LOADER $JOB_USERID/$JOB_PWD@$JOB_SID ${JOB_CTLFILE} data=${JOB_DATFILE} log=${JOB_LOGFILE} bad=${JOB_BADFILE} errors=1000000

