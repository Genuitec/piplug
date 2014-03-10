piplug
======

PiPlug provides an extensible front-end for the Raspberry Pi allowing small Java plug-ins to be written using Eclipse SWT (very small overhead -- no full Eclipse footprint).  The PiPlug runtime connects to a PiPlug daemon that services apps on your local network, with automatic discovery.  A plug-in for Eclipse gives you a simple view to upload your apps dynamically to the daemon with them automatically becoming available.

The following is an example view:

![ScreenShot](docs/images/piplug-view.png?raw=true)

The PiPlug front-end gives you a simple and familiar way to deploy applications to your various Pis in your home. You and your family and friends can simply use the cool apps you've built.

![ScreenShot](docs/images/piplug-apphome.png?raw=true)

Getting Started
---------------

To get started, you will install the PiView plug-in into your Eclipse IDE.

1. Go to Help : Add/Remove Software 
2. Install from site : http://github.com/Genuitec/piplug/updatesite
3. Choose to install the PiPlug view into your Eclipse
4. Use Window : Show View : Other : PiPlug Deploy to add app management
5. Check out Example applications from GitHub git@github.com:Genuitec/piplug-apps.git
6. Import the projects into your workspace

Example Applications
--------------------

Other applications in the example include Zork 1, 2 and 3 games from the Infocom Emulator, a Snake game ported from the SWT example games, and a simple Clock.

![ScreenShot](docs/images/piplug-zork2.png?raw=true)
![ScreenShot](docs/images/piplug-clock.png?raw=true)
![ScreenShot](docs/images/piplug-snake.png?raw=true)
