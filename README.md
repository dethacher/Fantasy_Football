# Fantasy_Football

## About
This is code is quick and very dirty attempt at automating a long term keeper fantasy football league. It is mostly written from example code and needs to be cleaned up at some point.

## Setup
``` sudo apt-get install libreoffice libreoffice-java-common groovy ```

## How to do Keepers automatically: (no longer needed?)
1. In Chrome go to: https://fantasy.nfl.com/league/2393954/manage/keeperview
2. Right click in gray area and select Save-as..
3. Save file to desired location.
4. Rename downloaded html page to keepers.html (Do not need downloaded folder.)

## How to do LTCs automatically:
- Create file called ltc.csv
- Add one entry per line using the following format
  -  Player Name, LTC (xx-yy), Team, Cost
  -  Ex. Tom Brady, LTC (20-22), A Flawed Matrix, 15
 
 ## How to run:
 1. Create Linux VM
 2. Install groovy
 3. Open commandline
 4. Run: groovy test.groovy (Need keeper.html(?) and ltc.csv files in same directory.)

## Workflow:
- Run script with keepers and automatic drop disabled for keeper and penalty values.
  - ``` groovy -classpath ".:/usr/lib/libreoffice/program/:$(printf %s: /lib/libreoffice/program/classes/*.jar)" test.groovy ```
- Run script with keepers.html and automatic drop for final keepers values, budget and list of dropped players. (Manual entry of all of this. NFL.com will handle keepers but not values.)
  - ``` groovy -classpath ".:/usr/lib/libreoffice/program/:$(printf %s: /lib/libreoffice/program/classes/*.jar)" test.groovy final ```
- LTC call out (Nothing is really done with this.)
- Draft
- Manual calculation of waivers.

