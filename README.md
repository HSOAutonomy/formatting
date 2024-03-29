# formatting

[![Build Status](https://travis-ci.org/HSOAutonomy/formatting.png)](https://travis-ci.org/HSOAutonomy/formatting) [![Build status](https://ci.appveyor.com/api/projects/status/xx70nsj0tl0cm92g?svg=true)](https://ci.appveyor.com/project/HSOAutonomy/formatting)

This repo contains instructions and a command line tool to help achieve consistent formatting for hso-autonomy projects (in an IDE-independent way). We use [clang-format](https://clang.llvm.org/docs/ClangFormat.html) for Java and C++, and [yapf](https://github.com/google/yapf) for Python.

## Setup

- Clone this repository.
- Run `java -jar formattingHelper.jar --init <project dir(s)>` (where `<project dir(s)>` is one or multiple project directories to which to add the config files). This should be done again when there are changes to the config / this repo.

In case the repository has not been set up for formatting yet:

- Add the following to `.gitignore`:

  ```
  .idea/**/watcherTasks.xml
  .clang-format
  *.TMP
  *.java-*
  ```

- Run `java -jar formattingHelper.jar <project dir(s)>` (same as before, but without `--init`) to recursively reformat all source files in the repo. With `--verbose`, the path of every formatted file is printed.

## IDE Setup

These instructions assume that the earlier, general setup has already been done.

### IntelliJ IDEA

(these instructions should work for all IDEA-based IDEs, including CLion / PyCharm)

- Install the [File Watchers plugin](https://plugins.jetbrains.com/plugin/7177-file-watchers): `File|Settings|Plugins|Browse Repositories...`, then search for "File Watchers" and install it.
- After restarting IDEA, making a change to a file and pressing <kbd>Ctrl</kbd>+<kbd>S</kbd> should trigger a reformat:

  ![](images/idea.gif)

## Eclipse

- Deactivate the built-in format-on-save if enabled (`Window|Preferences|Java|Editor|Save Actions`, uncheck "Format source code").
- Install a modified version of the [CppStyle plugin](https://github.com/wangzw/CppStyle/) by copying [`org.wangzw.cppstyle-1.4.0.6.jar`](org.wangzw.cppstyle-1.4.0.6.jar) to the `dropins` directory of your Eclipse installation.
- You may need to start Eclipse from the command line using `eclipse -clean` (to clear the plugin cache).
- Go to `Window|Preferences|CppStyle`. Here, specify the `Clang-format path` (one of the files in [`/binaries`](/binaries), depending on your OS) and enable `Run clang-format on file save`.
- Making a change to a file and pressing <kbd>Ctrl</kbd>+<kbd>S</kbd> should now trigger a reformat:

  ![](images/eclipse.gif)

### Visual Studio Code

- Install the [Clang-Format extension](https://marketplace.visualstudio.com/items?itemName=xaver.clang-format) (`View|Extensions`, search for "clang-format")
- enable format-on-save by adding this to `settings.json` (`File|Preferences|Settings`):

  ```json
  "editor.formatOnSave": true
  ```

- Also set the `"clang-format.executable"` path (on Windows, escape `\` with `\\`!), or make sure `clang-format` is in your `PATH`.
- After restarting VSCode, making a change to a file and pressing <kbd>Ctrl</kbd>+<kbd>S</kbd> should trigger a reformat:

  ![](images/vscode.gif)

## Known issues

- On Windows, clang-format may create `TMP` files next to source files sometimes (such as `WorldModel.java~RF3494d0.TMP`). This is why `*.TMP` should be added to `.gitignore` as instructed earlier. More info [here](https://bugs.llvm.org//show_bug.cgi?id=26286).

Overall it works very well, however.
