#!/bin/bash

LIMIT=1
if [ "$1" != "" ]; then
	LIMIT="$1"
fi

i=1
while [ $i -le $LIMIT ]; do
	npm run test | sed "s/^/[$i] /"
	RES=${PIPESTATUS[0]}
	if [ $RES -gt 0 ] ; then
		echo "FAILED: $RES"
		break;
	fi
	((i++))
done
