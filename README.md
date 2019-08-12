# TapIt 

This is an application to show all the water coolers that is available in SP.
As of now, for proof of concept, the water coolers that I will display will only be the area between t12 and t15.

## Required API:
1. Google Map
2. GeoLocation
3. GeoFencing
4. Distance Check ( _If this even exist_)

Flow of the whole App:
1. Using a boolean to check if the user is using the app for the first time

+ 1.1 If True (User using for the first time), Prompt a window for user to enter their loction or a button to get current location and store it in firebase. [Geofencing around 5m radius around the recorded location] ( **Purpose** :Remind user to bring their water bottle)

+ 1.2 If False (User has used the app before), Directly display the MapLayout [set default map display to sp with the sp map overlay]

2. **Map Activity** will display markers with nearby water cooler [Record water cooler loction with latlng from interent]

+ 2.1 When user click the button - " Find closest water cooler ", it will first calculate the distance between the user and all the water coolers available and return the location with a pop up window

+ 2.2 When user tap on the marker itself, it will pop out a window with picture and the location displayed of the water cooler

3. **AI Acitivty** User has to manually enter their data with the following parameter - date( **set the first date as day 1**), amount of water refill. **[PROBABLY NO TIME TO IMPLEMENT]**

+ 3.1 Click the button - "save and train" to train the model.

+ 3.2 Model will predict when the user is going to drink water and remind them to **refill** their water bottle

4. **Change location activtiy** Give user the chance to change their home location.
