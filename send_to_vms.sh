echo -n Username:
read username

echo -n Password:
read -s password

scp -r res/ dist/ $username@sp17-cs425-g03-01.cs.illinois.edu:~
