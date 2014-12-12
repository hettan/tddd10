#! /bin/bash

. functions.sh

processArgs $*

makeClasspath $BASEDIR/lib $BASEDIR/jars $BASEDIR/../agents/bin/sample

xterm -T agents -e bash -c "java -Xmx512m -cp $CP rescuecore2.LaunchComponents sample.SampleFireBrigade*n sample.SampleAmbulanceTeam*n sample.SamplePoliceForce*n sample.SampleCentre*n firebrigade.prediction.training.TrainingFitnessObserver -c $DIR/config/sample-agents.cfg 2>&1" &

PIDS="$PIDS $!"
echo "Agents started."

waitFor agents_status.txt "done"

kill $PIDS
rm agents_status.txt
