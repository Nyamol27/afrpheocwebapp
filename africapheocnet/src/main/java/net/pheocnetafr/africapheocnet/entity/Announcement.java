package net.pheocnetafr.africapheocnet.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tbl_announcement")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(length = 100000)
    private String content;

    @Lob
    private byte[] attachment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publication_date") 
    private Date publicationDate;

    private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	 @Column(name = "attachment_type") 
	    private String attachmentType;

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	private boolean attachmentAvailable;

	public boolean isAttachmentAvailable() {
		return attachmentAvailable;
	}

	public void setAttachmentAvailable(boolean attachmentAvailable) {
		this.attachmentAvailable = attachmentAvailable;
	}
	
	

	   
    
}
