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
