#!/bin/bash

if [ $# != 1 ]
then
    echo "Usage: $0 <screen name>"
    exit 1
fi

adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png ./"$1".png
adb shell rm /sdcard/screen.png
