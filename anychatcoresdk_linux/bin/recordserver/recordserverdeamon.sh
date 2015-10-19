#! /bin/bash
PRO_PATH=$(cd "$(dirname "$0")"; pwd)
export LD_LIBRARY_PATH=$PRO_PATH:$LD_LIBRARY_PATH
PROGRAM="anychatrecordserver"
while [ 1 ]
do
	sleep 5
	PRO_NOW=`ps aux | grep $PROGRAM | grep -v grep | wc -l`
	if [ $PRO_NOW -lt 1 ]; then
		$PRO_PATH/$PROGRAM -d
	fi
done
exit 0
