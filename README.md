# GoodnotesPDFMerger
A tool written in java to merge lectures slides onto a background image, such as the document page of Goodnotes :).

## usage

```
usage: java -jar GoodnotesPDFMerger -args
List of options for GoodnotesPDFMerger.jar
 -f,--filename <arg>   Specify which file is used as background. An example is "Goodnotes.pdf" or "Goodnotes.png".
                       Beware that when choosing -s the default file is "Goodnotes.pdf" whereas for -m
                       is "Goodnotes.png".
 -h,--help             Print help message
 -m,--merging          Uses images merging technique to merge slides onto background.
 -n,--number <arg>     Specify how many slides per page it needs to use. (Default is 3)
 -s,--superimposing    Uses pdfs superimposing technique to merge slides onto background.
Either one of -m or -s is mandatory, if you use both at the same time by default superimpose will be used.

```
