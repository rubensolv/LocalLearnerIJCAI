#!/bin/bash

#PBS -N BW32Map
#PBS -l nodes=1:ppn=3,mem=4gb,walltime=72:00:00
#PBS -q qtime

cd /storage1/dados/es91661/plots/NewRank10FinalTable/BW32Map

source /etc/profile.d/modules.sh 

#module load java/jre1.8.0_162
module load java/jdk11.0.7

java -jar MicroRTS.jar "7"
