package org.nlogo.extensions.file

import java.io.File

import org.nlogo.headless.TestLanguage

class Tests extends TestLanguage(Seq(new File("tests.txt").getCanonicalFile))
