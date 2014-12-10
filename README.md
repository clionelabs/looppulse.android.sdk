<h1>LoopPulse Android SDK</h1>

<h2>Quick Start</h2>
  1. Create a LoopPulse instance, passing in application context and LoopPulseListener:

    ```LoopPulse loopPulse = new LoopPulse(context, listener);```

  2. Authenticate with application ID and appliation Token:
    
    ```loopPulse.authenticate(APPLICATION_ID, APPLICATION_TOKEN);```

  3. Authentication is an asynchronous call, and the ```onAuthenticated()``` method in your LoopPulseListener will be fired up when it's ready. Inside ```onAuthenticated()```, you can start location monitoring:

    ```
    @Override
    public void onAuthenticated() {
      loopPulse.startLocationMonitoring();
    }
    ```
    
<h2>What's Next</h2>

  1. ```startLocationMonitoring()``` need not to be called inside onAuthenticated callback. Depending on your application needs, you can start monitoring at later time. You can also stop monitoring by calling ```loopPulse.stopLocationMonitoring()```. 
  
  2. LoopPulse will automatically assign a unique visitor ID for each mobile user. More specifically, our visitor ID binds to a single device. If your application has its own membership system (e.g. your app require user login), and you want to associate your own member ID to ours, you can call ```loopPulse.identifyUser(YOUR_MEMBER_ID);```. You will then be able to reference the information in the application dashboard. You can also tag a user with any custom data using ```loopPulse.tagVisitor(properties);``` where ```properties``` is key-value pairs.

  3. LoopPulseListner is a very important tool for you to debug your application or to understanding what's happening inside the LoopPulse blackbox; You can listen to the following events and responds accordingly:
    
    - ```onAuthenticated()``` when authentication is successfully returned
    - ```onAuthenticationError(String msg)``` when authentication is failed
    - ```onMonitoringStarted()``` when monitoring has started successfully when you call ```loopPulse.startLocationMonitoring()```
    - ```onMonitoringStopped()``` when monitoring has stopped successfully when you call ```loopPulse.stopLocationMonitoring()```
    - ```onBeaconDetected(BeaconEvent event)``` when a beacon event (ENTER/EXIT) is fired 

  4. LoopPulseListener can be changed after LoopPulse instance being instantiated:
    
    ```loopPulse.setLoopPulseListener(listener );```

<h2>Monitoring Mechanics</h2>
  The following events will happen behind the scence after ```startLocationMonitoring()``` is being called:
  
  1. The SDK will scan for the configured beacons periodically to detect ENTER/EXIT events. Once an event is detected, it will be fired up to the backend processing server (i.e. reflected in application dashboard). Monitoring will still function even when the app is in background.
  
  2. An exponential backoff mechanism is implemented for power consumption consideration. It starts scanning in 1 second after ```startLocationMonitoring()``` is called. If no beacone events are detected, it will then wait for 2 seconds before doing another scan. It continues for 4 seconds, 8 seconds, so and so, up until 30mins (which would be the maximum delay). If an event is detected, meaning that the users are probably inside active zones, the waiting time goes back again to 1 second and everything is started over.
  
  3. If you have configured the location coordinates (i.e. latitude, longitude and radius) in your backend, then the SDK will also automatically register Geofences to guard the regions. If we detect the user has entered the geo area, it will start scanning immediately with 1 second delay again (then exponentially backing-off).
  
