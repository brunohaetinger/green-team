#!/bin/bash

while true
  do
    time_now=`date`
    echo "${time_now} => Checking sales-data.csv file size. It ensures that the volume is mounted"
    du -hcs ../../../data/sales-data.csv || exit 1
    sleep 5
  done
    