package net.pheocnetafr.africapheocnet.util;

import java.util.Arrays;
import java.util.List;

public class AutoFilledData {
	 public static List<String> getExpertiseList() {
	        return Arrays.asList(
	                "PHEOC Legal instruments, plans, and procedures", "PHEOC workforce development", "Technology infrastructure and Information management"
	        );
	    }
	 
	 
	 public static List<String> getProfessionList() {
	        return Arrays.asList(
	                "Administrator","AVOHC-SURGE","Community health worker","First responder (such as, EMT, paramedic)","IT, information systems, and data management professional",
	                "Medical doctor","Public health professional","Researcher","Student","Teacher / Educator"
	        );
	    }
	 public static List<String> getOrganizationList() {
	        return Arrays.asList(
	               "Africa CDC","Government","MoH (Ministry of Health)","NPHI (National Public Health Institute)","Non-governmental organization",
	               "PHEOC (Public Health Emergency Operations Center)","School / University","UN Agency","WHO (World Health Organization)"
	        );
	    }
	

	 public static List<String> getGenderList() {
	        return Arrays.asList(
	                "Female", "Male"
	        );
	    }
	 
	 public static List<String> getLanguageList() {
	        return Arrays.asList(
	                "English", "French", "Portuguese"
	        );
	    }
	  public static List<String> getAllAfricanCountries() {
	        return Arrays.asList(
	                "Algeria", "Angola", "Benin", "Botswana", "Burkina Faso", "Burundi", "Cabo Verde",
	                "Cameroon", "Central African Republic", "Chad", "Comoros", "Congo (Congo-Kinshasa)", "Congo (Congo-Brazzaville)", "Djibouti",
	                "Egypt", "Equatorial Guinea", "Eritrea", "Eswatini", "Ethiopia", "Gabon", "Gambia",
	                "Ghana", "Guinea", "Guinea-Bissau", "Ivory Coast", "Kenya", "Lesotho", "Liberia",
	                "Libya", "Madagascar", "Malawi", "Mali", "Mauritania", "Mauritius", "Morocco",
	                "Mozambique", "Namibia", "Niger", "Nigeria", "Rwanda", "Sao Tome and Principe",
	                "Senegal", "Seychelles", "Sierra Leone", "Somalia", "South Africa", "South Sudan",
	                "Sudan", "Tanzania", "Togo", "Tunisia", "Uganda", "Zambia", "Zimbabwe"
	        );
	    }
	  
	  public static List<String> getDeploymentStatus() {
	        return Arrays.asList(
	                "Completed", "Ongoing"
	        );
	    }
	  public static List<String> getDeploymentEntitiesList() {
	        return Arrays.asList(
	        		 "Africa CDC", "BMGF","Resolve to save lives","UK HSA", "US CDC", "WHO"
	        );
	    }
	  public static List<String> getDeploymentTypeList() {
	        return Arrays.asList(
	        		 "Capacity building", "PHEOC Establishment / Strenghtening ", "Simulation exercise"
	        );
	    }
	  
	  public static List<String> getUserStatusList() {
	        return Arrays.asList(
	        		 "Enabled", "Disabled"
	        );
	    }
	  
	  public static List<String> getTwgGroupList() {
	        return Arrays.asList(
	        		 "PHEOC Legal instruments, plans, and procedures","PHEOC workforce development",
	        		 "PHEOC Workforce development","PHEOC Centers of Excellence"
	        );
	    }
	  
	  public static List<String> getTwgGroupStatusList() {
	        return Arrays.asList(
	        		 "Approved", "Pending"
	        );
	    }
	  
	  public static List<String> getTrainerStatusList() {
	        return Arrays.asList(
	        		 "Available", "Not Available"
	        );
	    }
	  
	  public static List<String> getTrainerNoticeOptions() {
	        return Arrays.asList(
	        		 "2 Weeks", "4 Weeks", "2 Months", "3 Months", "Negotiable"
	        );
	    }
	  
	  public static List<String> getTaskPriorityList() {
	        return Arrays.asList(
	        		 "High", "Medium", "Low"
	        );
	    }
	  
	  public static List<String> getTaskStatusList() {
	        return Arrays.asList(
	        		 "Ongoing", "Overdue", "Completed"
	        );
	    }
	  public static List<String> getProjectStatusList() {
	        return Arrays.asList(
	        		 "Ongoing", "Delayed", "Completed"
	        );
	    }
	  
	  public static List<String> getModuleList() {
	        return Arrays.asList(
	        		"ePHEM for IT Professional", "Incident Management System", 
	        		"Introduction to SIMEX", "Introduction to ePHEM", "PHEOC Advanced Training", "PHEOC Basic Training", 
	        		"PHEOC Intermediate Level Training", "WHO ERF"
	        );
	    }
	  
	  public static List<String> getTraineeRoleList() {
	        return Arrays.asList(
	        		 "Participant", "Facilitator"
	        );
	    }
	  
	  public static List<String> getTrainerDeploymentStatusList() {
	        return Arrays.asList(
	        		 "Yes", "No"
	        );
	    }
}
