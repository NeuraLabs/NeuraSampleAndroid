# NeuraSampleAndroid

<img src="https://user-images.githubusercontent.com/5232799/43466925-457f4b5a-94e9-11e8-995b-afa052cf9f15.png" alt="Neura is connected" width="210" height="350"> <img src="https://user-images.githubusercontent.com/5232799/43466924-45448dbc-94e9-11e8-9c6e-d66f360200dd.png" alt="Neura is disconnected" width="210" height="350">

## Introduction
This is a sample code for integrating <a href="http://www.theneura.com/">Neura</a> with a native Android application.<br/>
Go to the<a href="https://dev.theneura.com/tutorials/android"> Android tutorial</a> for more details.

## Requirements
1. Basic android knowledge.
2. Android studio installed.

## Before you start
1. Go over the <a href="https://dev.theneura.com/tutorials/android">Android tutorial</a>.
2. Neura sdk has fully methods and classes reference, <a href ="http://docs.theneura.com/android/">check it out</a>

After that, you can start playing with this sample.

## Integrate your own credentials in the NeuraSample project
If you wish to take this sample application, and integrate your own application, here are some basic steps that will help you during integration :

1. <a href ="https://dev.theneura.com/signup/">Subscribe</a> to our developer website Add an application.
2. Apply your own definitions to the sample application
  - Replace all occurrences of ```com.neura.sampleapplication``` with your own 'Application Package Name' :
    <br/>a.&nbsp;&nbsp;&nbsp;Application's ```build.gradle``` file.
    <br/>b.&nbsp;&nbsp;&nbsp;```AndroidManifest.xml``` file.
    <br/>c.&nbsp;&nbsp;&nbsp;All classes that have ```package com.neura.sampleapplication```.
  - Update the App ID and Secret you received after you <a href ="https://dev.theneura.com/app/new">created a new app</a>. You can also do it by using the <a href ="https://dev.theneura.com/tutorials/android">tutorial</a>

## Support
1. You can read classes and api methods at <a href ="http://docs.theneura.com/android/">Neura Sdk Reference</a>.
2. Contact us at https://support.theneura.com
