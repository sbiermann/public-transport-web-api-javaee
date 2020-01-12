Project description
----------------------

This application is a small web api for the [public-transport-enabler](https://github.com/schildbach/public-transport-enabler).


Checkout from GitHub
----------
Don't forget to init and update git submodules for the public-transport-enabler library.

cd public-transport-web-api

git submodule init

git submodule update

Run the application
-------------

```
./gradlew bootRun
```
Test URL for your Browser: http://localhost:8080/publictransportapi/rest/connection?from=6906508&to=6930811&product=T

----------


REST Endpoint description
-------------------
### BASIC URL
The basic url of the following endpoints are publictransportapi/rest, so for example the provider endpoint URL is, publictransportapi/rest/provider. 

### GET:  /provider
 With this endpoint you can get all existing provider which can be used as optional parameter, default provider is VAG Freiburg (Vagfr)  
 
**Example:** 
/provider

----------

### GET:  /station/suggest
 With this endpoint you can get the station ids which you need to use later.  
 
**Parameter:**
 
 1. q -- Name of station you want to search
 2. (optional) providerName -- Name of the provider, for example: NvbwProvider
 3. (optional) locationType -- type of the locations, default: ANY, possible values. ANY, STATION, STREET, POI

**Example:** 
/station/suggest?q=Technisches+Rathaus

----------

### GET: /connection
Lists all trips from one station to another with departure time and line number. 

**Please note that only direct connections will be listed**

 **Parameter:**
 
 1. from -- Station id from departure station
 2. to -- Station id from the arrival station
 3. product -- Product you want to use ( T = Tram, B = Bus)
 4. (optional) timeOffset -- Minutes e.g. you need to walk to the station.
 5. (optional) providerName -- Name of the provider, for example: NvbwProvider
 
**Example:** 
/connection?from=6906508&to=6930811&product=T

----------

### GET: /departure
Lists all departure trains of the given station

 **Parameter:**

 1. from -- Station id from departure station
 2. (optional) providerName -- Name of the provider, for example: NvbwProvider
 3. (optional) limit -- Limit the result set, default 10

**Example:**
/departure?from=6906508

----------

### GET: /connectionEsp
Get next departure time for your trip. Lightwight for easy processing with the ESP8266  
**Please note that only direct connections will be listed**

 **Parameter:**
 
 1. from -- Station id from departure station
 2. to -- Station id from the arrival station
 3. product -- Product you want to use ( T = Tram, B = Bus)
 4. (optional) timeOffset -- Minutes e.g. you need to walk to the station.
 5. (optional) providerName -- Name of the provider, for example: Vagfr
 
**Example:** 
/connectionEsp?from=6906508&to=6930811&product=T&timeOffset=5

----------

### GET: /departureFHEM
Lists all departure trains of the given station in the format for FHEM. http://forum.fhem.de/index.php/topic,48255.0.html

 **Parameter:**

 1. from -- Station id from departure station
 2. (optional) providerName -- Name of the provider, for example: NvbwProvider
 3. (optional) limit -- Limit the result set, default 10

**Example:**
/departureFHEM?from=6906508&limit=6

----------

### GET: /connectionRaw
Lists all trips from one station to another with with all data the public-transport-enabler libary delivers.

 **Parameter:**
 
 1. from -- Station id from departure station
 2. to -- Station id from the arrival station
 3. product -- Product you want to use ( T = Tram, B = Bus)
 4. (optional) timeOffset -- Minutes e.g. you need to walk to the station. 
 5. (optional) providerName -- Name of the provider, for example: NvbwProvider
 
**Example:** 
/connectionRaw?from=6906508&to=6930811&product=T
