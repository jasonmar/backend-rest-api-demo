#!/bin/sh

fn_ps(){
  pid=$(ps | egrep -v 'grep' | grep java | awk {'print $1'})
  echo "${pid}"
}

fn_ps

pid=$(ps |grep java | awk {'print $1'})
[ -z "${pid}" ] || kill ${pid}
sleep 2
fn_ps
./start.sh
sleep 2
fn_ps
