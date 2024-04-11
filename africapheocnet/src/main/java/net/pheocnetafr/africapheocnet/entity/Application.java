package net.pheocnetafr.africapheocnet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "tbl_application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "si")
    private int id;

    @Column(name = "firstName", length = 45, nullable = false)
    private String firstName;

    @Column(name = "lastName", length = 45, nullable = false)
    private String lastName;

    @Column(name = "email", length = 45, nullable = false)
    private String email;

    @Column(name = "nationality", length = 45, nullable = false)
    private String nationality;

    @Column(name = "gender", length = 45, nullable = false)
    private String gender;

    @Column(name = "profession", length = 45, nullable = false)
    private String profession;

    @Column(name = "organization", length = 45, nullable = false)
    private String organization;

    @Column(name = "position", length = 45, nullable = false)
    private String position;

    @Column(name = "areaExpertise", length = 45, nullable = false)
    private String areaExpertise;

    @Column(name = "language", length = 45, nullable = false)
    private String language;

    @Column(name = "pheocExp", length = 45, nullable = false)
    private String pheocExp;

    @Column(name = "roster", length = 45, nullable = false)
    private String roster;

    @Column(name = "statement", columnDefinition = "LONGTEXT", nullable = false)
    private String statement;

    @Column(name = "status", length = 45, nullable = false)
    private String status;

   
    public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public String getFirstName() {
		return firstName;
	}



	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}



	public String getLastName() {
		return lastName;
	}



	public void setLastName(String lastName) {
		this.lastName = lastName;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public String getNationality() {
		return nationality;
	}



	public void setNationality(String nationality) {
		this.nationality = nationality;
	}



	public String getGender() {
		return gender;
	}



	public void setGender(String gender) {
		this.gender = gender;
	}



	public String getProfession() {
		return profession;
	}



	public void setProfession(String profession) {
		this.profession = profession;
	}



	public String getOrganization() {
		return organization;
	}



	public void setOrganization(String organization) {
		this.organization = organization;
	}



	public String getPosition() {
		return position;
	}



	public void setPosition(String position) {
		this.position = position;
	}



	public String getAreaExpertise() {
		return areaExpertise;
	}



	public void setAreaExpertise(String areaExpertise) {
		this.areaExpertise = areaExpertise;
	}



	public String getLanguage() {
		return language;
	}



	public void setLanguage(String language) {
		this.language = language;
	}



	public String getPheocExp() {
		return pheocExp;
	}



	public void setPheocExp(String pheocExp) {
		this.pheocExp = pheocExp;
	}



	public String getRoster() {
		return roster;
	}



	public void setRoster(String roster) {
		this.roster = roster;
	}



	public String getStatement() {
		return statement;
	}



	public void setStatement(String statement) {
		this.statement = statement;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Application() {
    }

    
}

