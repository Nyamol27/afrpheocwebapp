package net.pheocnetafr.africapheocnet.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Entity
@Table(name = "tbl_member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private int id;

    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

    @Column(name = "email", length = 45, nullable = false)
    private String email;

    @Column(name = "gender", length = 45, nullable = false)
    private String gender;

    @Column(name = "nationality", length = 45, nullable = false)
    private String nationality;

   
    
    @Column(name = "profession", length = 45)
    private String profession;
    

    @Column(name = "organization", length = 45, nullable = false)
    private String organization;

    @Column(name = "position", length = 45, nullable = false)
    private String position;

    @Column(name = "expertise", length = 45)
    private String expertise;

    @Column(name = "language", length = 45, nullable = false)
    private String language;



    @Column(name = "enrolement", nullable = false)
    private LocalDate enrollment;

    @Column(name = "bio", columnDefinition = "LONGTEXT", nullable = false)
    private String bio;

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



	public String getGender() {
		return gender;
	}



	public void setGender(String gender) {
		this.gender = gender;
	}



	public String getNationality() {
		return nationality;
	}



	public void setNationality(String nationality) {
		this.nationality = nationality;
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



	public String getExpertise() {
		return expertise;
	}



	public void setExpertise(String expertise) {
		this.expertise = expertise;
	}



	public String getLanguage() {
		return language;
	}



	public void setLanguage(String language) {
		this.language = language;
	}

	

	public LocalDate getEnrollment() {
		return enrollment;
	}



	public void setEnrollment(LocalDate enrollment) {
		this.enrollment = enrollment;
	}



	public String getBio() {
		return bio;
	}



	public void setBio(String bio) {
		this.bio = bio;
	}



	public Member() {
    }

	
	 @Lob
	    @Column(name = "photo")
	    private byte[] photo; 
	    @Transient
	    private String base64Photo;

		public byte[] getPhoto() {
			return photo;
		}



		public void setPhoto(byte[] photo) {
			this.photo = photo;
		}



		public String getBase64Photo() {
			return base64Photo;
		}



		public void setBase64Photo(String base64Photo) {
			this.base64Photo = base64Photo;
		}
   
}
