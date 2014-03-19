piplug
======

PiPlug provides an extensible front-end for the Raspberry Pi allowing small Java plug-ins to be written using Eclipse SWT (very small overhead -- no full Eclipse footprint).  The PiPlug runtime connects to a PiPlug daemon that services apps on your local network, with automatic discovery.  A plug-in for Eclipse gives you a simple view to upload your apps dynamically to the daemon with them automatically becoming available.

The following is an example view:

![ScreenShot](docs/images/piplug-view.png?raw=true)

The PiPlug front-end gives you a simple and familiar way to deploy applications to your various Pis in your home. You and your family and friends can simply use the cool apps you've built.

![ScreenShot](docs/images/piplug-apphome.png?raw=true)

Getting Started
---------------

To get started, you will first install the PiView plug-in into your Eclipse IDE.

1. Go to Help : Add/Remove Software 
2. Install from site : http://genuitec.github.com/piplug/updatesite
3. Choose to install the PiPlug view into your Eclipse
4. Use Window : Show View : Other : PiPlug Deploy to add app management
5. Check out Example applications from GitHub git@github.com:Genuitec/piplug-apps.git
6. Import the projects into your workspace
7. Choose which examples to deploy, and use the Deploy option in the PiPlug Deploy view

Next, you will install PiPlug on your actual Raspberry Pi.

1. On your Pi, install needed modules:

        sudo apt-get install binfmt-support gnome-mime-data libbonobo2-0 libbonobo2-common libbonoboui2-0 \
        libbonoboui2-common libcanberra0 libfam0 libgnome2-0 libgnome2-common libgnomecanvas2-0 \
        libgnomecanvas2-common libgnomeui-0 libgnomeui-common libgnomevfs2-0 libgnomevfs2-common \
        libgnomevfs2-extra libice-dev libidl0 libnspr4 libnss3 libnss3-1d liborbit2 libpthread-stubs0 \
        libpthread-stubs0-dev libsm-dev libswt-cairo-gtk-3-jni libswt-gtk-3-jni libswt-webkit-gtk-3-jni \
        libx11-dev libx11-doc libxau-dev libxcb1-dev libxdmcp-dev libxt-dev ttf-dejavu-extra \
        x11proto-core-dev x11proto-input-dev x11proto-kb-dev xorg-sgml-doctools xtrans-dev libgtk2.0-dev \
        libxtst-dev libgl1-mesa-dev libglu1-mesa-dev

2. Install the latest Java 8 runtime (check version at https://jdk8.java.net/download.html):

        wget --no-check-certificate
        http://www.java.net/download/jdk8/archive/b124/binaries/jdk-8-ea-b124-linux-arm-vfp-hflt-17_jan_2014.tar.gz
        cd /opt
        sudo tar zxvf jdk-8-ea-b124-linux-arm-vfp-hflt-17_jan_2014.tar.gz
        sudo update-alternatives --install "/usr/bin/java" "java" "/opt/jdk1.8.0/bin/java" 1
        java -version
        
3. Confirm that Java 8 is activated as default (running java -version), if not:

        cd /usr/bin
        sudo rm java
        sudo ln -s /opt/jdk1.8.0/bin/java java
        
4. Download the latest PiPlug front-end from GitHub:

        wget https://github.com/Genuitec/piplug/releases/download/v1.0.2/piplug-frontend-1.0.2.tgz
        tar -xvzf piplug-frontend.tgz
        
5. Run the PiPlug front-end!

        piplug-frontend/piplug

Example Applications
--------------------

Other applications in the example include Zork 1, 2 and 3 games from the Infocom Emulator, a Snake game ported from the SWT example games, and a simple Clock.

![ScreenShot](docs/images/piplug-zork2.png?raw=true)
![ScreenShot](docs/images/piplug-clock.png?raw=true)
![ScreenShot](docs/images/piplug-snake.png?raw=true)
