package com.studify.service;

import com.studify.dto.gradegoal.GradeGoalDTOs;
import com.studify.entity.GradeGoal;
import com.studify.entity.GradeRecord;
import com.studify.entity.Subject;
import com.studify.entity.User;
import com.studify.exception.BusinessException;
import com.studify.exception.ResourceNotFoundException;
import com.studify.repository.GradeGoalRepository;
import com.studify.repository.GradeRecordRepository;
import com.studify.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeGoalService {

    private final GradeGoalRepository gradeGoalRepository;
    private final GradeRecordRepository gradeRecordRepository;
    private final SubjectRepository subjectRepository;

    // ─── GradeGoal CRUD ───────────────────────────────────────────────────────

    @Transactional
    public GradeGoalDTOs.GoalResponse create(GradeGoalDTOs.CreateRequest request, User user) {
        Subject subject = findSubjectOrThrow(request.subjectId(), user.getId());

        GradeGoal goal = GradeGoal.builder()
                .title(request.title())
                .subject(subject)
                .user(user)
                .targetGrade(request.targetGrade())
                .totalWeight(request.totalWeight())
                .build();

        return toGoalResponse(gradeGoalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public Page<GradeGoalDTOs.GoalResponse> findAll(User user, Pageable pageable) {
        return gradeGoalRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable)
                .map(this::toGoalResponse);
    }

    @Transactional(readOnly = true)
    public GradeGoalDTOs.GoalResponse findById(Long id, User user) {
        return toGoalResponse(findGoalOrThrow(id, user.getId()));
    }

    @Transactional
    public GradeGoalDTOs.GoalResponse update(Long id, GradeGoalDTOs.UpdateRequest request, User user) {
        GradeGoal goal = findGoalOrThrow(id, user.getId());

        if (request.title() != null) goal.setTitle(request.title());
        if (request.targetGrade() != null) goal.setTargetGrade(request.targetGrade());
        if (request.totalWeight() != null) {
            // Validar que o novo totalWeight não seja menor que o peso já registrado
            double weightRecorded = sumWeightRecorded(id, user.getId());
            if (request.totalWeight() < weightRecorded) {
                throw new BusinessException(
                        String.format("O novo peso total (%.2f) não pode ser menor que o peso já registrado (%.2f).",
                                request.totalWeight(), weightRecorded));
            }
            goal.setTotalWeight(request.totalWeight());
        }

        return toGoalResponse(gradeGoalRepository.save(goal));
    }

    @Transactional
    public void delete(Long id, User user) {
        GradeGoal goal = findGoalOrThrow(id, user.getId());
        goal.setDeletedAt(LocalDateTime.now());
        gradeGoalRepository.save(goal);
    }

    // ─── GradeRecord CRUD ─────────────────────────────────────────────────────

    @Transactional
    public GradeGoalDTOs.RecordResponse addRecord(Long goalId,
                                                   GradeGoalDTOs.CreateRecordRequest request,
                                                   User user) {
        GradeGoal goal = findGoalOrThrow(goalId, user.getId());

        double weightRecorded = sumWeightRecorded(goalId, user.getId());
        if (weightRecorded + request.weight() > goal.getTotalWeight()) {
            throw new BusinessException(
                    String.format("A soma dos pesos (%.2f + %.2f = %.2f) ultrapassa o peso total definido (%.2f).",
                            weightRecorded, request.weight(), weightRecorded + request.weight(),
                            goal.getTotalWeight()));
        }

        GradeRecord record = GradeRecord.builder()
                .gradeGoal(goal)
                .user(user)
                .title(request.title())
                .grade(request.grade())
                .weight(request.weight())
                .build();

        return toRecordResponse(gradeRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public Page<GradeGoalDTOs.RecordResponse> findRecords(Long goalId, User user, Pageable pageable) {
        findGoalOrThrow(goalId, user.getId()); // valida ownership
        return gradeRecordRepository.findByGoalIdAndUserId(goalId, user.getId(), pageable)
                .map(this::toRecordResponse);
    }

    @Transactional
    public GradeGoalDTOs.RecordResponse updateRecord(Long goalId, Long recordId,
                                                      GradeGoalDTOs.UpdateRecordRequest request,
                                                      User user) {
        findGoalOrThrow(goalId, user.getId()); // valida ownership da meta
        GradeRecord record = findRecordOrThrow(recordId, user.getId());

        if (request.weight() != null) {
            // Peso atual menos o peso antigo + novo peso não pode ultrapassar totalWeight
            GradeGoal goal = record.getGradeGoal();
            double weightRecorded = sumWeightRecorded(goalId, user.getId());
            double weightWithoutThis = weightRecorded - record.getWeight();
            if (weightWithoutThis + request.weight() > goal.getTotalWeight()) {
                throw new BusinessException(
                        String.format("O novo peso total registrado (%.2f) ultrapassa o peso total definido (%.2f).",
                                weightWithoutThis + request.weight(), goal.getTotalWeight()));
            }
            record.setWeight(request.weight());
        }
        if (request.title() != null) record.setTitle(request.title());
        if (request.grade() != null) record.setGrade(request.grade());

        return toRecordResponse(gradeRecordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(Long goalId, Long recordId, User user) {
        findGoalOrThrow(goalId, user.getId()); // valida ownership
        GradeRecord record = findRecordOrThrow(recordId, user.getId());
        record.setDeletedAt(LocalDateTime.now());
        gradeRecordRepository.save(record);
    }

    // ─── Summary ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GradeGoalDTOs.SummaryResponse getSummary(Long goalId, User user) {
        GradeGoal goal = findGoalOrThrow(goalId, user.getId());
        List<GradeRecord> records = gradeRecordRepository.findAllActiveByGoalIdAndUserId(goalId, user.getId());

        double weightedSum = records.stream()
                .mapToDouble(r -> r.getGrade() * r.getWeight())
                .sum();
        double weightRecorded = records.stream()
                .mapToDouble(GradeRecord::getWeight)
                .sum();
        double weightRemaining = goal.getTotalWeight() - weightRecorded;

        double currentAverage = weightRecorded > 0 ? weightedSum / weightRecorded : 0.0;

        Double neededGrade = null;
        String status;

        if (weightRemaining <= 0.0) {
            // Todas as avaliações lançadas
            status = currentAverage >= goal.getTargetGrade() ? "ACHIEVED" : "FAILED";
        } else {
            neededGrade = (goal.getTargetGrade() * goal.getTotalWeight() - weightedSum) / weightRemaining;
            if (neededGrade > 10.0) {
                status = "FAILED";
            } else if (neededGrade <= goal.getTargetGrade()) {
                status = "ON_TRACK";
            } else {
                status = "AT_RISK";
            }
        }

        List<GradeGoalDTOs.RecordResponse> recordResponses = records.stream()
                .map(this::toRecordResponse)
                .toList();

        return new GradeGoalDTOs.SummaryResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getSubject().getId(),
                goal.getSubject().getName(),
                goal.getTargetGrade(),
                goal.getTotalWeight(),
                weightRecorded,
                weightRemaining,
                round2(currentAverage),
                neededGrade != null ? round2(neededGrade) : null,
                status,
                recordResponses
        );
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private double sumWeightRecorded(Long goalId, Long userId) {
        return gradeRecordRepository.findAllActiveByGoalIdAndUserId(goalId, userId)
                .stream()
                .mapToDouble(GradeRecord::getWeight)
                .sum();
    }

    private GradeGoal findGoalOrThrow(Long id, Long userId) {
        return gradeGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta de nota não encontrada: " + id));
    }

    private GradeRecord findRecordOrThrow(Long id, Long userId) {
        return gradeRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de nota não encontrado: " + id));
    }

    private Subject findSubjectOrThrow(Long subjectId, Long userId) {
        return subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Matéria não encontrada: " + subjectId));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private GradeGoalDTOs.GoalResponse toGoalResponse(GradeGoal goal) {
        return new GradeGoalDTOs.GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getSubject().getId(),
                goal.getSubject().getName(),
                goal.getSubject().getColor(),
                goal.getTargetGrade(),
                goal.getTotalWeight(),
                goal.getCreatedAt()
        );
    }

    private GradeGoalDTOs.RecordResponse toRecordResponse(GradeRecord record) {
        return new GradeGoalDTOs.RecordResponse(
                record.getId(),
                record.getTitle(),
                record.getGrade(),
                record.getWeight(),
                record.getCreatedAt()
        );
    }
}
