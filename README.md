
# File Extension for NetLogo

The file extension gives you the power to interact with outside files.

There are two main modes when dealing with files: reading and writing. The
difference is the direction of the flow of data. When you are reading in
information from a file, data that is stored in the file flows into your model.
On the other hand, writing allows data to flow out of your model and into a file.

When working with files, always begin by using the primitive [file:open](#fileopen). This
specifies which file you will be interacting with. None of the other primitives
work unless you open a file first.

The next primitive you use dictates which mode the file will be in until
the file is closed, reading or writing. To switch modes, close and then reopen
the file.

The reading primitives include [file:read](#fileread), [file:read-line](#fileread-line), [file:read-characters](#fileread-characters),
and [file:at-end?](#fileat-end?) Note that the file must exist already before you can open it
for reading.

> **Code Examples**: File Input Example

The primitives for writing are similar to the primitives that print things in
the Command Center, except that the output gets saved to a file. They include
[file:print](#fileprint), [file:show](#fileshow), [file:type](#filetype), and [file:write](#filewrite). Note that you can never
"overwrite" data. In other words, if you attempt to write to a file with
existing data, all new data will be appended to the end of the file. (If you
want to overwrite a file, use [file:delete](#filedelete) to delete it, then open it for writing.)

> **Code Examples**: File Output Example

When you are finished using a file, you can use the command [file:close](#fileclose) to end
your session with the file. If you wish to remove the file afterwards, use the
primitive [file:delete](#filedelete) to delete it. To close multiple opened files, one needs to
first select the file by using [file:open](#fileopen) before closing it.

```netlogo
;; Open 3 files
file:open "myfile1.txt"
file:open "myfile2.txt"
file:open "myfile3.txt"

;; Now close the 3 files
file:close
file:open "myfile2.txt"
file:close
file:open "myfile1.txt"
file:close
```

Or, if you know you just want to close every file, you can use [file:close-all](#fileclose-all).

Two primitives worth noting are [file:write](#filewrite) and [file:read](#fileread) . These primitives are
designed to easily save and retrieve NetLogo constants such as numbers, lists,
booleans, and strings. [file:write](#filewrite) will always output the variable in such a
manner that [file:read](#fileread) will be able to interpret it correctly.

```netlogo
file:open "myfile.txt"  ;; Opening file for writing
ask turtles
  [ file:write xcor file:write ycor ]
file:close

file:open "myfile.txt"  ;; Opening file for reading
ask turtles
  [ setxy file:read file:read ]
file:close
```

### Letting the user choose

The [[user-directory]], [[user-file]], and [[user-new-file]] primitives are
useful when you want the user to choose a file or directory for your code to
operate on.

## Primitives

### 

[`file:at-end?`](#fileat-end?)
[`file:close`](#fileclose)
[`file:close-all`](#fileclose-all)
[`file:delete`](#filedelete)
[`file:exists?`](#fileexists?)
[`file:flush`](#fileflush)
[`file:open`](#fileopen)
[`file:print`](#fileprint)
[`file:read`](#fileread)
[`file:read-characters`](#fileread-characters)
[`file:read-line`](#fileread-line)
[`file:show`](#fileshow)
[`file:type`](#filetype)
[`file:write`](#filewrite)



### `file:at-end?`

**file:at-end?**


Reports true when there are no more characters left to read in from the current
file (that was opened previously with [file:open](#fileopen)). Otherwise,
reports false.

```
file:open "my-file.txt"
print file:at-end?
=> false ;; Can still read in more characters
print file:read-line
=> This is the last line in file
print file:at-end?
=> true ;; We reached the end of the file
```

See also [file:open](#fileopen), [file:close-all](#fileclose-all).



### `file:close`

**file:close**


Closes a file that has been opened previously with [file:open](#fileopen).

Note that this and [file:close-all](#fileclose-all) are the only ways to restart
to the beginning of an opened file or to switch between file modes.

If no file is open, does nothing.

See also [file:close-all](#fileclose-all), [file:open](#fileopen).



### `file:close-all`

**file:close-all**


Closes all files (if any) that have been opened previously with [file:open](#fileopen).

See also [file:close](#fileclose), [file:open](#fileopen).



### `file:delete`

**file:delete** ***string***


Deletes the file specified as *string*.

*string* must be an existing file with writable permission by the user. Also,
the file cannot be open. Use the command [file:close](#fileclose) to close an
opened file before deletion.

Note that the string can either be a file name or an absolute file path. If it
is a file name, it looks in whatever the current directory is. This can be
changed using the command [[set-current-directory]]. It is defaulted to the
model's directory.



### `file:exists?`

**file:exists?** ***string***


Reports true if *string* is the name of an existing file on the system.
Otherwise it reports false.

Note that the string can either be a file name or an absolute file path. If it
is a file name, it looks in whatever the current directory is. This can be
changed using the command [[set-current-directory]]. It defaults to to the
model's directory.



### `file:flush`

**file:flush**


Forces file updates to be written to disk. When you use [file:write](#filewrite)
or other output commands, the values may not be immediately written to disk.
This improves the performance of the file output commands. Closing a file
ensures that all output is written to disk.

Sometimes you need to ensure that data is written to disk without closing the
file. For example, you could be using a file to communicate with another program
on your machine and want the other program to be able to see the output
immediately.



### `file:open`

**file:open** ***string***


This command will interpret *string* as a path name to a file and open the file.
You may then use the reporters [file:read](#fileread), [file:read-line](#fileread-line),
and [file:read-characters](#fileread-characters) to read in from the file, or
[file:write](#filewrite), [file:print](#fileprint), [file:type](#filetype), or
[file:show](#fileshow) to write out to the file.

Note that you can only open a file for reading or writing but not both. The next
file i/o primitive you use after this command dictates which mode the file is
opened in. To switch modes, you need to close the file using [file:close](#fileclose).

Also, the file must already exist if opening a file in reading mode.

When opening a file in writing mode, all new data will be appended to the end of
the original file. If there is no original file, a new blank file will be
created in its place. (You must have write permission in the file's directory.)
(If you don't want to append, but want to replace the file's existing contents,
use [file:delete](#filedelete) to delete it first, perhaps inside a carefully if you're not
sure whether it already exists.)

Note that *string* can either be a file name or an absolute file path. If it
is a file name, it looks in whatever the current directory is. This can be
changed using the command [[set-current-directory]]. It is defaulted to the
model's directory.

```
file:open "my-file-in.txt"
print file:read-line
=> First line in file ;; File is in reading mode
file:open "C:\\NetLogo\\my-file-out.txt"
;; assuming Windows machine
file:print "Hello World" ;; File is in writing mode
```

Opening a file does not close previously opened files. You can use [file:open](#fileopen)
to switch back and forth between multiple open files.

See also [file:close](#fileclose), [file:close-all](#fileclose-all).



### `file:print`

**file:print** ***value***


Prints *value* to an opened file, followed by a carriage return.

This agent is *not* printed before the value, unlike [file:show](#fileshow).

Note that this command is the file i/o equivalent of print, and [file:open](#fileopen)
needs to be called before this command can be used.

See also [file:show](#fileshow), [file:type](#filetype), [file:write](#filewrite).



### `file:read`

**file:read**


This reporter will read in the next constant from the opened file and interpret
it as if it had been typed in the Command Center. It reports the resulting
value. The result may be a number, list, string, boolean, or the special value
nobody.

Whitespace separates the constants. Each call to [file:read](#fileread) will
skip past both leading and trailing whitespace.

Note that strings need to have quotes around them. Use the command [file:write](#filewrite)
to have quotes included.

Also note that the [file:open](#fileopen) command must be called before this
reporter can be used, and there must be data remaining in the file. Use the
reporter [file:at-end?](#fileat-end) to determine if you are at the end of the
file.

```
file:open "my-file.data"
print file:read + 5
;; Next value is the number 1
=> 6
print length file:read
;; Next value is the list [1 2 3 4]
=> 4
```

See also [file:open](#fileopen), [file:write](#filewrite).



### `file:read-characters`

**file:read-characters** ***number***


Reports the given *number* of characters from an opened file as a string. If
there are fewer than that many characters left, it will report all of the
remaining characters.

Note that it will return every character including newlines and spaces.

Also note that the [file:open](#fileopen) command must be called before this
reporter can be used, and there must be data remaining in the file. Use the
reporter [file:at-end?](#fileat-end) to determine if you are at the end of the
file.

```
file:open "my-file.txt"
print file:read-characters 5
;; Current line in file is "Hello World"
=> Hello
```

See also [file:open](#fileopen).



### `file:read-line`

**file:read-line**


Reads the next line in the file and reports it as a string. It determines the
end of the file by a carriage return, an end of file character or both in a row.
It does not return the line terminator characters.

Also note that the [file:open](#fileopen) command must be called before this
reporter can be used, and there must be data remaining in the file. Use the
reporter [file:at-end?](#fileat-end) to determine if you are at the end of the
file.

```
file:open "my-file.txt"
print file:read-line
=> Hello World
```

See also [file:open](#fileopen).



### `file:show`

**file:show** ***value***


Prints *value* to an opened file, preceded by this agent agent, and followed by
a carriage return. (This agent is included to help you keep track of what agents
are producing which lines of output.) Also, all strings have their quotes
included similar to [file:write](#filewrite).

Note that this command is the file i/o equivalent of show, and [file:open](#fileopen)
needs to be called before this command can be used.

See also [file:print](#fileprint), [file:type](#filetype), [file:write](#filewrite).



### `file:type`

**file:type** ***value***


Prints *value* to an opened file, *not* followed by a carriage return (unlike
[file:print](#fileprint) and [file:show](#fileshow)). The lack of a carriage
return allows you to print several values on the same line.

This agent is *not* printed before the value. unlike [file:show](#fileshow).

Note that this command is the file i/o equivalent of type, and [file:open](#fileopen)
needs to be called before this command can be used.

See also [file:print](#fileprint), [file:show](#fileshow), and [file:write](#filewrite).



### `file:write`

**file:write** ***value***


This command will output *value*, which can be a number, string, list, boolean,
or nobody to an opened file, not followed by a carriage return (unlike
[file:print](#fileprint) and [file:show](#fileshow)).

This agent is *not* printed before the value, unlike [file:show](#fileshow). Its
output also includes quotes around strings and is prepended with a space. It
will output the value in such a manner that [file:read](#fileread) will be able
to interpret it.

Note that this command is the file i/o equivalent of write, and [file:open](#fileopen)
needs to be called before this command can be used.

```
file:open "locations.txt"
ask turtles
  [ file:write xcor file:write ycor ]
```

See also [file:print](#fileprint), [file:show](#fileshow), [file:type](#filetype).





