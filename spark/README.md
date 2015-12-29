# spark install memo

## 準備

### sudoers
```Bash:sudoer
visudo -f /etc/sudoers.d/hadoop

%hadoop ALL=(ALL) NOPASSWD: ALL
Defaults:%hadoop !requiretty
Defaults:%hadoop env_keep += SSH_AUTH_SOCK
```

### pssh インストール
```Bash:install_pssh
wget https://parallel-ssh.googlecode.com/files/pssh-2.3.1.tar.gz
tar xzf pssh-2.3.1.tar.gz
cd pssh-2.3.1
sudo python setup.py build
sudo python setup.py install
```

### hostを編集
```Bash:Host
node2
node3
```

### psshの確認
```Bash:check_pssh
pssh -h /home/hadoop/host -i uptime
sudo pssh -h /home/hadoop/host -i whoami
sudo pscp -h /home/hadoop/host /etc/hosts /etc
```

### JDK インストール
```Bash:install_JDK
mkdir work
cd work
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.rpm" -O jdk-8u45-linux-x64.rpm
sudo rpm -ivh /home/hadoop/work/jdk-8u45-linux-x64.rpm
sudo alternatives --install /usr/bin/java java /usr/java/jdk1.8.0_45/bin/java 180045
pssh -h /home/hadoop/host -i mkdir work
pscp -h /home/hadoop/host /home/hadoop/work/jdk-8u45-linux-x64.rpm /home/hadoop/work/
sudo pssh -h /home/hadoop/host -i rpm -ivh /home/hadoop/work/jdk-8u45-linux-x64.rpm
sudo pssh -h /home/hadoop/host -i alternatives --install /usr/bin/java java /usr/java/jdk1.8.0_45/bin/java 180045
```

## hadoop

### download
```Bash:download_hadoop
wget -4 -P /home/hadoop/work http://ftp.meisei-u.ac.jp/mirror/apache/dist/hadoop/common/hadoop-2.7.1/hadoop-2.7.1.tar.gz
tar zxvf /home/hadoop/work/hadoop-2.7.1.tar.gz -C /home/hadoop/work
sudo mv /home/hadoop/work/hadoop-2.7.1 /usr/local
sudo pscp -h /home/hadoop/host -r /usr/local/hadoop-2.7.1 /usr/local/
```

### パスを通す
```Bash:.bashrc
# .bashrc

# Source global definitions
if [ -f /etc/bashrc ]; then
        . /etc/bashrc
fi

# User specific aliases and functions
export JAVA_HOME=/usr/java/jdk1.8.0_45
export HADOOP_HOME=/usr/local/hadoop-2.7.1
export HADOOP_INSTALL=$HADOOP_HOME
export HADOOP_MAPRED_HOME=$HADOOP_HOME
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_HDFS_HOME=$HADOOP_HOME
export YARN_HOME=$HADOOP_HOME
PATH=$PATH:$HOME/bin:$JAVA_HOME/bin:$HADOOP_HOME/sbin:$HADOOP_HOME/bin
export PATH
```

```Bash:update_bashrc
source ~/.bashrc
pscp -h /home/hadoop/host /home/hadoop/.bashrc /home/hadoop/
pssh -h /home/hadoop/host -i source /home/hadoop/.bashrc
```

### 権限設定
```Bash:change_own
sudo chown hadoop:hadoop ${HADOOP_HOME} -R
sudo chgrp hadoop ${HADOOP_HOME} -R
sudo mkdir /data/namenode
sudo chown hadoop:hadoop /data/namenode
sudo chgrp hadoop /data/namenode
sudo pssh -h /home/hadoop/host -i chown hadoop:hadoop ${HADOOP_HOME} -R
sudo pssh -h /home/hadoop/host -i chgrp hadoop ${HADOOP_HOME} -R
sudo pssh -h /home/hadoop/host -i mkdir /data/datanode
sudo pssh -h /home/hadoop/host -i chown hadoop:hadoop /data/datanode
sudo pssh -h /home/hadoop/host -i chgrp hadoop /data/datanode
```

### 設定ファイルを編集

#### core-site
```
cd ${HADOOP_HOME}
vi ./etc/hadoop/core-site.xml
```
```xml:core-site.xml
<property>
    <name>fs.defaultFS</name>
    <value>hdfs://idapgp111:9000/</value>
</property>
```

