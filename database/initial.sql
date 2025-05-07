DROP DATABASE IF EXISTS liquidDemocracy;

create database liquidDemocracy;

use liquidDemocracy;

create table user
(
    id                   bigint auto_increment primary key,
    email                varchar(255) not null,
    name                 varchar(255) not null,
    password_hash        varchar(255) not null,
    security_answer_hash varchar(255) not null,
    security_question    varchar(255) not null,
    surname              varchar(255) not null,
    username             varchar(255) not null,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    constraint UKob8kqyqqgmefl0aco34akdtpe
        unique (email),
    constraint UKsb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);

create table topic
(
    id         bigint auto_increment primary key,
    title      varchar(255) null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table user_roles
(
    role_id    int    null,
    user_id    bigint not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FK55itppkw3i07do3h7qoclqd4k
        foreign key (user_id) references user (id)
);

create table voting
(
    valid       bit          not null default true,
    end_date    datetime     null,
    start_date  datetime     null,
    vote_limit  int          null,
    voting_type int          null,
    id          bigint auto_increment primary key,
    topic_id    bigint       not null,
    information varchar(500) null,
    name        varchar(255) not null,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FKh2nr2qn6xoihm5jrsx1h4bpn8
        foreign key (topic_id) references topic (id)
);

create table delegate
(
    user_id    bigint not null,
    voting_id  bigint not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    primary key (user_id, voting_id),
    constraint FK4skpwjlnsiit7t9vp5u8hkxkm
        foreign key (voting_id) references voting (id),
    constraint FKp6gramowkaekmoc6f3qvmuj1r
        foreign key (user_id) references user (id)
);

create table delegation
(
    delegate_id  bigint null,
    delegator_id bigint null,
    id           bigint auto_increment primary key,
    voting_id    bigint null,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FK7dw6pixqsnw0j5ve3wpkjqwip
        foreign key (voting_id) references voting (id),
    constraint FKik7qx4v02j0talcitadbyj3ct
        foreign key (delegate_id) references user (id),
    constraint FKtgl1tfoexboay41dgt4xbu1i4
        foreign key (delegator_id) references user (id)
);

create table voting_option
(
    id          bigint auto_increment primary key,
    voting_id   bigint       null,
    description varchar(255) null,
    name        varchar(255) not null,
    constraint FK2cx9g9vegaeiosakpqyekres7
        foreign key (voting_id) references voting (id)
);

create table vote
(
    id                bigint auto_increment primary key,
    voter_id          bigint null,
    voting_id         bigint null,
    original_voter_id bigint null,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FK6luh53gc8pvd17nsmegcton3
        foreign key (original_voter_id) references user (id),
    constraint FK81w29sp6dc6dwe9wpynsst9nf
        foreign key (voting_id) references voting (id),
    constraint FKhtn2def3h3mw3ft6mtjtxatub
        foreign key (voter_id) references user (id)
);

create table vote_details
(
    vote_rank        int    null,
    id               bigint auto_increment primary key,
    vote_id          bigint null,
    voting_option_id bigint not null,
    constraint FKnk9oc153g92rgrxbv5srgty3u
        foreign key (vote_id) references vote (id),
    constraint FKsvi89dukj85ettv8foshk7t6j
        foreign key (voting_option_id) references voting_option (id)
);

create table feedback
(
    id        bigint auto_increment primary key,
    user_id   bigint       null,
    voting_id bigint       null,
    content   varchar(255) not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FK1ag30qr7m3hh5em751f04ntws
        foreign key (voting_id) references voting (id),
    constraint FK7k33yw505d347mw3avr93akao
        foreign key (user_id) references user (id)
);

create table message
(
    id         bigint auto_increment primary key,
    user_id    bigint       null,
    voting_id  bigint       null,
    content    varchar(255) not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FK8cjr8kvorpkpkn6ln62hq9l2s
        foreign key (voting_id) references voting (id),
    constraint FKb3y6etti1cfougkdr0qiiemgv
        foreign key (user_id) references user (id)
);

create table message_details
(
    liked      bit    not null,
    id         bigint auto_increment primary key,
    message_id bigint not null,
    user_id    bigint not null,
    constraint FKb49rk2t47spg295q3p6tv7drg
        foreign key (message_id) references message (id),
    constraint FKxs4ly4grxksc0jjcxplghqqx
        foreign key (user_id) references user (id)
);

create table audit_log
(
    action    int           null,
    id        bigint auto_increment primary key,
    user_id   bigint        null,
    details   varchar(1000) null,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    constraint FKqir5ob5q1x1w30jk4j3e4p58c
        foreign key (user_id) references user (id)
);

create table voting_committee
(
    user_id   bigint not null,
    voting_id bigint not null,
    primary key (user_id, voting_id),
    constraint FK6pppdd2xpj26qj6lj91ekqpm8
        foreign key (user_id) references user (id),
    constraint FKe55ruk0ms95r7gx9cn73af55s
        foreign key (voting_id) references voting (id)
);

create table participant
(
    status    bit    null,
    id        bigint auto_increment primary key,
    user_id   bigint null,
    voting_id bigint null,
    constraint FK4svhsrblkwahlhmee8nml5a50
        foreign key (voting_id) references voting (id),
    constraint FKj2ywtc5meno2ouhf5pcq9rsbh
        foreign key (user_id) references user (id)
);

# Test data

INSERT INTO topic (title)
VALUES ('Εκλογές και Δημοκρατία'),
       ('Κλιματική Αλλαγή'),
       ('Τεχνολογία και Εργασία'),
       ('Βιωσιμότητα και Περιβάλλον'),
       ('Ανθρώπινα Δικαιώματα και Ελευθερίες'),
       ('Εκπαίδευση και Τεχνολογία'),
       ('Οικονομία και Ανισότητα'),
       ('Υγεία και Ευημερία'),
       ('Προστασία Ζώων και Φύσης'),
       ('Κοινωνική Αλληλεγγύη');

#Password = 12345
INSERT INTO user (email, name, password_hash, security_answer_hash, security_question, surname, username) VALUES
('giannis@example.com', 'Γιάννης', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'abc123hashedanswer', 'Ποιο είναι το όνομα του πρώτου σκύλου σας;', 'Παπαδόπουλος', 'giannis123'),
('maria@example.com', 'Μαρία', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'def456hashedanswer', 'Ποιο είναι το αγαπημένο σας χρώμα;', 'Κωνσταντίνου', 'maria456'),
('nikos@example.com', 'Νίκος', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'ghi789hashedanswer', 'Σε ποια πόλη γεννηθήκατε;', 'Ιωάννου', 'nikos789'),
('elena@example.com', 'Έλενα', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'jkl012hashedanswer', 'Ποιο είναι το όνομα του πρώτου σας δασκάλου;', 'Παπαδοπούλου', 'elena012'),
('kostas@example.com', 'Κώστας', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'mno345hashedanswer', 'Ποιο είναι το αγαπημένο σας ζώο;', 'Σταυρόπουλος', 'kostas345'),
('anna@example.com', 'Άννα', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'pqr678hashedanswer', 'Ποιο είναι το όνομα του αγαπημένου σας βιβλίου;', 'Παπαδοπούλου', 'anna678'),
('dimitris@example.com', 'Δημήτρης', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'stu901hashedanswer', 'Ποια είναι η αγαπημένη σας ταινία;', 'Μιχαηλίδης', 'dimitris901'),
('sofia@example.com', 'Σοφία', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'vwx234hashedanswer', 'Ποιο είναι το αγαπημένο σας φαγητό;', 'Κωνσταντίνου', 'sofia234'),
('george@example.com', 'Γιώργος', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'yza567hashedanswer', 'Ποια είναι η αγαπημένη σας πόλη;', 'Παπαδάκης', 'george567'),
('evgenia@example.com', 'Ευγενία', '$2a$10$PWHtwaK49GG5Q9qJYLS0sugLWUn8FfGm9mJKtOFs2CDyauYD.YVVC', 'bcd890hashedanswer', 'Ποιο είναι το αγαπημένο σας χόμπι;', 'Σταυροπούλου', 'evgenia890');

-- Ρόλος 1 για όλους τους χρήστες
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(8, 1),
(9, 1),
(10, 1);

-- Ρόλος 2 για τους πρώτους 8 χρήστες
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2),
(2, 2),
(3, 2),
(4, 2),
(5, 2),
(6, 2),
(7, 2),
(8, 2);

