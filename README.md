# COMP20081_Group_Assessment

If running the app outside the app container the terminal emulator will only work properly if your pc has been authorised. First generate a key pair by running the following in the terminal

```
ssh-keygen
```

Then copy the public key from ```~/.ssh/id_rsa.pub``` and append it to the list of keys in ```<APP_ROOT>/Docker/Storage/authorized_keys```

---

In order to run the app the containers need to be running. It will not even run without them.

If it's the first time launching the containers the docker-compose needs to be built. This can be done by openning a terminal in ```<APP_ROOT>/Docker``` and running the following:

```
docker-compose build
```

From this point on, as long as the docker-compose file is not changed the containers can be started by running the following in ```<APP_ROOT>/Docker```:

```
docker-compose up -d
```

To bring the containers down run the following in ```<APP_ROOT>/Docker```:

```
docker-compose down
```

---

The first time the app is ran on a machine or in a new container it may be necessary to set known hosts. To do this run the set_known_hosts.sh script in the projects root directory:

```
bash ./set_known_hosts.sh
```
