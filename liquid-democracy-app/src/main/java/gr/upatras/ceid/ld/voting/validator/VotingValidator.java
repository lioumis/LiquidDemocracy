package gr.upatras.ceid.ld.voting.validator;

import gr.upatras.ceid.ld.common.enums.VotingType;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.common.exception.VotingCreationException;
import gr.upatras.ceid.ld.common.utils.DateHelper;
import gr.upatras.ceid.ld.delegation.entity.DelegationEntity;
import gr.upatras.ceid.ld.delegation.repository.DelegationRepository;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.user.repository.UserRepository;
import gr.upatras.ceid.ld.voting.dto.VotingCreationDto;
import gr.upatras.ceid.ld.voting.dto.VotingInitializationDto;
import gr.upatras.ceid.ld.voting.dto.VotingOptionDto;
import gr.upatras.ceid.ld.voting.entity.ParticipantEntity;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import gr.upatras.ceid.ld.voting.repository.ParticipantRepository;
import gr.upatras.ceid.ld.voting.repository.VoteRepository;
import gr.upatras.ceid.ld.voting.repository.VotingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class VotingValidator {

    private final ParticipantRepository participantRepository;

    private final VoteRepository voteRepository;

    private final DelegationRepository delegationRepository;

    private final VotingRepository votingRepository;

    private final UserRepository userRepository;

    public VotingValidator(ParticipantRepository participantRepository, VoteRepository voteRepository, DelegationRepository delegationRepository, VotingRepository votingRepository, UserRepository userRepository) {
        this.participantRepository = participantRepository;
        this.voteRepository = voteRepository;
        this.delegationRepository = delegationRepository;
        this.votingRepository = votingRepository;
        this.userRepository = userRepository;
    }

    public void validateVotingDates(VotingEntity voting) throws ValidationException {
        if (voting.getStartDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία δεν έχει ξεκινήσει ακόμα.");
        }

        if (voting.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει.");
        }
    }

    public void validateParticipation(VotingEntity voting, UserEntity voter) throws ValidationException {
        if (!voting.getElectoralCommittee().contains(voter) && !voting.getDelegates().contains(voter)) {
            ParticipantEntity participantEntity = participantRepository.findByUserAndVoting(voter, voting)
                    .orElseThrow(() -> new ValidationException("Δεν υπάρχει αίτηση συμμετοχής στην ψηφοφορία"));

            if (participantEntity.getStatus() == null) {
                throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία δεν έχει εξεταστεί ακόμα");
            }

            if (Boolean.FALSE.equals(participantEntity.getStatus())) {
                throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία έχει απορριφθεί");
            }
        }
    }

    public void validateChoice(List<String> voteChoices, VotingEntity voting) throws ValidationException {
        if (voteChoices == null || voteChoices.isEmpty()) {
            throw new ValidationException("Δεν υπάρχει επιλογή");
        }

        if (voting.getVotingType().equals(VotingType.SINGLE) && voteChoices.size() > 1) {
            throw new ValidationException("Η ψηφοφορία δεν επιτρέπει πολλές επιλογές");
        }

        if (voting.getVotingType().equals(VotingType.MULTIPLE) && voting.getVoteLimit() != null && voteChoices.size() > voting.getVoteLimit()) {
            throw new ValidationException("Έχετε κάνει παραπάνω επιλογές από τις επιτρεπόμενες");
        }
    }

    public void checkIfAlreadyVoted(UserEntity voter, VotingEntity voting) throws ValidationException {
        if (voteRepository.existsByOriginalVoterAndVoting(voter, voting)) {
            throw new ValidationException("Έχετε ήδη ψηφίσει για αυτή την ψηφοφορία");
        }
    }

    public void checkIfDelegationExists(UserEntity voter, VotingEntity voting) throws ValidationException {
        Optional<DelegationEntity> delegationOpt = delegationRepository.findByDelegatorAndVoting(voter, voting);
        if (delegationOpt.isPresent()) {
            throw new ValidationException("Έχετε ήδη αναθέσει την ψήφο σας σε άλλο χρήστη και δεν μπορείτε να ψηφίσετε άμεσα");
        }
    }

    public void validateComment(String comment) throws ValidationException {
        if (comment == null || comment.trim().isEmpty()) {
            throw new ValidationException("Το σχόλιο είναι κενό");
        }

        if (comment.length() > 255) {
            throw new ValidationException("Το σχόλιο είναι υπερβολικά μεγάλο");
        }
    }

    public void validateFeedback(String feedback) throws ValidationException {
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new ValidationException("Η ανατροφοδότηση είναι κενή");
        }

        if (feedback.length() > 255) {
            throw new ValidationException("Η ανατροφοδότηση είναι υπερβολικά μεγάλη");
        }
    }

    public void validateHasExpired(LocalDateTime endDate) throws ValidationException {
        if (endDate == null) {
            throw new ValidationException("Η ημερομηνία λήξης είναι κενή");
        }
        if (endDate.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία είναι ακόμα ενεργή");
        }
    }

    public void validateHasNotExpired(LocalDateTime endDate) throws ValidationException {
        if (endDate == null) {
            return;
        }
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει");
        }
    }

    public void validateIsValid(VotingEntity voting) throws ValidationException {
        if (!voting.isValid()) {
            throw new ValidationException("Η ψηφοφορία έχει ακυρωθεί");
        }
    }

    public void validateVotingInitialization(VotingInitializationDto votingInitializationDto) throws ValidationException {
        if (votingInitializationDto == null) {
            throw new ValidationException("Εσφαλμένα δεδομένα");
        }

        if (votingInitializationDto.name() == null || votingInitializationDto.name().trim().isEmpty()) {
            throw new ValidationException("Ο τίτλος της ψηφοφορίας είναι κενός");
        }

        if (votingInitializationDto.name().length() > 255) {
            throw new ValidationException("Ο τίτλος της ψηφοφορίας είναι υπερβολικά μεγάλος");
        }

        if (votingInitializationDto.committee() == null || votingInitializationDto.committee().isEmpty()) {
            throw new ValidationException("Η εφορευτική επιτροπή είναι κενή");
        }

        if (votingInitializationDto.committee().size() < 3) {
            throw new ValidationException("Η εφορευτική επιτροπή πρέπει να αποτελείται από τουλάχιστον 3 άτομα");
        }
    }

    public void checkIfNameExists(String name) throws ValidationException {
        if (votingRepository.existsByNameIgnoreCase(name)) {
            throw new ValidationException("Υπάρχει ήδη ψηφοφορία με αυτό το όνομα");
        }
    }

    public Set<UserEntity> checkCommittee(List<String> memberList) throws ValidationException, VotingCreationException {
        Set<UserEntity> committee = new HashSet<>();
        Map<Integer, String> errorMetaData = new HashMap<>();

        for (int i = 0; i < memberList.size(); i++) {
            String usernameString = memberList.get(i);
            Optional<UserEntity> memberOptional = userRepository.findByUsername(usernameString);
            if (memberOptional.isEmpty()) {
                errorMetaData.put(i, "Το μέλος δεν βρέθηκε");
            } else {
                if (!committee.add(memberOptional.get())) {
                    throw new ValidationException("Παρακαλώ εισάγετε διαφορετικούς χρήστες");
                }
            }
        }

        if (!errorMetaData.isEmpty()) {
            throw new VotingCreationException("Τουλάχιστον ένα από τα μέλη δεν βρέθηκε", errorMetaData);
        }

        return committee;
    }

    public void checkAuthorizationToEdit(VotingEntity voting, UserEntity user) throws AuthorizationException {
        if (!voting.getElectoralCommittee().contains(user)) {
            throw new AuthorizationException("Δεν ανήκετε στην εφορευτική επιτροπή της ψηφοφορίας");
        }
    }

    public LocalDateTime validateStartDate(String startDateString, LocalDateTime existingStartDate) throws ValidationException {
        LocalDateTime startDate = DateHelper.startDateToLocalDateTime(startDateString);
        if (startDate.isBefore(LocalDate.now().atStartOfDay().plusDays(1))) {
            throw new ValidationException("Η ημερομηνία έναρξης δεν μπορεί να οριστεί στο παρελθόν");
        }
        if (existingStartDate != null && existingStartDate.isAfter(startDate)) {
            throw new ValidationException("Η ημερομηνία έναρξης μπορεί να μετακινηθεί μόνο προς το μέλλον");
        }
        return startDate;
    }

    public LocalDateTime validateEndDate(String endDateString, LocalDateTime existingEndDate, LocalDateTime startDate) throws ValidationException {
        LocalDateTime endDate = DateHelper.endDateToLocalDateTime(endDateString);

        if (endDate.isBefore(LocalDate.now().atTime(23, 59, 59).plusDays(1))) {
            throw new ValidationException("Η ημερομηνία λήξης δεν μπορεί να οριστεί στο παρελθόν ή στην επόμενη μία ημέρα");
        }

        if (startDate != null) {
            if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                throw new ValidationException("Η ημερομηνία λήξης δεν μπορεί να ταυτίζεται με την ημερομηνία έναρξης ή να οριστεί πριν από αυτή");
            }
            if (existingEndDate != null && endDate.isBefore(existingEndDate)) {
                throw new ValidationException("Η ημερομηνία λήξης δεν μπορεί να μεταφερθεί προς το παρελθόν εφόσον η ψηφοφορία έχει ημερομηνία έναρξης");
            }
        }

        return endDate;
    }

    public void validateMandatoryFields(VotingCreationDto votingCreationDto) throws ValidationException {
        if (votingCreationDto.endDate() == null) {
            throw new ValidationException("Η ημερομηνία λήξης είναι κενή");
        }

        if (votingCreationDto.description() == null || votingCreationDto.description().trim().isEmpty()) {
            throw new ValidationException("Οι πληροφορίες είναι κενές");
        }

        if (votingCreationDto.mechanism() == null) {
            throw new ValidationException("Ο τύπος ψηφοφορίας είναι κενός");
        }

        if (votingCreationDto.options() == null || votingCreationDto.options().isEmpty()) {
            throw new ValidationException("Δεν έχουν οριστεί επιλογές");
        }

        if (VotingType.MULTIPLE.equals(VotingType.fromName(votingCreationDto.mechanism())) &&
                votingCreationDto.voteLimit() != null &&
                votingCreationDto.voteLimit() > votingCreationDto.options().size()) {
            throw new ValidationException("Ο μέγιστος αριθμός επιλογών είναι μεγαλύτερος από τον αριθμό επιλογών που έχουν δοθεί");
        }
    }

    public VotingType validateVotingMechanism(String mechanism) throws ValidationException {
        try {
            return VotingType.fromName(mechanism);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Ο τύπος ψηφοφορίας είναι εσφαλμένος");
        }
    }

    public String validateInformation(String information) throws ValidationException {
        if (information != null) {
            if (information.trim().length() > 500) {
                throw new ValidationException("Το μήκος των πληροφοριών είναι υπερβολικά μεγάλο");
            }
            return information.trim();
        }
        return null;
    }

    public void validateVotingOptions(List<VotingOptionDto> options) throws ValidationException {
        for (VotingOptionDto option : options) {
            if (option.title() == null || option.title().trim().isEmpty()) {
                throw new ValidationException("Ο τίτλος μίας επιλογής είναι κενός");
            }
            if (option.title().length() > 255) {
                throw new ValidationException("Ο τίτλος μίας επιλογής είναι υπερβολικά μεγάλος");
            }

            if (option.details() == null || option.details().trim().isEmpty()) {
                throw new ValidationException("Η περιγραφή μίας επιλογής είναι κενή");
            }
            if (option.details().length() > 255) {
                throw new ValidationException("Η περιγραφή μίας επιλογής είναι υπερβολικά μεγάλη");
            }
        }
    }

    public Integer validateOptionsAndLimit(List<VotingOptionDto> options, VotingType type, Long limit) throws ValidationException {
        if (options != null && VotingType.MULTIPLE.equals(type) && limit > options.size()) {
            throw new ValidationException("Ο μέγιστος αριθμός επιλογών είναι μεγαλύτερος από τον αριθμό επιλογών που έχουν δοθεί");
        }

        if (VotingType.SINGLE.equals(type)) {
            return null;
        }
        return limit.intValue();
    }

    public void checkIfRequestExists(UserEntity user, VotingEntity voting) throws ValidationException {
        if (voting.getElectoralCommittee().contains(user)) {
            throw new ValidationException("Έχετε ήδη πρόσβαση στην ψηφοφορία ως μέλος της εφορευτικής επιτροπής");
        }

        if (voting.getDelegates().contains(user)) {
            throw new ValidationException("Έχετε ήδη πρόσβαση στην ψηφοφορία ως αντιπρόσωπος");
        }

        if (participantRepository.existsByUserAndVoting(user, voting)) {
            throw new ValidationException("Υπάρχει ήδη αίτημα συμμετοχής για τη συγκεκριμένη ψηφοφορία");
        }
    }

    public void validateVotingDatesForRequest(LocalDateTime start, LocalDateTime end) throws ValidationException {
        if (start == null) {
            throw new ValidationException("Η καταχώρηση αιτήματος συμμετοχής δεν είναι δυνατή ακόμα");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η καταχώρηση αιτήματος συμμετοχής δεν είναι πλέον δυνατή");
        }

        if (end.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει");
        }
    }

    public void validateVotingDatesForGettingRequests(LocalDateTime start, LocalDateTime end) throws ValidationException {
        if (start == null) {
            throw new ValidationException("Η ψηφοφορία δεν έχει ημερομηνία έναρξης");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει ήδη ξεκινήσει");
        }

        if (end != null && end.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει");
        }
    }

}
