package net.pheocnetafr.africapheocnet.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import net.pheocnetafr.africapheocnet.entity.TwgMembershipRequest;

public interface TwgMembershipRequestRepository extends JpaRepository<TwgMembershipRequest, Long> {
    List<TwgMembershipRequest> findByStatus(String status);
    List<TwgMembershipRequest> findByTwg(String twg);
    List<TwgMembershipRequest> findByTwgAndStatus(String twg, String status);
    List<TwgMembershipRequest> findByEmailAndStatus(String email, String status); 
    List<TwgMembershipRequest> findByEmailAndTwgAndStatus(String email, String twg, String status); 
    List<TwgMembershipRequest> findByEmail(String email);
    List<TwgMembershipRequest> findByEmailAndWorkingGroup_IdAndStatus(String email, Long workingGroupId, String status);
    long countByTwgAndStatus(String twgName, String status);
    Page<TwgMembershipRequest> findByTwg(String twgName, PageRequest pageRequest);
}
