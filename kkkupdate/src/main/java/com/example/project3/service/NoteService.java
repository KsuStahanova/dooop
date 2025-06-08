package com.example.project3.service;

import com.example.project3.model.Note;
import com.example.project3.model.User;
import com.example.project3.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    @Transactional(readOnly = true)
    public List<Note> getUserNotes(User user) {
        log.debug("Getting notes for user: {}", user.getEmail());
        return noteRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Optional<Note> getNoteById(Long id, User user) {
        log.debug("Getting note by id: {} for user: {}", id, user.getEmail());
        return noteRepository.findById(id)
                .filter(note -> note.getUser().getId().equals(user.getId()));
    }

    @Transactional
    public Note createNote(String title, String content, User user) {
        log.info("Creating note for user: {}", user.getEmail());
        Note note = new Note(title, content, user);
        Note savedNote = noteRepository.save(note);
        log.info("Note created with id: {}", savedNote.getId());
        return savedNote;
    }

    @Transactional
    public Note updateNote(Long id, String title, String content, User user) {
        log.info("Updating note with id: {} for user: {}", id, user.getEmail());

        Note note = noteRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Note not found or access denied"));

        note.setTitle(title);
        note.setContent(content);

        Note updatedNote = noteRepository.save(note);
        log.info("Note updated successfully");
        return updatedNote;
    }

    @Transactional
    public void deleteNote(Long id, User user) {
        log.info("Deleting note with id: {} for user: {}", id, user.getEmail());

        Note note = noteRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Note not found or access denied"));

        noteRepository.delete(note);
        log.info("Note deleted successfully");
    }

    @Transactional(readOnly = true)
    public long getUserNotesCount(User user) {
        return noteRepository.countByUser(user);
    }
}