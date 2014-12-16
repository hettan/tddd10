while true; do
echo "Running generation..."

java -cp agents.jar firebrigade.prediction.training.LaunchTrainingConductor

killall java

echo "Done running simulation."

done
