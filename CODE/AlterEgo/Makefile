all: build
build:
	ant debug

clean:
	ant clean

install:
	adb install -r bin/AlterEgo-debug.apk

log:
	tail -n250 -F logout.txt | logcat-color
bil: build install log
