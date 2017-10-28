# How to compile
The following steps describes on how to properly build [La2Eden](http://la2eden.com).

**Project dependencies:**
  - [Java 8 (JDK)](http://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html)
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
  - A svn client. [SlikSVN](https://sliksvn.com/download/) is recommended though.

**Steps:**
1. Download and install [JDK 8](http://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html)
2. Set `JAVA_HOME` environment variable
  - Right-click "My Computer" on your Desktop.
  - Click "Properties".
  - Click "Advanced system settings".
  - Click the "Environment Variables..." button.
  - Click the "New..." button.
  - Type at Variable name: `JAVA_HOME`
  - Type at Variable value: `<path_to_your_jdk8_folder>` *(e.g. `C:\Program Files\Java\jdk1.8.0_91`)*
3. Download and extract [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
4. Checkout the source files
  - From the `VCS` menu, click the "Check out from Version Control" dropdown and select `Git`
  - Type at the `Git Repository URL` field: `https://gitlab.com/queued/la2eden`
  - Click "Clone".
5. Compile the project
  - From the "Ant Build" side-window, hit the green-forward arrow *">"* button.
  - Wait for the "Ant build completed successfully" console message.
  - Go to `<project_path>\build\` folder and get your `La2Eden.zip` file.

**You just made it! :-)**
