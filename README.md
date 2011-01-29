Window Matching Agent
=====================

The Window Matching Agent is a simple java agent that uses bytecode manipulation to change the value
sun.awt.X11.XToolkit assigns to the WM_CLASS property

Usage
-----

To use it add the following argument to your commandline

-javaagent:agent.jar=YOUR_APPLICATION_IDENTIFIER
