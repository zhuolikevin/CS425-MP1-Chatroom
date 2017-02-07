# CS425/ECE428 Distributed Systems MP1

## Run

- The project building is powered by [Apache Ant](http://ant.apache.org/). If it is at your hand, you can simply run the following command at the root of the project:

  ```bash
  $ ant
  ```
  
  Otherwise, we also provide a shell script to build the project.
  
  ```bash
  $ ./build.sh
  ```

  Either of above will generate a directory named `bin/`. All the compiled byte codes are contained within.

- Move to `bin/`, and Run the **NodeLauncher** with specified port number.

  ```bash
  $ java NodeLauncher [port]
  ```
