#!/bin/bash

project_dir=${1:-./}

cd ${project_dir}

read -p "Input project name (default=testpj): " project_name
project_name=${project_name:-testpj}
read -p "Input version (default=0.1): " version
version=${version:-0.1} 

if [ -e ./${project_name} ]; then
  echo "project ${project_name} is already exists."
  exit 1

else
  mkdir -p ${project_name}/{project,target}
  cd ./${project_name}
  mkdir -p src/{main,test}/{resources,scala,java}
  echo "name :=\"${project_name}\"\nversion :=\"${version}\"">build.sbt
  echo 'object Hi { def main(args: Array[String]) = println("Hi!") }' > ./src/main/scala/hw.scala
fi

