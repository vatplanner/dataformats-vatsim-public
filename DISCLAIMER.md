# Disclaimer and Policies

**This section can also be found on [README.md](README.md).**

## Use of real-world aviation formats

VATSIM uses some data in the same way as in real-world aviation, for example ICAO flight plan equipment codes (field 10a/10b and PBN remarks), so parsers and data representations for such formats have also been added to this library. Although such data is supported by this library such classes should not be blindly used for real-world aviation. Such "unsafe" classes generally hold an additional disclaimer on class JavaDoc but it could also be missing in some cases. Note that there generally is no warranty given for any code in this library but applying classes/data from a library of an unprofessional simulation environment for real-world aviation use in particular would be highly irresponsible to say the least. Also note that parser/data present in this library may have adjusted to the specific needs of VATSIM, so although they look compatible to real-world scenarios they result in invalid data when applied outside the context of VATSIM/flight simulation. You have been warned...

## Privacy filtering

**Privacy filter is under construction and should not be attempted to be used yet.**

As a fallout from GPDR and local law and regulations, it appeared to make sense to implement a privacy filter to help with removal of some information from data files. However, even in case that it should work properly, it will not magically make your program compliant with any data protection laws. Read the full disclaimer and all JavaDoc documentation before attempting to use it:

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

## Usage and retrieval of VATSIM data

Although this implementation is provided under an open license, usage of the data itself, while being publicly available, is subject to restrictions. Please refer to official VATSIM documents regarding any policies associated with its use.

Some technical policies stressed explicitely on the initial `status.txt` file should be repeated here as otherwise they may go unnoticed when using this library:

 * `status.txt` ~~should only be fetched once per application start~~
   * June 2020: new policy is to check _regularly_ for changes which makes much more sense - restrictions are no longer listed
 * `vatsim-data.txt` and `vatsim-servers.txt` (not supported yet) should be loaded from randomly chosen servers in a round-robin fashion, i.e. decide a random server each time you download the files
   * June 2020: VATSIM now uses server-side load balancing for data feeds, so only one URL is now provided but the feature for cooperative client-side load balancing still exists, so expect to see multiple URLs at some point and continue to choose one randomly even if you currently only see one.
 * obey the minimum `RELOAD` intervals if specified in/parsed from those files
   * June 2020: Temporarilly, reload intervals have been observed to have been practically removed by using floating point values. The limit has however been reset to 1 minute now (was 2 minutes a year ago). There is no longer any mention of reload intervals other than the now unexplained header field. It still does not make sense to fetch more often as current data seems to be updated at most once per minute.

Further or updated policies may be in effect; please read all available VATSIM-provided documents and regularly check the raw source files you are feeding into these parsers.

If you are debugging this library or are wondering about some missing/erroneous data you may want to have a look at the (incomplete) list of datafile [oddities](docs/oddities.md) which have been observed so far while importing larger amounts of live data.

