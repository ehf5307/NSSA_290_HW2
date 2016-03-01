NSSA 290 HW2
=====================

To build: 
	MakeJars.bat 	- compiles all src code files
			- builds RunClient.jar and RunServer.Jar

To run:
	RunClient.Jar	- launches a client
	RunServer.jar	- launches a server
		
Mani folder: mainifest files used when making jar files. 
	basically specify which class has main

CODE SETUP:
	- Server.java contains all server code
	- Launcher.java is the iniial window for the client
		- takes IP of server
		- laucnhes chat window
	- ChatClient.java all client code
	- constants.java shared info for server and client
