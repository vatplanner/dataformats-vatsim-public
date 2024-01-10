# Data Format Parsers & Entities for Publicly Available VATSIM Information

[![Coverage Status](https://coveralls.io/repos/github/vatplanner/dataformats-vatsim-public/badge.svg?branch=master)](https://coveralls.io/github/vatplanner/dataformats-vatsim-public?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/org.vatplanner/dataformats-vatsim-public.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=org.vatplanner%3Adataformats-vatsim-public)
[![JavaDoc](https://javadoc.io/badge2/org.vatplanner/dataformats-vatsim-public/javadoc.svg)](https://javadoc.io/doc/org.vatplanner/dataformats-vatsim-public)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE.md)

This library's goal is to provide parsers and entities for data formats used by [VATSIM](https://www.vatsim.net/)
and provided *publicly*.
It's *not* possible to connect to the actual network using this library as VATSIM uses an undisclosed (NDA'd) network
protocol and the network is only permitted to be connected to with authorized software. Actually, this library doesn't
connect at all, it just parses and will require you to fetch all source data and feed it into the parser yourself... :)

The scope of this project is rather small but an essential part of a bigger project I'm working on (VATPlanner).
Ut still seemed like a good idea to split the data parsers & entities apart to be easily reusable if needed in other
projects.

## Features

Implemented:

- parsers to work with raw data:
    - VATSIM status information (data feed):
        - `NetworkInformation` holds basic meta data such as URLs concerning data feed and other VATSIM services and is
          provided through initial `status.txt` or `status.json` files
        - `DataFile`s (the so called "data feed") contain all ATC stations & pilots currently online as well as pre-filed
          flight plans and can be parsed from publicly accessible JSON data feed file. Legacy `vatsim-data.txt` files are
          also still supported.
        - legacy version 8/9 and JSON v3 formats are supported
    - VATSIM online transceivers
    - [VAT-Spy data](https://github.com/vatsimnetwork/vatspy-data-project):
        - `VatSpyFileParser` parses the `VATSpy.dat` file holding information about countries, airports, FIRs/UIRs and the
          international date line
        - `FIRBoundaryFileParser` parses the `FIRBoundaries.dat` file holding more detailed sector information and outlines
- `NetworkInformation` and `DataFile`s can also be encoded to legacy format version 8/9
- `GraphImport` imports parsed `DataFile`s (snapshots in time) to a deduplicated graph structure
    - splits connections spanning multiple flights
    - continues flights on client connection losses
    - attempts to continue previous flight if data file splits information
    - ignores service clients (AFVDATA etc.)

Planned:

- parsing of EuroScope sector files (defining VATSIM air spaces and airport ground layouts)
- encoding of `NetworkInformation` and `DataFile` to JSON formats
- privacy filter to help removing personal information from data files
    - *do not use the existing implementation* (it was never ready for production) as it is subject to a full rewrite
      because the old approach can no longer be applied to JSON formats

## Build instructions

Due to recent ongoing refactoring you will need to locally install the latest development version (`0.1-SNAPSHOT`)
of [VATPlanner Commons](https://github.com/vatplanner/vatplanner-commons) until it gets published to Maven Central.

## Current API State

API is currently not stable and may change without notice. Restructuring of the whole project is currently pending
(#18). A stable release should be expected after restructuring is complete; `0.1-pre`-releases are published prior to
that.

**Privacy filter was under construction and is now deprecated as it requires a major rework due to JSON format
introduction. It should not be used in current state.**

## Examples and Development Tools

See [dataformats-vatsim-public-examples](https://github.com/vatplanner/dataformats-vatsim-public-examples) for a
collection of development tools which may also be useful to look into as "example code" on how to use this library.

## License

The implementation and accompanying files are released under [MIT license](LICENSE.md). Parsed data is subject to
policies and restrictions, see the disclaimer below.

## Disclaimer and Policies

**This section is a copy of [DISCLAIMER.md](DISCLAIMER.md).**

### Use of real-world aviation formats

VATSIM uses some data in the same way as in real-world aviation, for example ICAO flight plan equipment codes
(field 10a/10b and PBN remarks), so parsers and data representations for such formats have also been added to this
library. Although such data is supported by this library such classes should not be blindly used for real-world
aviation. Such "unsafe" classes generally hold an additional disclaimer on class JavaDoc but it could also be missing in
some cases. Note that there generally is no warranty given for any code in this library but applying classes/data from a
library of an unprofessional simulation environment for real-world aviation use in particular would be highly
irresponsible to say the least. Also note that parser/data present in this library may have adjusted to the specific
needs of VATSIM, so although they look compatible to real-world scenarios they result in invalid data when applied
outside the context of VATSIM/flight simulation. You have been warned...

### Privacy filtering

**Privacy filter was under construction and is now deprecated as it requires a major rework due to JSON format
introduction. It should not be used in current state.**

As a fallout from GPDR and local law and regulations, it appeared to make sense to implement a privacy filter to help
with removal of some information from data files. However, even in case that it should work properly, it will not
magically make your program compliant with any data protection laws. Read the full disclaimer and all JavaDoc
documentation before attempting to use it:

```
THERE IS NO GUARANTEE THAT THE PROVIDED PRIVACY FILTERING MECHANISM WORKS
RELIABLY, NEITHER IN TERMS OF PROTECTING PRIVACY NOR MAINTAINING INTEGRITY OR
AVAILABILITY OF DATA.

IT IS ONLY TO BE UNDERSTOOD AS A SINGLE FIRST TOOL YOU CAN UTILIZE IN YOUR
OWN EFFORTS TO BUILD, OPERATE AND MAINTAIN A CHAIN OF COMPONENTS PROCESSING
AND STORING DATA IN A LEGALLY COMPLIANT, TRUSTWORTHY AND RESPONSIBLE WAY
THROUGHOUT YOUR ENTIRE INDIVIDUALLY CRAFTED SYSTEM. APPLYING THIS FILTER,
EVEN WHEN WORKING AS DESIRED, CAN NOT FREE YOU FROM YOUR OWN
RESPONSIBILITIES.

CONFIGURATION OF THE FILTER AS WELL AS ALL FURTHER HANDLING OF DATA REQUIRES
YOUR OWN JUDGMENT AND PRE-CAUTIONS. BY COMMON SENSE, ALL DATA SHOULD BE
REDUCED FURTHER WHENEVER POSSIBLE, WHICH IS BEYOND THE CAPABILITIES PROVIDED
BY THIS INITIAL FILTER.

PROCESSING ANY INFORMATION REMAINING AFTER APPLYING THE FILTER STILL REQUIRES
INDIVIDUAL ASSESSMENT AND IS YOUR OWN RESPONSIBILITY!

FILTERED DATA OBVIOUSLY REMAINS SUBJECT TO VATSIM POLICIES AND RESTRICTIONS.
EXPLANATIONS OF CONFIGURATION OPTIONS AS WELL AS THIS DISCLAIMER ITSELF ONLY
DESCRIBE THOUGHTS AND OBSERVATIONS BY PROGRAM AUTHORS AND ARE NOT TO BE
CONFUSED WITH LEGAL ADVICE WHICH CANNOT BE PROVIDED.
```

### Usage and retrieval of VATSIM data

Although this implementation is provided under an open license, usage of the data itself, while being publicly
available, is subject to restrictions. Please refer to official VATSIM documents regarding any policies associated with
its use.

Some technical policies stressed explicitely on the initial `status.txt` file should be repeated here as otherwise they
may go unnoticed when using this library:

* `status.txt` ~~should only be fetched once per application start~~
    * June 2020: new policy is to check _regularly_ for changes which makes much more sense - restrictions are no longer
      listed
* `vatsim-data.txt` and `vatsim-servers.txt` (not supported yet) should be loaded from randomly chosen servers in a
  round-robin fashion, i.e. decide a random server each time you download the files
    * June 2020: VATSIM now uses server-side load balancing for data feeds, so only one URL is now provided but the
      feature for cooperative client-side load balancing still exists, so expect to see multiple URLs at some point and
      continue to choose one randomly even if you currently only see one.
* obey the minimum `RELOAD` intervals if specified in/parsed from those files
    * June 2020: Temporarily, reload intervals have been observed to have been practically removed by using floating
      point values. The limit has however been reset to 1 minute now (was 2 minutes a year ago). There is no longer any
      mention of reload intervals other than the now unexplained header field. It still does not make sense to fetch more
      often as current data seems to be updated at most once per minute.
    * April 2021: Multiple official sources have recently communicated update and fetch intervals of 15 seconds but the
      reload interval field on JSON v3 format still exists with a granularity of minutes and has not changed (indicated
      minimum interval is still 1 minute). Check latest official VATSIM policies and statements on how to determine the
      actual reload interval wanted by VATSIM.

Further or updated policies may be in effect; please read all available VATSIM-provided documents and regularly check
the raw source files you are feeding into these parsers.

If you are debugging this library or are wondering about some missing/erroneous data you may want to have a look at the
(incomplete) list of datafile [oddities](docs/oddities.md) which have been observed so far while importing larger
amounts of live data.
