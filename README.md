# Ecore2Tg

Ecore2Tg allows it to transform Ecore models to TGraph schemas and vice versa. Ecore instances can be transformed to TGraphs and vice versa, according to the schema transformation. 

## Installation and Building

The ecore2tg project depends on the `jgralab` project and the `common` project. In addition, it depends on the following Eclipse plugins:

* [`org.eclipse.emf.ecore`](git://git.eclipse.org/gitroot/emf/org.eclipse.emf.git)
* [`org.eclipse.emf.ecore.xmi`](git://git.eclipse.org/gitroot/emf/org.eclipse.emf.git)
* [`org.eclipse.emf.ecore.common`](git://git.eclipse.org/gitroot/emf/org.eclipse.emf.git)

There are two different ways of building and using ecore2tg: 

1. building `ecore2tg` with ant and use it as library  
2. install `ecore2tg` as Eclipse plugin and use the provided wizards

### Building as library using ant

Checkout the `jgralab` project and the `common` project into the same folder and build them with ant according to their building instructions. 

Checkout the `ecore2tg` project to the same folder.
Create a new folder `lib` in the `ecore2tg` project and copy the Eclipse plugins to this folder.
Go into the `ecore2tg` folder and run `ant` in a console.

### Using as Eclipse plugin

Download `Eclipse for RCP and RAP Developers` from the [Eclipse website](http://www.eclipse.org/downloads/). Install `EMF - Eclipse Modeling Framework SDK` and `Ecore Tools SDK` via the update page.

Checkout the `common` project, the `jgralab` project and the `jgralab4eclipse` project and install them by following their installing instructions. 

Checkout the `ecore2tg` project.

Import the `jgralab4eclipse` project and the `ecore2tg` project into your Eclipse workspace. 

Open the `MANIFEST.MF` file within Eclipse. Start a new Eclipse application by pressing `Launch an Eclipse application`.

In the new Eclipse instance, you can find the `ecore2tg` wizards by selecting File->Import->JGraLab.

## License

Copyright (C) 2007-2012 The JGraLab Team <ist@uni-koblenz.de>

Distributed under the General Public License (Version 3), with the following
additional grant:

    Additional permission under GNU GPL version 3 section 7

    If you modify this Program, or any covered work, by linking or combining it
    with Eclipse (or a modified version of that program or an Eclipse plugin),
    containing parts covered by the terms of the Eclipse Public License (EPL),
    the licensors of this Program grant you additional permission to convey the
    resulting work.  Corresponding Source for a non-source form of such a
    combination shall include the source code for the parts of JGraLab used as
    well as that of the covered work.


<!-- Local Variables:        -->
<!-- mode: markdown          -->
<!-- indent-tabs-mode: nil   -->
<!-- End:                    -->