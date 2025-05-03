#!/bin/bash

# 1. Update and add the AdoptOpenJDK PPA for Java 21
sudo apt update
sudo apt install -y wget gnupg2
wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptopenjdk.gpg
echo "deb [signed-by=/usr/share/keyrings/adoptopenjdk.gpg] https://adoptopenjdk.jfrog.io/adoptopenjdk/deb jammy main" \
  | sudo tee /etc/apt/sources.list.d/adoptopenjdk.list

# 2. Install OpenJDK 21
sudo apt update
sudo apt install -y openjdk-21-jdk

# 3. Verify and set defaults
java -version
javac -version
sudo update-alternatives --auto java
sudo update-alternatives --auto javac

# 4. Install Maven
sudo apt install -y maven

# 5. Verify Maven
mvn -v

# 6. (Optional) Persist JAVA_HOME in your shell
echo 'export JAVA_HOME=/usr/lib/jvm/adoptopenjdk-21-hotspot-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH'          >> ~/.bashrc
source ~/.bashrc
