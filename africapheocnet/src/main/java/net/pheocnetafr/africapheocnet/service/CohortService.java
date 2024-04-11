package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.pheocnetafr.africapheocnet.entity.Cohort;
import net.pheocnetafr.africapheocnet.repository.CohortRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CohortService {

    private final CohortRepository cohortRepository;

    @Autowired
    public CohortService(CohortRepository cohortRepository) {
        this.cohortRepository = cohortRepository;
    }

    public List<Cohort> getAllCohorts() {
        return cohortRepository.findAll();
    }

    public Cohort getCohortById(Long id) {
        Optional<Cohort> optionalCohort = cohortRepository.findById(id);
        return optionalCohort.orElse(null);
    }

    public Cohort createCohort(Cohort cohort) {
        return cohortRepository.save(cohort);
    }

    public Cohort updateCohort(Long id, Cohort updatedCohort) {
        Optional<Cohort> optionalCohort = cohortRepository.findById(id);
        if (optionalCohort.isPresent()) {
            Cohort existingCohort = optionalCohort.get();
            existingCohort.setCohortDetails(updatedCohort.getCohortDetails());
            return cohortRepository.save(existingCohort);
        }
        return null; // Cohort not found
    }

    public void deleteCohort(Long id) {
        cohortRepository.deleteById(id);
    }
}

