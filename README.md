# Smartthings for DSC Based Alarm Panels (others may work as well)


## Overview


Overall, the documentation for how to get your Smartthings hub integrated with a DSC panel is spread out and difficult to find.  There's a great thread (https://community.smartthings.com/t/dsc-vista-alarm-smartapp-and-devices-based-on-alarmserver/3143) on the community forum and MANY people that helped get all of this together.  My goal is to make this a one stop shop to get you from ground zero to integrated with Smartthings.  I will say one word of caution, this will take you time and you need to understand a little bit about how to setup Smartthings.  This isn't easy, but I will try to help as much as I can.

To start, you'll need to purchase an integration unit like Envisalink v4 (http://www.eyezon.com/?page_id=176).  According to the thread mentioned above, version 2 and 3 work as well, however YMMV as I used version 4.  This unit only works with certain panels from DSC, so please check the compatibility chart before purchasing it.  

The reason you need this device is that Smartthings cannot currently talk directly with the DSC panel.  Even further, you will also need a small server (Raspberry Pi, Windows, Linux/Unix, etc. would work) to run a program called Alarm Server.  Ultimately, all of this together will allow you to make calls to your DSC panel using web based URLs.  For example, http://alarmserver:8111/api/alarm/arm.  This URL would ultimately arm your security system.  This will allow the smartthings app to call your DSC Panel to do things like Arm, Disarm, etc...


## Install EnvisaLink


Once you purchase the EnvisaLink, it is very simple to install if you are even the slightest bit handy.  Here's a link to a YouTube video that explains how to install it.  I won't go into much more detail here other than to say that it doesn't have WiFi.  If your alarm unit isn't next to an enthernet jack, then you will need to find an alternative method.  There are many options that I won't go into here like MoCa (if you have Coax close by), Powerline Adapter, Wifi Client Bridge, etc...

https://www.youtube.com/watch?v=0MVauULEZuE

Stop here until you have the EnvisaLink installed and you can see your unit on the evisalink webpage.


## Install Alarm Server


Now that you have that you have envisalink, you can start installing Alarm Server.  Unfortuantely, Smartthings cannot talk directly to Envisalink, so we need another middleman and that's where AlarmServer comes in to play.  You'll need a server setup to run Alarm Server with Python 2.7.10 installed.  Here's python.

https://www.python.org/downloads/

Next, you'll want to install PIP (Package Management System for Python, similar to apt-get on unix) so that you can install pyOpenSSL which is required for AlarmServer.  Here's the instructions for installing PIP.

https://pip.pypa.io/en/latest/installing/

Validate that you have pip installed correctly by running `pip freeze` in the Python27\Scripts directory.

Example:

C:\Python27\Scripts>pip freeze
cffi==1.3.1
cryptography==1.1.1
enum34==1.1.1
etc...

Once PIP is installed and the freeze command works, run the following:

pip install requests
pip install pyopenssl

Now that you have thes modules installed, the next step is completely optional.  This will allow you to run AlarmServer with HTTPS which is more secure, however it won't work with our Smartthings app.  If your goal is to install the Smartthings app, then I would just skip this step.

For those still here and wanting to run HTTPS, we need to obtain an SSL certificate.  To do that, I would recommend installing OpenSSL or purchasing your own.  I won't explain the latter as we are going for a home setup with this tutorial.  Here's a link to the open SSL binaries page.  This will give you the link to their Wiki where they have the recommended binaries for your OS.

https://www.openssl.org/community/binaries.html

Here's a direct link to the binaries I used, however this page looks like it's from 1990.  I didn't initially trust it, but once I saw it was the recommended binary from openssl.org, that rested my nerves.

https://slproweb.com/products/Win32OpenSSL.html

Once installed, you'll need to run the following command to generate the key and cert from the OpenSSL's bin directory.

openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout server.key -out server.crt

This will ask you a bunch of questions.  They were all easy to answer.  Juggie's documentation on this step states that only common name is the semi-important field.  This should be set to your host name.


## Skip to  here if you don't care to run HTTPS!



We are FINALLY ready to install Alarm Server.  Here's the link to Juggie's version.  For smartthings, you have to use the Smartthings branch which I linked to here directly.  If you don't have GIT, just click the "Download ZIP" button on the github page and extract that into any directory you want.  I used C:/tools/AlarmServer-smartthings

https://github.com/juggie/AlarmServer/tree/smartthings

There are many different versions of AlarmServer floating around, but I used juggies.

It's installed!  Oh wait, it's not configured yet...


## Configure Alarm Server


The first step is to rename alarmserver-example.cfg to alarmserver.cfg.  You will then want to open alarmserver.cfg in your favorite editor (Notepadd++ for me!) and start editing to customize it for your install.  I would change...

