# NeuraSampleAndroid

##Introduction
This is a sample code for integrating <a href="http://www.theneura.com/">Neura</a> with a native Android application.<br/>
Go to <a href="https://dev.theneura.com/docs/getstarted">getting started with Neura</a> for more details.

##Requirements 
1. Basic android knowledge.
2. Android studio installed.

##Before you start
Go over the <a href="https://dev.theneura.com/docs/guide/android/sdk">android sdk guide</a>.

After that, you can start playing with this sample.

##Integrate your own credentials in the NeuraSample project
If you wish to take this sample application, and integrate your own application, here are some basic steps that will help you during integration : 

1. <a href ="https://dev.theneura.com/console/new">Add an application</a>(If you haven't registerd to Neura, you'll have to create a new account).
  - Make sure that under 'Tech Info' (2nd section) you're specifying your own 'Application Package Name'. 
  - Under 'Permissions' select the permissions and services you want to receive from Neura.
2. Apply your own definitions to the sample application
  - Replace all occurrences of ```com.neura.sampleapplication``` with your own 'Application Package Name' :
    <br/>a.&nbsp;&nbsp;&nbsp;Application's ```build.gradle``` file.
    <br/>b.&nbsp;&nbsp;&nbsp;```AndroidManifest.xml``` file.
    <br/>c.&nbsp;&nbsp;&nbsp;All classes that have ```package com.neura.sampleapplication```.
  - Open ```strings.xml``` file, and update ```app_uid``` and ```app_secret``` with your own values.
    <br/>Your values can be recieved from <a href="https://dev.theneura.com/console/">Applications console</a>, just copy your uid and secret : <br/>
    ![uid_secret](https://s21.postimg.org/3qpj2gurr/uid_secret.png)
  - Open ```FragmentMain``` file, and copy the permissions you've declared to your application from 'Permissions' section to ```mPermissions``` variable.
    ![permissions](https://s17.postimg.org/uwq3v3te7/Screen_Shot_2016_08_30_at_1_27_59_PM.png)
  - Follow our <a href="https://dev.theneura.com/docs/guide/android/pushnotification"> push notification guide</a> in order to generate 'Project Number' and 'server key'.
    <br/>a.&nbsp;&nbsp;&nbsp;<a href ="https://dev.theneura.com/console">Open your project</a> and set 'server key' to 'Android Push Credentials' under 'Tech Info' section.
    <br/>b.&nbsp;&nbsp;&nbsp;Open ```strings.xml``` file and set your own 'Project Number' to ```google_api_project_number``` resource.

##Support
You can ask question and view existing questions with the Neura tag on <a href="https://stackoverflow.com/questions/tagged/neura?sort=newest&pageSize=30">StackOverflow</a>.
