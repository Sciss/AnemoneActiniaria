# Anemone Actiniaria

[![Build Status](https://github.com/Sciss/AnemoneActiniaria/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/AnemoneActiniaria/actions?query=workflow%3A%22Scala+CI%22)

This project contains my personal set-up for the algorithmic live improvisation duo _Anemone Actiniaria_.
It is an extension of [Wolkenpumpe](https://github.com/Sciss/Wolkenpumpe).

All code here
is (C)opyright 2014&ndash;2021 by Hanns Holger Rutz. All rights reserved. This project is released under the
[GNU General Public License](https://github.com/Sciss/AnemoneActiniaria/raw/main/LICENSE) v3+ and comes with absolutely no warranties.
To contact the author, send an e-mail to `contact at sciss.de`.

## building

Builds with sbt against Scala 2.12. Use `sbt run`, or `sbt assembly` to create a self-contained jar (you may need the
latter if you want to add the tablet library to the system's class path).

## running

To use the Wacom controls, add environment variable `LD_LIBRARY_PATH=lib` to the JVM run
(currently only Linux native library included).

## network connection

To David:

    zita-j2n --chan 4 192.168.102.135 57130

From David:

    zita-n2j --chan 1,2,3,4 --buff 40 192.168.102.77 57130

