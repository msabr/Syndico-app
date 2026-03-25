package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.AssemblyMeeting;
import com.syndico.syndicoapp.models.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssemblyMeetingRepository extends JpaRepository<AssemblyMeeting, Long> {
    List<AssemblyMeeting> findByStatus(MeetingStatus status);
}
