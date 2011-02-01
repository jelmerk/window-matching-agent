Window Matching Agent
=====================

The Window Matching Agent is a simple java agent that uses bytecode manipulation to change the value
sun.awt.X11.XToolkit assigns to the WM_CLASS property

Building from source
--------------------

Make sure you have have [ant](http://ant.apache.org) installed then from the commandline run :

ant jar

the generated agent.jar file will end up in the dist folder

Usage
-----

To use it add the following argument to your commandline

-javaagent:agent.jar=YOUR_APPLICATION_IDENTIFIER
