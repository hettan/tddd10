package ambulance;

public enum AmbulanceTeamTasks {
	//AT has no task
	NO_TASK,
	//AT has found a human
	FOUND_HUMAN,
	//AT is loading a civilian
	LOADING,
	//AT is moving a civilian to refuge
	MOVE_TO_REFUGE,
	//AT is rescuing a human
	RESCUING,
	//AT is searching for buried human
	SEARCHING,
	//AT is moving to a human which need to be rescued
	MOVING_TO_HUMAN,
	//AT stuck in blockade
	STUCK,
	//AT buried in a building
	BURIED,
}
