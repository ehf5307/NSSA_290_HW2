javac *.java

jar -cmfv MANI/Mani_Client.txt RunClient.jar Chat*.class Constants.class Launcher*.class 

jar -cmfv MANI/Mani_Server.txt RunServer.jar Constants.class Server*.class  

pause