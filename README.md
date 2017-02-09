# CS425/ECE428 Distributed Systems MP1

## Run the program

- The project building is powered by [Apache Ant](http://ant.apache.org/). If it is at your hand, you can simply run the following command at the root of the project:

    ```bash
    $ ant
    ```
  
 This will generate a directory named `dist/`. The complied and packed `.jar` package is inside.

- Move to `dist/`, and Run the **cs425-mp1.jar** with specified port number and address file if needed.

    ```bash
    $ java -jar cs425-mp1.jar [port] [address_file]
    ```
  
    for example:
  
    ```bash
    $ java -jar cs425-mp1.jar 10001 address_test.txt
    ```

## Send program to virtual machines

- We provided a shell script for sending `.jar` file and `res/` to all vms. However **sshpass** is needed for the script.

    ```bash
    $ brew install https://raw.githubusercontent.com/kadwanev/bigboybrew/master/Library/Formula/sshpass.rb
    ```
    
- If we want to send `dist/` and `res/` to vms, and run the codes on them. Just use the **send_to_vms.sh** script.

    ```bash
    $ ./send_to_vms.sh
    ```

- If permission denied, grant execution access for the file

    ```bash
    $ chmod +x send_to_vms.sh
    ```