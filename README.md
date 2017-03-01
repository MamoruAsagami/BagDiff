# BagDiff
BagDiff is a JAVA GUI program to compare two bag files, new and old, and report the differences. In a way, BagDiff resembles Unix, Linux comm command.  Unlike comm command, input files don't have to be sorted in advance.
## 1 How to run
1. From command line shell, type java -jar BagDiff.jar
2. From GUI shell, double-click BagDiff.jar icon.

It runs in GUI mode.

## 2 GUI main window
 Element  | Description
----------|-------------
New Document   | New bag file, each line is an element of new bag.  Use the button to pop up a file browser.  You can drop a file icon into the text field, as well.
Old Document   | Old bag file, each line is an element of old bag.  Use the button to pop up a file browser.  You can drop a file icon into the text field, as well.
Out Document  | Output file to write report.  Use the button to pop up a file browser.  You can drop a file icon into the text field, as well.
Encoding.New  | New Document file encoding. Automatic means to use heuristic logic to decide the file encoding. Default means the system default file encoding.
Encoding.Old  | Old Document file encoding. Automatic means to use heuristic logic to decide the file encoding. Default means the system default file encoding.
Encoding.Out | Output file encoding. Automatic means to use the same encoding as New Document. Default means the system default file encoding.
Locale   | Locale used to sort bag documents before comparing.
Select.New | New elements, which are in New Document and not in Old Document, are copied to Out Document.
Select.Old | Old elements, which are in Old Document and not in New Document, are copied to Out Document.
Select.Common | Common elements, which are in both New and Old Document, are copied to Out Document.
Prefixes.New | Prefix prepended to new elements. "\t" is translated to tab code.
Prefixes.Old | Prefix prepended to old elements. "\t" is translated to tab code.
Prefixes.Common | Prefix prepended to common elements. "\t" is translated to tab code.
Diff | Starts to make diff report.

## 3 Example

The following table shows an example of New Document, Old Document and Out Document, respectively.

New Document  | Old Document | Out Document
--------------|--------------|---------------
 a<br>b<br>b<br>c<br>d | e<br>d<br>c<br>b |\++a<br>\==b<br>\++b<br>\==c<br>\==d<br>\--e

