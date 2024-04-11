package net.pheocnetafr.africapheocnet.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import net.pheocnetafr.africapheocnet.forum.ForumTopic;

@Entity
@Table(name = "tbl_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    private String firstName;
    private String lastName;
    private String role; 
    private boolean enabled;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiration")
    private LocalDateTime resetTokenExpiration;
    

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public LocalDateTime getResetTokenExpiration() {
		return resetTokenExpiration;
	}

	public void setResetTokenExpiration(LocalDateTime resetTokenExpiration) {
		this.resetTokenExpiration = resetTokenExpiration;
	}

	 public User(String firstName, String lastName, String email) {
	        this.firstName = firstName;
	        this.lastName = lastName;
	        this.email = email;
	    }
	 
	 public User() {
	       
	    }
	 
	 @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<ForumTopic> forumTopics = new ArrayList<>();

	    // Constructors, getters, and setters
	    
	    public List<ForumTopic> getForumTopics() {
	        return forumTopics;
	    }

	    public void setForumTopics(List<ForumTopic> forumTopics) {
	        this.forumTopics = forumTopics;
	    }
	    private String username;

	    public String getUsername() {
	        return this.username;
	    }
	    
	    
	    public boolean isMfaEnabled() {
			return mfaEnabled;
		}

		public void setMfaEnabled(boolean mfaEnabled) {
			this.mfaEnabled = mfaEnabled;
		}

		public String getVerificationCode() {
			return verificationCode;
		}

		public void setVerificationCode(String verificationCode) {
			this.verificationCode = verificationCode;
		}

		@Column(name = "mfa_enabled")
	    private boolean mfaEnabled;

	    @Column(name = "verification_code")
	    private String verificationCode;
	    
	    
	    @Column(name = "verification_code_expiration")
	    private LocalDateTime verificationCodeExpiration;


		public LocalDateTime getVerificationCodeExpiration() {
			return verificationCodeExpiration;
		}

		public void setVerificationCodeExpiration(LocalDateTime verificationCodeExpiration) {
			this.verificationCodeExpiration = verificationCodeExpiration;
		}
	    
	    
}

