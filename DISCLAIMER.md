# Disclaimer and Policies

**This section can also be found on [README.md](README.md).**

Although this implementation is provided under an open license, usage of the data itself, while being publicly available, is subject to restrictions. Please refer to official VATSIM documents regarding any policies associated with its use.

Some technical policies stressed explicitely on the initial `status.txt` file should be repeated here as otherwise they may go unnoticed when using this library:

 * `status.txt` should only be fetched once per application start
 * `vatsim-data.txt` and `vatsim-servers.txt` (not supported yet) should be loaded from randomly chosen servers in a round-robin fashion, i.e. decide a random server each time you download the files
 * obey the minimum `RELOAD` intervals if specified in/parsed from those files

Further or updated policies may be in effect; please read all available VATSIM-provided documents and regularly check the raw source files you are feeding into these parsers.