#### yarn-site
```
cd ${HADOOP_HOME}
vi ./etc/hadoop/yarn-site.xml
```

```xml:yarn-site.xml
<configuration>
<!-- Site specific YARN configuration properties -->
<property>
  <name>yarn.resourcemanager.hostname</name>
  <value>idapgp111</value>
</property>
<property>
  <name>yarn.nodemanager.aux-services</name>
  <value>mapreduce_shuffle</value>
</property>
<property>
  <name>yarn.nodemanager.address</name>
  <value>${yarn.nodemanager.hostname}:8041</value>
</property>
<property>
  <name>yarn.nodemanager.vmem-check-enabled</name>
  <value>false</value>
</property>
</configuration>
```

#### mapred-site
```
cd ${HADOOP_HOME}
cp ./etc/hadoop/mapred-site.xml.template ./etc/hadoop/mapred-site.xml
vi ./etc/hadoop/mapred-site.xml
```

```xml:mapred-site.xml
<configuration>
<property>
  <name>mapreduce.framework.name</name>
  <value>yarn</value> <!-- and not local (!) -->
</property>
</configuration>
```

#### hdfs-site
```
cd ${HADOOP_HOME}
vi ./etc/hadoop/hdfs-site.xml
```

```xml:hdfs-site.xml
<configuration>
<property>
  <name>dfs.replication</name>
  <value>3</value>
</property>
<property>
  <name>dfs.permissions</name>
  <value>false</value>
</property>
<property>
   <name>dfs.datanode.data.dir</name>
   <value>/data/datanode</value>
</property>
<property>
  <name>dfs.namenode.data.dir</name>
  <value>/data/namenode</value>
</property>
</configuration>
```

#### slaves
```
cd ${HADOOP_HOME}
vi ./etc/hadoop/slaves
```

```Bash:slaves
node2
node3
```

### 各ノードに配布
```Bash:update_hadoop
sudo pscp -h /home/hadoop/host -r /usr/local/hadoop-2.7.1 /usr/local/
sudo pssh -h /home/hadoop/host -i chown hadoop ${HADOOP_HOME} -R
sudo pssh -h /home/hadoop/host -i chgrp hadoop ${HADOOP_HOME} -R
```

### 起動
```Bash:run_hadoop
start-all.sh
```

### サンプル実行
```Bash:execute_hadoop
hadoop jar ${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.1.jar wordcount /input/test.txt /output
```

## spark

### download
```Bash:download_spark
cd ${HOME}/work
wget http://ftp.jaist.ac.jp/pub/apache/spark/spark-1.5.1/spark-1.5.1-bin-hadoop2.6.tgz
tar zxvf spark-1.5.1-bin-hadoop2.6.tgz
sudo cp -r spark-1.5.1-bin-hadoop2.6 /usr/local
sudo chown hadoop /usr/local/spark-1.5.1-bin-hadoop2.6 -R
sudo chgrp hadoop /usr/local/spark-1.5.1-bin-hadoop2.6 -R
```

### 環境変数
```Bash:~/.bashrc
export SPARK_HOME=/usr/local/spark-1.5.1-bin-hadoop2.6
PATH=$PATH:$HOME/bin:$JAVA_HOME/bin:$HADOOP_HOME/sbin:$HADOOP_HOME/bin:$HADOOP_INSTALL/bin:$SPARK_HOME/bin:$SPARK_HOME/sbin
```

### INFO -> WARN
```Bash:INFO2WARN
cd $SPARK_HOME/conf
cp log4j.properties.template log4j.properties
```

### 実行

```Bash:execute_spark
spark-submit --master yarn-client --class org.apache.spark.examples.SparkPi ${SPARK_HOME}/lib/spark-examples-1.5.1-hadoop2.6.0.jar 10
```

## Scala

### install

```Bash
cd {HOME}/work
wget http://www.scala-lang.org/files/archive/scala-2.11.5.tgz
tar xvzf scala-2.11.5.tgz 
sudo mv scala-2.11.5 /usr/local/scala
```

```Bash:.bashrc
export SCALA_HOME=/usr/local/scala
export PATH=$PATH:$SCALA_HOME/bin
```



