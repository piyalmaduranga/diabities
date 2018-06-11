package examples.IRAObjects;

public class Proposal {
	String proposalID;
	String ExerciseHrs;
	String WaterLevel;
	String DietPlan;
	
	public Proposal(String proposalID, String exerciseHrs, String waterLevel, String dietPlan) {
		super();
		this.proposalID = proposalID;
		ExerciseHrs = exerciseHrs;
		WaterLevel = waterLevel;
		DietPlan = dietPlan;
	}
	public String getProposalID() {
		return proposalID;
	}
	public void setProposalID(String proposalID) {
		this.proposalID = proposalID;
	}
	public String getExerciseHrs() {
		return ExerciseHrs;
	}
	public void setExerciseHrs(String exerciseHrs) {
		ExerciseHrs = exerciseHrs;
	}
	public String getWaterLevel() {
		return WaterLevel;
	}
	public void setWaterLevel(String waterLevel) {
		WaterLevel = waterLevel;
	}
	public String getDietPlan() {
		return DietPlan;
	}
	public void setDietPlan(String dietPlan) {
		DietPlan = dietPlan;
	}
	
}
