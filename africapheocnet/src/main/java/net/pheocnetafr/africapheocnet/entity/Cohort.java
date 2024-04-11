package net.pheocnetafr.africapheocnet.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_cohort")
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cohort_details")
    private String cohortDetails;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCohortDetails() {
		return cohortDetails;
	}

	public void setCohortDetails(String cohortDetails) {
		this.cohortDetails = cohortDetails;
	}

    
}

