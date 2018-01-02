Baidu CarLife Vehicle

    CarLife is a Smartphone-Integration solution, with which drivers can
share the mobile applications suitable for safe driving conditions on MD 
(Mobile Device) to HU (Head Unit) through the multi-screen sharing and 
interaction technology, and use the Touch Screen, Hard Keys, Knob Control 
and Microphone to control CarLife. 
    This solution takes full advantages of both MD and HD, and provides 
safe and abundant infotainment experience for drivers.
    All HMI in CarLife are strictly adjusted to vehicle regulations, 
making them suitable for driving conditions.
    Functions of Carlife Vehicle mainly include connection, video , audio , 
touch and protocol analysis.It supports iPhone and Android phone's USB 
connection, and has by far covered 95% of the best selling mobile phones 
on the market. After connection, the car screen would synchronize with the 
phone screen,through which drivers of the vehicle could reduce the frequency 
of picking up a smart phone and focus more on driving safe.
    Carlife Vehicle could achieve some specific mobile functions (eg. Connection, 
focus status, wake-up, MIC, Bluetooth, phone call) through bdcf configuration file. 
When the vehicle starts up, it would firstly read the bdcf file, get the channel 
number and configuration of the related features ( refer to bdcf file for details), 
then load the related functions based on the channel number and configuration.
    For the communication protocol between the car terminal and the mobile 
terminal,please refer to<<Baidu CarLife Integration Specification>>

CarLife Vehicle Hardware Requirements:
    CPU : 600HZ+
    USB : USB2.0 HiSpeed Host,Standard USB?A port
    Screen Resolution : Support at least 800*480 @60HZ
    Video : H264 hardware Decoding
    Audio : PCM
    Voice Input : Microphone in
    Type of Control : Touch screen or Physical button
    Telephone : Bluetooth 2.0 
	
To compile run:
    1.Compile in Android Studio
    2.Install APK into the vehicle
    3.Copy bdcf from the catalog of assets to the catalog of /data/local/tmp of the vehicle
    4.Install Carlife in the mobile phone, then connect it with the vehicle through USB.
    5.Start up Carlife of the vehicle
Statement:
    1.For the USB connection,please refer to the Carlife SPC.
    2.Channel IDs in bdcf are used for debugging. For business production, please apply new Channel IDs on the Carlife Official Website.

CarLife homepage:
http://carlife.baidu.com/


README Updated on:
2017-11-09
	