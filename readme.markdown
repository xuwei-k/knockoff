# Knockoff - Markdown in Scala

[![Build Status](https://travis-ci.org/foundweekends/knockoff.svg?branch=master)](https://travis-ci.org/foundweekends/knockoff)
[![scaladoc](http://javadoc-badge.appspot.com/org.foundweekends/knockoff_2.12.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.foundweekends/knockoff_2.12/knockoff/index.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.foundweekends/knockoff_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.foundweekends/knockoff_2.12)


This is a simple Markdown to object model to XHTML system.

```scala
import knockoff.DefaultDiscounter._

toXHTML(knockoff("""# My Markdown Content """))
```

You can use the blocks returned from the `knockoff` method to do useful things, like fetch the header:

```scala
val blocks = knockoff("""# My markdown""")
blocks.find( _.isInstanceOf[Header] ).map( toText ).getOrElse( "No header" )
```
