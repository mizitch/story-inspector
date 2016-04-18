# story-inspector
A platform for analyzing works of fiction and a set of tools built on that platform.

TODO: fill out this file :)

Story Inspector's main purpose is to execute a report against a story. A report is (mostly) just a collection of analyzers. An analyzer examines a story and produces comments on the story (which can be output as comments in a word document) as well as a summary (which can be output as headings, text, images, tables, etc in a word document). 

Story Inspector currently supports executing a report against a story, as well as a UI for creating, editing, opening and saving reports. Currently only one analyzer type exists, a relatively simple word searcher. I decided to prioritize building out the platform before creating a bunch of analyzers (mostly because if I let myself go crazy on analyzers I would end up with a cool command line tool and not enough motivation to do the hard work of building out the UI).

See https://github.com/mizitch/story-inspector/wiki/Analyzer-plan for a sketch of the plan for supported analyzers.

It has incomplete unit tests currently (relatively complete coverage of a couple of packages) and a couple of "limited integration" tests. I want to write UI tests for it, probably using the TextFX framework.

I still need to write a developer setup wiki. A few brief notes on development:
* It uses Maven for build/dependency management. If you don't know what that is, look it up.
* Run the command 'mvn jfx:jar' to create a jar file in target/site/jfx
* You can also just use eclipse (or another IDE) to run MainApp.java's main method. That is the approach I used mostly while developing
* In the src/main/test directory there is an eclipse junit test launcher configuration file. It has some arguments/options set that may be required to execute certain tests in eclipse. If you want to use another IDE, it should be possible to open that file and reverse engineer what kind of test configuration setup needs to be done.
* I have developed this entirely on Ubuntu 14.01, so I'm not sure how portable development is (although in theory, it should work on anything that runs Maven and Java). Guessing there will be some quirks.
