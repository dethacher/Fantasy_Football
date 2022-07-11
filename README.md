# Fantasy_Football

## About:
This code is a quick and dirty attempt at automating a long term keeper fantasy football league.

## Setup:
``` sudo apt-get install libreoffice groovy git ```

``` sudo apt-get install libreoffice-java-common ```

``` git clone https://github.com/dethacher/Fantasy_Football.git ```

``` cd Fantasy_Football ```

## How to do LTCs automatically:
- Create file called ltc.csv in data folder
- Add one entry per line using the following format
  -  Player Name,LTC (xx-yy),Team,Cost
  -  Ex. Tom Brady,LTC (20-22),A Flawed Matrix,15
  
## How to do Name Changes automatically:
- Create file called name_changes.csv in data folder
- Add one entry per line using the following format
  -  New Name,Old Name
  -  Ex. Team 8,Choco Chips

## Workflow:
- Run script
  - ``` groovy -classpath ".:/usr/lib/libreoffice/program/:$(printf %s: /lib/libreoffice/program/classes/*.jar)" offseason.groovy ```
- LTC call out (Nothing is really done with this.)
- Draft
- Manual calculation of waivers.

## Future:
Waivers and LTC?