logfile --> Leave blank for now. You can turn this on later if you'd like
certfile & keyfile --> Directory where you installed the cert if you didn't skip the step above.  Even if you did skip the step, I would recommend putting the directory of the default certfile/keyfile that came with your Alarm Server zip file downloaded earlier.  Not doing this caused me an error.
partition1 --> Make this equal to what your partition is called in the Envisalink webapp.
zone# --> Add enough zones to match your alarm system and remove the rest.
Smartthings --> There will be an entire section on this part because it is difficult.  Skip this for now.
[envisalink] --> Update this section to match your install for envisalink.  The only thing I changed is the password.  You can do that by clicking on the "Network" button on your internal Envisalink homepage and filling in the "Change User Password" box.  This isn't required, but should be done for security reasons.
alarmcode --> Leave blank!  If you fill this in, anyone on your network can simply call a URL to disarm your Security Panel.  VERY INSECURE!!!  We will add this into the smartthings configuration.

Let's turn on the Alarm Server to make sure you don't see any errors.  From the directory where you installed Alarm Server, run the following.

python alarmserver.py 

You should not see any errors at this point.  The two things that caught me were the certfile/keyfiles not being filled in and the logfile not having the right permissions.  At this point, I'm going to assume it is running.  If you are still having problems and can't figure it out, go to the community forum and post your error to see if someone can help.

Once running, make sure you can see the alarmserver web page by navigating to...

<server ip address>:<alarm server port>

In my case, this was...

192.168.1.2:8111

This should bring up a webpage that shows the status of your Partition (panel) and Zones.  Try clicking Arm (or Disarm if your panel is already armed) and make sure it controls your panel.  I would also validate that your zones are connected correctly by opening a door or window and seeing the status on the Alarm Server.


## Smartthings Integration

Wow, you've made it!!  Really, this is an accomplishment!  Unfortunately, the hardest part is ahead.  We now need to create the Smartthings App and generate the keys.  I don't know why Smartthings decided to make this so difficult to understand, but they did.  On top of that, it was recently changed and took me a while to get it right.  The good news is that it has been well documented by others (THANK YOU!) so we will use their document with a few tweaks.

Let's start with Kholloway's documentation.  I shameless pulled his words here as I personally hate bouncing around between guides, however this is the link to his guide as well as the code we will be using.  Thank you Kholloway!!!

https://github.com/kholloway/smartthings-dsc-alarm


## Kholloway's Documentation - Setting up Smartthings


