package net.pheocnetafr.africapheocnet.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.pheocnetafr.africapheocnet.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

   
    Member findByEmail(String email);
    

    
    List<Member> findByNationality(String nationality);
    
    List<Member> findByExpertise(String expertise);
    
 // Method to count all members
    @Query("SELECT COUNT(m) FROM Member m")
    int countAllMembers();

    // Method to retrieve members by page
    @Query("SELECT m FROM Member m")
    List<Member> findMembersByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    
    List<Member> findByLastNameContaining(String lastName);
    
    List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);


    // Method to count members by nationality
    @Query("SELECT m.nationality, COUNT(m) FROM Member m GROUP BY m.nationality")
    List<Object[]> countMembersByNationality();

    // Method to count members by profession
    @Query("SELECT m.profession, COUNT(m) FROM Member m GROUP BY m.profession")
    List<Object[]> countMembersByProfession();

    // Method to count members by organization
    @Query("SELECT m.organization, COUNT(m) FROM Member m GROUP BY m.organization")
    List<Object[]> countMembersByOrganization();

    // Method to fetch enrollment data for the past three months
    @Query("SELECT m.enrollment, COUNT(m) FROM Member m WHERE m.enrollment >= ?1 AND m.enrollment <= ?2 GROUP BY m.enrollment")
    List<Object[]> getEnrollmentData(LocalDate startDate, LocalDate endDate);


}



