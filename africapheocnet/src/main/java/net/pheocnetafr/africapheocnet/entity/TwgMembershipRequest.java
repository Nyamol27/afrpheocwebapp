package net.pheocnetafr.africapheocnet.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_groups")
public class TwgMembershipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String twg; // Working Group
    private String status; // "Pending," "Approved," or "Rejected"
    
    @Column(name = "email")
    private String email;
    
    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    private Member member; 
    
    @ManyToOne
    @JoinColumn(name = "working_group_id", referencedColumnName = "id")
    private WorkingGroup workingGroup;

    // Constructors
    public TwgMembershipRequest() {
    }

    public TwgMembershipRequest(String name, String twg, String status, Member member, WorkingGroup workingGroup) {
        this.name = name;
        this.twg = twg;
        this.status = status;
        this.member = member;
        this.workingGroup = workingGroup;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTwg() {
        return twg;
    }

    public void setTwg(String twg) {
        this.twg = twg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
    
    public String getEmail() {
        if (member != null) {
            return member.getEmail();
        } else {
            return null;
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public WorkingGroup getWorkingGroup() {
        return workingGroup;
    }

    public void setWorkingGroup(WorkingGroup workingGroup) {
        this.workingGroup = workingGroup;
    }
    
    private String position;
    private String organization;
    private String nationality;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}
    
    private String firstName;
    private String lasttName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLasttName() {
		return lasttName;
	}

	public void setLasttName(String lasttName) {
		this.lasttName = lasttName;
	}
    
    
}
