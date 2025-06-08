package com.example.project3.controller;

import com.example.project3.model.Note;
import com.example.project3.model.User;
import com.example.project3.service.NoteService;
import com.example.project3.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NotesController {

    private final NoteService noteService;
    private final UserService userService;

    @GetMapping
    public String listNotes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = getCurrentUser(userDetails);
            List<Note> notes = noteService.getUserNotes(user);

            model.addAttribute("notes", notes);
            model.addAttribute("user", user);

            return "notes/list";
        } catch (Exception e) {
            log.error("Error loading notes: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading notes: " + e.getMessage());
            return "dashboard";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("note", new Note());
        return "notes/form";
    }

    @PostMapping("/new")
    public String createNote(
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(userDetails);
            noteService.createNote(title, content, user);

            redirectAttributes.addFlashAttribute("success", "Заметка успешно создана!");
            return "redirect:/notes";
        } catch (Exception e) {
            log.error("Error creating note: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании заметки: " + e.getMessage());
            return "redirect:/notes";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = getCurrentUser(userDetails);
            Note note = noteService.getNoteById(id, user)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            model.addAttribute("note", note);
            return "notes/form";
        } catch (Exception e) {
            log.error("Error loading note for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading note: " + e.getMessage());
            return "notes/list";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateNote(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(userDetails);
            noteService.updateNote(id, title, content, user);

            redirectAttributes.addFlashAttribute("success", "Заметка успешно обновлена!");
            return "redirect:/notes";
        } catch (Exception e) {
            log.error("Error updating note: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении заметки: " + e.getMessage());
            return "redirect:/notes";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(userDetails);
            noteService.deleteNote(id, user);

            redirectAttributes.addFlashAttribute("success", "Заметка успешно удалена!");
            return "redirect:/notes";
        } catch (Exception e) {
            log.error("Error deleting note: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении заметки: " + e.getMessage());
            return "redirect:/notes";
        }
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
    }
}