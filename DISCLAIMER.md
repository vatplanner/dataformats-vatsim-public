# Disclaimer and Policies

**This section can also be found on [README.md](README.md).**

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

 * `status.txt` should only be fetched once per application start
 * `vatsim-data.txt` and `vatsim-servers.txt` (not supported yet) should be loaded from randomly chosen servers in a round-robin fashion, i.e. decide a random server each time you download the files
 * obey the minimum `RELOAD` intervals if specified in/parsed from those files

Further or updated policies may be in effect; please read all available VATSIM-provided documents and regularly check the raw source files you are feeding into these parsers.