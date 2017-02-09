#!/bin/bash

echo -n Username:
read username

echo -n Password:
read -s password

for i in {1..9}
do
    sshpass -p $password scp -r res/ dist/ $username@sp17-cs425-g03-0$i.cs.illinois.edu:~
    echo "[SUCCESS] Send to sp17-cs425-g03-0$i.cs.illinois.edu"
done

sshpass -p $password scp -r res/ dist/ $username@sp17-cs425-g03-10.cs.illinois.edu:~
echo "[SUCCESS] Send to sp17-cs425-g03-10.cs.illinois.edu"
