package com.studify.service;

import com.studify.dto.attendance.AttendanceDTOs;
import com.studify.entity.AttendanceRecord;
import com.studify.entity.RecurringClass;
import com.studify.entity.Subject;
import com.studify.entity.User;
import com.studify.exception.BusinessException;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.AttendanceRecordRepository;
import com.studify.repository.RecurringClassRepository;
import com.studify.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final RecurringClassRepository recurringClassRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public AttendanceDTOs.Response createOrUpdate(AttendanceDTOs.CreateRequest request, User user) {
        RecurringClass recurringClass = recurringClassRepository.findByIdAndUserIdAndDeletedAtIsNull(
                request.recurringClassId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aula recorrente não encontrada: " + request.recurringClassId()));

        // Validação 1: A data deve corresponder ao dia da semana da aula recorrente
        if (!request.date().getDayOfWeek().equals(recurringClass.getDayOfWeek())) {
            throw new BusinessException("A data informada não corresponde ao dia da semana configurado para esta aula (" + recurringClass.getDayOfWeek() + ")");
        }

        // Validação 2: A data deve estar dentro do período de vigência da aula recorrente
        if (request.date().isBefore(recurringClass.getStartDate()) || request.date().isAfter(recurringClass.getEndDate())) {
            throw new BusinessException("A data informada está fora do período de vigência da aula recorrente (" + recurringClass.getStartDate() + " até " + recurringClass.getEndDate() + ")");
        }

        // Tenta encontrar um registro ativo existente para realizar o upsert
        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository
                .findByUserIdAndRecurringClassIdAndDateAndDeletedAtIsNull(user.getId(), request.recurringClassId(), request.date());

        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            record.setPresent(request.present());
        } else {
            record = AttendanceRecord.builder()
                    .recurringClass(recurringClass)
                    .user(user)
                    .date(request.date())
                    .present(request.present())
                    .build();
        }

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDTOs.Response> findBySubjectId(Long subjectId, User user, Pageable pageable) {
        subjectRepository.findByIdAndUserId(subjectId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada: " + subjectId));

        return attendanceRecordRepository.findBySubjectIdAndUserId(subjectId, user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void delete(Long id, User user) {
        AttendanceRecord record = attendanceRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro de presença não encontrado: " + id));

        record.setDeletedAt(LocalDateTime.now());
        attendanceRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public AttendanceDTOs.GlobalAttendanceSummary getSummary(User user) {
        List<Subject> subjects = subjectRepository.findByUserId(user.getId());
        List<RecurringClass> recurringClasses = recurringClassRepository.findByUserIdAndDeletedAtIsNull(user.getId());
        List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findAllActiveByUserId(user.getId());

        // Mapear aulas recorrentes por ID de matéria
        Map<Long, List<RecurringClass>> classesBySubject = recurringClasses.stream()
                .filter(rc -> rc.getSubject() != null)
                .collect(Collectors.groupingBy(rc -> rc.getSubject().getId()));

        // Mapear registros de presença por ID de matéria (através da aula recorrente)
        Map<Long, List<AttendanceRecord>> attendanceBySubject = attendanceRecords.stream()
                .filter(ar -> ar.getRecurringClass() != null && ar.getRecurringClass().getSubject() != null)
                .collect(Collectors.groupingBy(ar -> ar.getRecurringClass().getSubject().getId()));

        List<AttendanceDTOs.SubjectAttendanceSummary> summaries = new ArrayList<>();
        int warningCount = 0;
        int dangerCount = 0;
        int exceededCount = 0;

        for (Subject subject : subjects) {
            List<RecurringClass> subjectClasses = classesBySubject.getOrDefault(subject.getId(), List.of());
            List<AttendanceRecord> subjectAttendances = attendanceBySubject.getOrDefault(subject.getId(), List.of());

            // 1. Calcular total de aulas teóricas no período de vigência das aulas recorrentes
            int totalClasses = 0;
            for (RecurringClass rc : subjectClasses) {
                if (rc.isActive()) {
                    totalClasses += calculateOccurrences(rc.getStartDate(), rc.getEndDate(), rc.getDayOfWeek());
                }
            }

            // 2. Calcular presenças e faltas atuais marcadas
            int currentAbsences = 0;
            int currentPresences = 0;
            for (AttendanceRecord ar : subjectAttendances) {
                if (ar.isPresent()) {
                    currentPresences++;
                } else {
                    currentAbsences++;
                }
            }

            int totalMarked = currentPresences + currentAbsences;

            // 3. Percentual de faltas em relação às aulas totais
            double absencePercentage = 0.0;
            if (totalClasses > 0) {
                absencePercentage = ((double) currentAbsences / totalClasses) * 100.0;
            }

            // 4. Determinação de Status
            String status = "SAFE";
            int limitPct = subject.getMaxAbsencePercentage();
            double warningLimitPct = limitPct * 0.50; // 50% do limite de faltas (ex: 12.5%)
            double dangerLimitPct = limitPct * 0.80;  // 80% do limite de faltas (ex: 20%)

            if (totalClasses > 0) {
                if (absencePercentage > limitPct) {
                    status = "EXCEEDED";
                    exceededCount++;
                } else if (absencePercentage >= dangerLimitPct) {
                    status = "DANGER";
                    dangerCount++;
                } else if (absencePercentage >= warningLimitPct) {
                    status = "WARNING";
                    warningCount++;
                }
            }

            summaries.add(new AttendanceDTOs.SubjectAttendanceSummary(
                    subject.getId(),
                    subject.getName(),
                    subject.getColor(),
                    totalClasses,
                    currentAbsences,
                    currentPresences,
                    totalMarked,
                    absencePercentage,
                    limitPct,
                    status
            ));
        }

        return new AttendanceDTOs.GlobalAttendanceSummary(
                subjects.size(),
                warningCount,
                dangerCount,
                exceededCount,
                summaries
        );
    }

    private int calculateOccurrences(LocalDate start, LocalDate end, java.time.DayOfWeek dayOfWeek) {
        if (start == null || end == null || dayOfWeek == null || start.isAfter(end)) {
            return 0;
        }
        int count = 0;
        LocalDate date = start;
        // Caminha até o primeiro dia correspondente
        while (date.getDayOfWeek() != dayOfWeek && !date.isAfter(end)) {
            date = date.plusDays(1);
        }
        // Conta as semanas até ultrapassar a data final
        while (!date.isAfter(end)) {
            count++;
            date = date.plusWeeks(1);
        }
        return count;
    }

    private AttendanceDTOs.Response toResponse(AttendanceRecord record) {
        return new AttendanceDTOs.Response(
                record.getId(),
                record.getRecurringClass().getId(),
                record.getRecurringClass().getTitle(),
                record.getRecurringClass().getSubject() != null ? record.getRecurringClass().getSubject().getId() : null,
                record.getRecurringClass().getSubject() != null ? record.getRecurringClass().getSubject().getName() : null,
                record.getDate(),
                record.isPresent(),
                record.getCreatedAt()
        );
    }
}
