# NeuraSampleAndroid

<img src="https://s11.postimg.org/gtxw1agb7/neura_login.png" alt="neura_login" width="210" height="350">
<img src="https://s14.postimg.org/fmnzmnxox/situation.png" alt="situation" width="210" height="350">
<img src="https://s21.postimg.org/9yeirqdc7/missing_gym.png" alt="missing_gym" width="210" height="350">

##Introduction
This is a sample code for integrating <a href="http://www.theneura.com/">Neura</a> with a native Android application.<br/>
Go to <a href="https://dev.theneura.com/docs/getstarted">getting started with Neura</a> for more details.

##Requirements 
1. Basic android knowledge.
2. Android studio installed.

##Before you start
1. Go over the <a href="https://dev.theneura.com/docs/guide/android/sdk">android sdk guide</a>.
2. Neura sdk has fully methods and classes reference, <a href ="http://docs.theneura.com/android/com/neura/standalonesdk/service/NeuraApiClient.html">check it out</a>

After that, you can start playing with this sample.

##Integrate your own credentials in the NeuraSample project
If you wish to take this sample application, and integrate your own application, here are some basic steps that will help you during integration : 

1. <a href ="https://dev.theneura.com/console/new">Add an application</a>(If you haven't registers to Neura, you'll have to create a new account).
  - Make sure that under 'Tech Info' (2nd section) you're specifying your own 'Application Package Name'. 
  - Under 'Permissions' select the permissions and services you want to receive from Neura.
2. Apply your own definitions to the sample application
  - Replace all occurrences of ```com.neura.sampleapplication``` with your own 'Application Package Name' :
    <br/>a.&nbsp;&nbsp;&nbsp;Application's ```build.gradle``` file.
    <br/>b.&nbsp;&nbsp;&nbsp;```AndroidManifest.xml``` file.
    <br/>c.&nbsp;&nbsp;&nbsp;All classes that have ```package com.neura.sampleapplication```.
  - Open ```strings.xml``` file, and update ```app_uid``` and ```app_secret``` with your own values.
    <br/>Your values can be received from <a href="https://dev.theneura.com/console/">Applications console</a>, just copy your uid and secret : <br/>
    ![uid_secret](https://s21.postimg.org/3qpj2gurr/uid_secret.png)
  - Open ```FragmentMain``` file, and copy the permissions you've declared to your application from 'Permissions' section to ```mPermissions``` variable.<br/>
    <img src="https://s17.postimg.org/uwq3v3te7/Screen_Shot_2016_08_30_at_1_27_59_PM.png" alt="permissions_list" width="600" height="150">
  - In order to receive events from Neura, follow our <a href="https://dev.theneura.com/docs/guide/android/pushnotification"> push notification guide</a> to integrate <a href="https://firebase.google.com/docs/cloud-messaging/">Firebase Cloud Messaging</a>.

##Testing while developing
Obviously, it's not very convenient for a developer to receive events on realtime, so, Neura has generated 
an events simulation, and you can connect it with your application by calling : ```mNeuraApiClient.simulateAnEvent();``` 
and for example, generate the event : 'UserStartedWalking'.<br/>
FYI, you need to be logged in to Neura in order to simulate an event.<br/> 
You can read about it more on <a href ="http://docs.theneura.com/android/com/neura/standalonesdk/service/NeuraApiClient.html#simulateAnEvent--">Simulate event method</a>.

##Support
1. Go to <a href="https://dev.theneura.com/docs/getstarted">getting started with Neura</a> for more details.
2. You can read classes and api methods at <a href ="http://docs.theneura.com/android/com/neura/standalonesdk/service/NeuraApiClient.html">Neura Sdk Reference</a>.
3. You can ask question and view existing questions with the Neura tag on <a href="https://stackoverflow.com/questions/tagged/neura?sort=newest&pageSize=30">StackOverflow</a>.
