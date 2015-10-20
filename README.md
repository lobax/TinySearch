# TinySearch

A simple search engine, made as a project for the course Algorithms and Data structures (ID1020).
Search for a word, and it will list the documents that the searched term occurs in. It also supports
some basic operations and orderings - see **Query Syntax** for more on the query language. 

Full specifications for this project can be found at https://github.com/lobax/TinySearch/blob/master/id1020-proj2.pdf

##Query syntax
The languange supports three basic operations: 
* Union         (|)
* Intersection  (+)
* Difference    (-)

The operations and the search terms need to be typed in prefix notation. 

For example: If you wish to search for all documents that contain the both the words *delicious* and *vegetable*, you should type *+ delicious vegetable*. If you wish find all documents that countain the word *delicious*, but not *bacon*, you typ *- delicious bacon*.  

The resulting list of documents can be ordered in various ways, by appending *orderby property ordering* to the end of the query. These are the supported orderings: 

Property | Ordering
------------ | -------------
relevance| asc
popularity | desc

Real word example: *friendship orderby popularity desc* 

##FAQ
> How do I run it? 

An executable jar file can be found under *target*. 



