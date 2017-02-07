rm -rf bin
mkdir bin
cd src
javac -d ../bin ConnectionListener.java  MessageReceiver.java  NodeLauncher.java
