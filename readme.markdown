# Knockoff - Markdown in Scala

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
