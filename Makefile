all:
	find -name "*.java" > sources.list
	javac @sources.list -d bin/

launch: all
	cd bin/ && java MySnakeLauncher

mlaunchserv: all
	cd bin/ && java MySnakeMultiplayerDedicatedServer 4444

mlaunchclient: all
	cd bin/ && java MySnakeLauncherMultiplayer localhost 4444
