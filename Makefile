all:
	find -name "*.java" > sources.list
	javac @sources.list -d bin/

launch: all
	cd bin/ && java MySnakeLauncher

mlaunchserv: all
	cd bin/ && java MySnakeLauncherMultiplayer server 0.0.0.0 4444

mlaunchclient: all
	cd bin/ && java MySnakeLauncherMultiplayer client localhost 4444