-- Ρόλος 3 για τους χρήστες 1, 2, 3, 8, 9, 10
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 3),
(2, 3),
(3, 3),
(8, 3),
(9, 3),
(10, 3);

-- Ρόλος 4 για τους χρήστες 8, 10
INSERT INTO user_roles (user_id, role_id) VALUES
(8, 4),
(10, 4);

INSERT INTO voting (voting_type, vote_limit, start_date, end_date, topic_id, information, name)
VALUES (1,NULL,DATE_SUB(CURDATE(), INTERVAL 7 DAY),DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 12 DAY), INTERVAL 23 HOUR)
    + INTERVAL 59 MINUTE + INTERVAL 59 SECOND,1,'Ψηφοφορία για τη βελτίωση των δημοκρατικών θεσμών και την ενίσχυση της συμμετοχής των πολιτών στις διαδικασίες λήψης αποφάσεων.','Ενίσχυση της Δημοκρατίας'),
(1, NULL, DATE_SUB(CURDATE(),  INTERVAL 3 DAY), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 14 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 7, 'Ψηφοφορία για την υιοθέτηση οικονομικών μεταρρυθμίσεων που στοχεύουν στη βελτίωση της φορολογίας και των δημόσιων δαπανών.', 'Οικονομική Μεταρρύθμιση'),
(2, 2, CURDATE(), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 17 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 4, 'Ψηφοφορία για τη συμμετοχή των πολιτών στη διαχείριση φυσικών πόρων, όπως δάση και υδάτινοι πόροι, με γνώμονα τη βιωσιμότητα. Επιλέξτε τις καλύτερες κατά την άποψη σας επιλογές που θα συμβάλλουν περισσότερο στην προστασία του περιβάλλοντος', 'Διαχείριση Φυσικών Πόρων'),
(1, NULL, DATE_ADD(CURDATE(),  INTERVAL 1 DAY), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 22 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 2, 'Ψηφοφορία για την εισαγωγή βιώσιμων ενεργειακών πολιτικών που στοχεύουν στη μείωση της εξάρτησης από μη ανανεώσιμες πηγές.', 'Βιώσιμη Ενέργεια'),
(1, NULL, DATE_ADD(CURDATE(),  INTERVAL 2 DAY), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 24 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 6, 'Ψηφοφορία για αλλαγές στο εκπαιδευτικό σύστημα που περιλαμβάνουν αναθεώρηση της ύλης και ενίσχυση των δεξιοτήτων των μαθητών.', 'Μεταρρύθμιση στην Εκπαίδευση'),
(1, NULL, DATE_ADD(CURDATE(),  INTERVAL 4 DAY), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 28 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 5, 'Ψηφοφορία για την αναθεώρηση της νομοθεσίας με στόχο την προώθηση της ισότητας και της δίκαιης μεταχείρισης όλων των πολιτών.', 'Ισότητα στη Νομοθεσία'),
(1, NULL, DATE_ADD(CURDATE(),  INTERVAL 5 DAY), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 33 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 7, 'Ψηφοφορία για την παροχή κινήτρων προς νέες επιχειρήσεις,  ενίσχυση της καινοτομίας και μείωση της ανεργίας.', 'Στήριξη Επιχειρηματικότητας'),
(1, NULL, DATE_SUB(CURDATE(), INTERVAL 38 DAY), DATE_ADD(DATE_SUB(CURDATE(),  INTERVAL 24 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 4, 'Ψηφοφορία για μέτρα διατήρησης των υδάτινων πόρων,  εστιάζοντας στη μείωση της ρύπανσης και στη σωστή διαχείριση.', 'Διατήρηση Υδάτινων Πόρων'),
(1, NULL, DATE_SUB(CURDATE(),  INTERVAL 59 DAY), DATE_ADD(DATE_SUB(CURDATE(),  INTERVAL 49 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 2, 'Ψηφοφορία για την αξιοποίηση ανανεώσιμων πηγών ενέργειας, όπως ηλιακή και αιολική,  για τη μείωση του αποτυπώματος άνθρακα.', 'Ανανέωση Ενεργειακών Πόρων'),
(1, NULL, DATE_SUB(CURDATE(),  INTERVAL 99 DAY), DATE_ADD(DATE_SUB(CURDATE(),  INTERVAL 90 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 6, 'Ψηφοφορία για τη βελτίωση του δημόσιου σχολικού συστήματος, περιλαμβάνοντας υποδομές και εκπαιδευτικά προγράμματα.', 'Δημόσια Εκπαίδευση'),
(2, 4, DATE_ADD(CURDATE(),  INTERVAL 266 DAY), DATE_ADD(DATE_ADD(CURDATE(),  INTERVAL 275 DAY), INTERVAL 23 HOUR)
       + INTERVAL 59 MINUTE + INTERVAL 59 SECOND, 3, 'Ψηφοφορία για τη βελτίωση του δημόσιου σχολικού συστήματος, περιλαμβάνοντας υποδομές και εκπαιδευτικά προγράμματα.', 'Δοκιμαστική με πολλαπλή επιλογή');

INSERT INTO delegate (user_id, voting_id)
VALUES (1, 1),
(2, 1),
(3, 2),
(4, 2),
(5, 3),
(6, 3),
(7, 4),
(2, 4),
(4, 5),
(6, 10);

INSERT INTO delegation (delegate_id, delegator_id, voting_id) VALUES
(1, 8, 1),
(2, 9, 1),
(3, 10, 2),
(4, 5, 2),
(5, 6, 3),
(6, 7, 3),
(7, 1, 4),
(2, 3, 4),
(4, 10, 5),
(6, 1, 10),
(6, 2, 10),
(6, 3, 10);

INSERT INTO participant (status, user_id, voting_id) VALUES
(true, 9, 1),
(true, 10, 2),
(true, 5, 2),
(true, 6, 3),
(true, 7, 3),
(true, 1, 4),
(true, 10, 5),
(true, 4, 2),
(true, 2, 4),
(true, 4, 5),
(true, 4, 5),
(true, 1, 10),
(true, 2, 10),
(true, 5, 10),
(true, 6, 10);

INSERT INTO voting_committee (user_id, voting_id) VALUES
(1, 1),
(2, 1),
(8, 1),
(3, 2),
(6, 2),
(8, 2),
(5, 3),
(4, 3),
(8, 3),
(7, 4),
(3, 4),
(8, 4),
(3, 5),
(2, 5),
(8, 5),
(1, 6),
(2, 6),
(3, 6),
(6, 7),
(3, 7),
(8, 7),
(3, 8),
(1, 8),
(4, 8),
(6, 9),
(4, 9),
(3, 9),
(3, 10),
(8, 10);

INSERT INTO voting_option (voting_id, description, name) VALUES
(1, 'Ενίσχυση των δικαιωμάτων των πολιτών μέσω ηλεκτρονικής διακυβέρνησης.', 'Ηλεκτρονική Διακυβέρνηση'),
(1, 'Αύξηση της διαφάνειας στις κυβερνητικές αποφάσεις.', 'Διαφάνεια'),
(1, 'Καθιέρωση νέων τρόπων συμμετοχής των πολιτών στις δημοκρατικές διαδικασίες.', 'Συμμετοχή των Πολιτών'),
(2, 'Μείωση φόρων για μικρές επιχειρήσεις.', 'Φορολογικές Ελαφρύνσεις'),
(2, 'Ανακατεύθυνση δημόσιων δαπανών προς την παιδεία και την υγεία.', 'Προτεραιότητα σε Κοινωνικές Παροχές'),
(2, 'Καταπολέμηση της φοροδιαφυγής μέσω αυστηρότερων ελέγχων.', 'Καταπολέμηση Φοροδιαφυγής'),
(3, 'Συμμετοχή τοπικών κοινοτήτων στη διαχείριση δασών.', 'Συμμετοχή Τοπικών Κοινοτήτων'),
(3, 'Δημιουργία εθνικού σχεδίου για τη βιώσιμη διαχείριση των υδάτων.', 'Εθνικό Σχέδιο Διαχείρισης Υδάτων'),
(3, 'Αύξηση των προσπαθειών για προστασία απειλούμενων περιοχών.', 'Προστασία Απειλούμενων Περιοχών'),
(4, 'Προώθηση της ηλιακής ενέργειας.', 'Ηλιακή Ενέργεια'),
(4, 'Αξιοποίηση της αιολικής ενέργειας σε αγροτικές περιοχές.', 'Αιολική Ενέργεια'),
(4, 'Παροχή επιδοτήσεων για εγκαταστάσεις πράσινης ενέργειας.', 'Επιδοτήσεις Πράσινης Ενέργειας'),
(5, 'Αναθεώρηση της ύλης μαθημάτων για περισσότερες δεξιότητες.', 'Αναθεώρηση Ύλης'),
(5, 'Εκσυγχρονισμός σχολικών υποδομών.', 'Εκσυγχρονισμός Υποδομών'),
(5, 'Δημιουργία προγραμμάτων ενίσχυσης επαγγελματικού προσανατολισμού.', 'Επαγγελματικός Προσανατολισμός'),
(6, 'Αναθεώρηση νόμων για την ισότητα φύλων.', 'Ισότητα Φύλων'),
(6, 'Κατάργηση διακρίσεων στην εργασία και πρόσληψη.', 'Κατάργηση Διακρίσεων'),
(6, 'Δημιουργία εθνικής στρατηγικής για ίσες ευκαιρίες.', 'Εθνική Στρατηγική'),
(7, 'Μείωση γραφειοκρατίας για νέες επιχειρήσεις.', 'Μείωση Γραφειοκρατίας'),
(7, 'Παροχή φορολογικών κινήτρων για startups.', 'Φορολογικά Κίνητρα'),
(7, 'Στήριξη καινοτομίας μέσω ερευνητικών προγραμμάτων.', 'Στήριξη Καινοτομίας'),
(8, 'Κατασκευή μονάδων επεξεργασίας υδάτων.', 'Επεξεργασία Υδάτων'),
(8, 'Ανάπτυξη προγραμμάτων ευαισθητοποίησης για την προστασία υδάτων.', 'Ευαισθητοποίηση'),
(9, 'Ενίσχυση υποδομών για αποθήκευση ηλιακής ενέργειας.', 'Αποθήκευση Ενέργειας'),
(9, 'Ανάπτυξη μικρών ανεμογεννητριών για οικιακή χρήση.', 'Μικρές Ανεμογεννήτριες'),
(10, 'Κατασκευή νέων σχολείων σε υποβαθμισμένες περιοχές.', 'Νέα Σχολεία'),
(10, 'Παροχή δωρεάν ψηφιακών εργαλείων για μαθητές.', 'Ψηφιακά Εργαλεία'),
(11, 'Στήριξη καινοτομίας μέσω ερευνητικών προγραμμάτων.', 'Στήριξη Καινοτομίας'),
(11, 'Δημιουργία προγραμμάτων ενίσχυσης επαγγελματικού προσανατολισμού.', 'Επαγγελματικός Προσανατολισμός'),
(11, 'Αύξηση των προσπαθειών για προστασία απειλούμενων περιοχών.', 'Προστασία Απειλούμενων Περιοχών'),
(11, 'Ανακατεύθυνση δημόσιων δαπανών προς την παιδεία και την υγεία.', 'Προτεραιότητα σε Κοινωνικές Παροχές'),
(11, 'Δημιουργία εθνικού σχεδίου για τη βιώσιμη διαχείριση των υδάτων.', 'Εθνικό Σχέδιο Διαχείρισης Υδάτων');

INSERT INTO vote (voter_id, voting_id, original_voter_id) VALUES
(null, 10, 8),
(null, 10, 6),
(6, 10, 1),
(6, 10, 2),
(6, 10, 3),
(null, 10, 5);

INSERT INTO vote_details (vote_rank, vote_id, voting_option_id) VALUES
(1, 1, 26),
(1, 2, 27),
(1, 3, 27),
(1, 4, 27),
(1, 5, 27),
(1, 6, 27);

INSERT INTO feedback (user_id, voting_id, content) VALUES
(10, 8, 'Η διαδικασία ψηφοφορίας πρέπει να είναι πιο διαφανής, με σαφέστερη παρουσίαση των αποτελεσμάτων.'),
(8, 9, 'Θα ήταν χρήσιμο να προστίθενται περισσότερες εξηγήσεις για τις επιλογές.'),
(6, 10, 'Η διαδικασία θα μπορούσε να είναι πιο άμεση.'),
(5, 10, 'Ίσως να ήταν χρήσιμο το να υπάρχουν περισσότερες επιλογές στις ψηφοφορίες.');

INSERT INTO message (user_id, voting_id, content) VALUES
(2, 1, 'Πιστεύω ότι η ψήφος μας πρέπει να ενισχύει τη διαφάνεια στη διακυβέρνηση.'),
(4, 1, 'Πώς θα διασφαλίσουμε ότι οι πολίτες έχουν ενεργό ρόλο στη διαδικασία λήψης αποφάσεων;'),
(3, 2, 'Οι μεταρρυθμίσεις πρέπει να είναι δίκαιες και να ωφελούν όλους, όχι μόνο λίγους.'),
(5, 2, 'Είναι σημαντικό να μειώσουμε τη γραφειοκρατία που επηρεάζει τις δημόσιες δαπάνες.'),
(6, 3, 'Η διατήρηση των φυσικών μας πόρων είναι κρίσιμη για τις επόμενες γενιές.'),
(7, 3, 'Θα μπορούσαμε να εντάξουμε τους πολίτες στη διαχείριση με τοπικές ομάδες συνεργασίας.'),
(8, 4, 'Η μετάβαση στις ανανεώσιμες πηγές ενέργειας είναι η μόνη βιώσιμη λύση για το μέλλον.'),
(9, 4, 'Θα πρέπει να δοθούν κίνητρα σε επιχειρήσεις για επενδύσεις σε πράσινη ενέργεια.'),
(10, 5, 'Η αναθεώρηση της ύλης πρέπει να περιλαμβάνει περισσότερη έμφαση στις δεξιότητες ζωής.'),
(2, 5, 'Πώς μπορούμε να εξασφαλίσουμε ίσες ευκαιρίες εκπαίδευσης για όλους;'),
(3, 6, 'Η αναθεώρηση των νόμων είναι απαραίτητη για τη μείωση των κοινωνικών ανισοτήτων.'),
(5, 6, 'Πρέπει να διασφαλίσουμε ότι κάθε πολίτης αντιμετωπίζεται δίκαια.'),
(6, 7, 'Οι νεοφυείς επιχειρήσεις χρειάζονται χρηματοδότηση και πρόσβαση σε δίκτυα καινοτομίας.'),
(8, 7, 'Η μείωση της ανεργίας περνάει μέσα από την ενίσχυση της τοπικής επιχειρηματικότητας.');

INSERT INTO message_details (liked, message_id, user_id) VALUES
(1, 1, 3),
(1, 2, 5),
(0, 3, 4),
(1, 4, 6),
(1, 5, 9),
(1, 6, 10),
(0, 7, 2),
(1, 8, 3),
(1, 9, 4),
(0, 10, 5),
(0, 11, 6),
(1, 12, 7),
(0, 13, 8),
(1, 14, 9);
