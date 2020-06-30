# Oddities observed on live data

While I don't have any insight into internals, VATSIM data files obviously appear to be composed by merging multiple data sources which can run out of sync. Every so often this process fails with obscure errors and inconsistencies which could be believed to be errors on the consumer side (i.e. in this library's parser, graph import or in applications applying the library) when it's really the data file that is partially corrupted.

Some of these errors are so frequent that there are workarounds on graph import (such as all flight plan data blanking out for a few minutes or even hours).

After chasing too many of these issues I decided collecting them here to prevent someone else from falling into the same traps in the future and spending too much time on debugging.

## same pilot session seen with multiple callsigns/flights

In general, it seems to be a reasonable assumption that pilots only connect with one callsign at a time: There is no way of changing callsigns after login and VATSIM generally only allows a single connection per VATSIM ID at a time (unless special PR accounts are used, which is a very rare sight).

While fixing #7 it was discovered that although this assumption may be valid on actual connections, the data file may contain contradictory garbage nevertheless:

 - pilot was logged in with one callsign ABC123 and had a filed flight plan, the flight completed
 - pilot logs out and back in to change to a new callsign ABC456 without a flight plan

Observed in data file from March 2020:

 - data file 1: (old flight)
   - only ABC123 is present with flight plan, logon time A
 - data file 2: (new flight)
   - pilot shows up twice using callsigns ABC123 and ABC456 at the same time
   - both entries have logon time B
   - ABC123 still has flight plan and position data (geo coordinated, altitude, QNH, heading, speed)
   - ABC456 has neither flight plan nor position data
 - data file 3:
   - still at logon time B
   - ABC123 retains the flight plan but has lost position data
   - ABC456 still has no flight plan but all position data
 - data file 4:
   - ABC123 has been removed (data file is clean again)
