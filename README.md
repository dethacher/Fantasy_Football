# Fantasy_Football

## About:
This is code is quick and very dirty attempt at automating a long term keeper fantasy football league. It is mostly written from example code and needs to be cleaned up at some point.

## Setup:
``` sudo apt-get install libreoffice groovy ```

``` sudo apt-get install libreoffice-java-common ```

## How to do LTCs automatically:
- Create file called ltc.csv
- Add one entry per line using the following format
  -  Player Name, LTC (xx-yy), Team, Cost
  -  Ex. Tom Brady, LTC (20-22), A Flawed Matrix, 15

## Workflow:
- Change directory to source folder
  - ``` cd src ```
- Run script
  - ``` groovy -classpath ".:/usr/lib/libreoffice/program/:$(printf %s: /lib/libreoffice/program/classes/*.jar)" offseason.groovy ```
- LTC call out (Nothing is really done with this.)
- Draft
- Manual calculation of waivers.