### Setup a Smartthings developer account at:

 [https://graph.api.smartthings.com](https://graph.api.smartthings.com)


### Setup device types

Using the Smartthings IDE create 3 new device types using the code from the devicetypes directory.

There are 3 types of devices you can create:

* DSC Panel       - (Shows partition status info)
* DSC ZoneContact - (contact device open/close)
* DSC ZoneMotion  - (motion device active/inactive)

In the Web IDE for Smartthings create a new device type for each of the above devices and paste in the code for each device from the corresponding groovy files in the repo.

You can name them whatever you like but I recommend using the names above 'DSC Panel', 'DSC ZoneContact', 'DSC ZoneMotion' since those names directly identify what they do.

For all the device types make sure you save them and then publish them for yourself.

### Create panel device

Create a new device and choose the type of "DSC Panel" that you published earlier. The network id needs to be **partition1**.

### Create individual zones
Create a new "Zone Device" for each Zone you want Smartthings to show you status for. 

The network id needs to be the word 'zone' followed by the matching zone number that your DSC system sees it as.

For example: **zone1** or **zone5**

## End of my shameless stealing of Kholloway's words

## Smartthings OAuth - The hard part

OK - I didn't take the last paragraph because this is the point where it gets very hard if you have never done this before.  Once you do it once, it is much easier to do it again.  I will also warn you that the api changed in the past month so your old method may not work.  Here's a draft of the documentation from Jim @ Smartthings.

https://community.smartthings.com/t/remote-oauth2-token-request-failed-with-401/28541/11

This worked perfectly EXCEPT for one issue.  You will NEED to use curl to return the token, however everything else can be run in a browser like Chrome.  It just didn't work for me in a browser.  One other hint, save off every URL that you generate in case you need to go back a step (you will need to go back a step).  

Here's how I did it - stealing Jim's words.


### Install Curl on Windows (again a 1990s webpage, but this is the golden source)

Here's the main webapge for cURL.  This is a great tool for downloading the contents of a URL and we will use it here to present a POST to Smartthings.

http://curl.haxx.se/download.html

Version I downloaded: http://curl.haxx.se/latest.cgi?curl=win64-ssl-sspi

This will download a zip file with an curl.exe inside of it.  Extract this file somewhere, open your command window to the same folder where you extracted it.  We will run the command in a moment.


### Get Authorization Code

Run in browser: https://graph.api.smartthings.com/oauth/authorize?
        response_type=code&
        client_id=YOUR-SMARTAPP-CLIENT-ID&
        scope=app&
        redirect_uri=YOUR-SERVER-URI
        
Example:
https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id={client_id}&scope=app&redirect_uri=https://graph.api.smartthings.com/oauth/callback
        
This will require the user to log in with their ST credentials, choose a Location, and select what devices may be accessed. An authorization code will be returned that lasts for 24 hours.

You will receive a "Oh No! Something Went Wrong" at the end of this process.  It very likely didn't!!  Look at the URL.  You should have a "code=".  Grab the information after the =, this is your new authorization code.

Note that when a location is chosen, SmartThings will attempt to find a SmartApp with the requested client ID on the specific server associated with that location. If one cannot be found, it will fail to load the devices, with a message about no SmartApp being found for that client ID. This is what is happening with SmartApps that are not published to all servers, or if they are using client IDs/secrets for a different server.


### Get Access Token

Use the authorization code, along with the client ID and secret, to get the access token.  This is where we will use cURL.

https://graph.api.smartthings.com/oauth/token

The following parameters should be sent on the request:

grant_type: use "authorization_code" for this flow.
code: this is the authorization code obtained from the previous step.
client_id: this is the client id of the SmartApp. It is the identifier for the SmartApp.
client_secret: the OAuth client secret of the SmartApp.
redirect_uri: the URI of the server that will receive the token. This must match the URI you used to obtain the authorization code.

Example:
curl -k https://graph.api.smartthings.com/oauth/token?grant_type=authorization_code&client_id={client_id}&client_secret={client_secret}&redirect_uri=https://graph.api.smartthings.com/oauth/callback&scope=app&code={code}

That will return a response like:

{
  "access_token": "XXXXXXXXXXX",
  "expires_in": 1576799999,
  "token_type": "bearer"
}
The token is long-lived (50 years) and doesn't support refresh tokens. Refresh tokens will be supported in a future release.

The access token should be kept securely by the third party.

Keep the "Access Token" safe somewhere, we will use it in the alarmserver.cfg!!

### Get SmartApp Endpoints

Using the access token, get the endpoint for the SmartApp:

Run in browser: https://graph.api.smartthings.com/api/smartapps/endpoints?access_token={access_token}

A successful response will look like this (we will probably remove sending back the client secret soon, and the url is there for legacy purpose - use the uri):

{
    "oauthClient": {
        "clientSecret": "CLIENT-SECRET",
        "clientId": "CLIENT-ID"
    },
    "uri": "BASE-URL/api/smartapps/installations/INSTALLATION-ID",
    "base_url": "BASE-URL",
    "url": "/api/smartapps/installations/INSTALLATION-ID"
}

Keep the "INSTALLATION-ID" safe somewhere, we will use it in the alarmserver.cfg!!

## Configure Alarm Server - Part 2


Make sure you stop Alarm Server (if you didn't earlier), and open alarmserver.cfg again, and fill in the following parameters that you retrieved from the previous steps.

callbackurl_base=https://graph.api.smartthings.com/api/smartapps/installations
callbackurl_app_id={INSTALLATION-ID}
callbackurl_access_token={Access Token}

Start Alarm Server back up.  You should now see that Smartthings and Alarm Server are communicating.  Validate this by opening a door to see if Smartthings shows an open contact using the DSC Panel panel you installed earlier.

## Create a Smartthings App to Arm/Stay/Disarm and Integrate with "Smart Home Monitor"

Navigate back to https://graph.api.smartthings.com and create a new SmartApp (My SmartApps --> New SmartApp).  Click "From Code" and paste the code from smartapps/phiz118/dsc-command-center-v2.src from this github repository.  Click Create.  Click Publish --> For Me

Create a new Device Type (My Device Types --> New Device Type).  Click "From Code" and paste the code from devicetypes/phiz118/dsc-command-center.src from this github repository.  Click Create.  Click Publish --> For Me.

Create a new Device (My Devices --> New Device).

Name: Anything you want --> I used DCS Command
Device Network Id: Anything you want that's unique --> I used seccom1
Type: DSC Command Center
Location: Name of your hub

Click Create. 

Open the Smartthings App on your phone or tablet, navigate to SmartApps, DSC Command V2 and fill in the following:

IP: IP address of your Alarm Server
Port: Port of Alarm Server configured in alarmserver.cfg
Integrate w/ Smart Monitor: Yes/No - Yes if you want the Smart Home Monitor integration!

Button for Alarm - Which: DSC Command
Name: Anything you want --> I used DSC Command V2

Click Done

If everything is work you now have a "Thing" called DSC Command (or whatever you named it) that has 3 buttons for Arm, Arm Stay, and Disarm.  These should control your system.

You also have the Smartthings Home Monitor.  The buttons here ALSO control your system.  It's your preference to what you want to use.

And...  That's IT!!!

I want to thank the following folks (and all others who may have helped write this code).  I mainly just pieced things together.  These folks did the real work.  I appreciate it!!!

JTT-AE <aesystems@gmail.com>
Rob Fisher <robfish@att.net>
Carlos Santiago <carloss66@gmail.com>
