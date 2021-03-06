package ZadanieRekrutacyjne.ZadanieRekrutacyjne.Controllers;

import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Entities.Student;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Entities.Teacher;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Services.StudentService;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Services.TeacherService;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.ViewModels.StudentViewModel;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.ViewModels.TeacherForStudentViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
@RequestMapping("students")
public class StudentController {
    private final StudentService studentService;
    private final TeacherService teacherService;

    @GetMapping(value = "")
    public String viewHomePage(
            @RequestParam(value = "page") Optional<Integer> page,
            @RequestParam(value = "pageSize") Optional<Integer> pageSize,
            @RequestParam(value = "column") Optional<String> column,
            @RequestParam(value = "sortAscending") Optional<Boolean> sortAscending,
            Model model) {
        var currentPage = page.orElse(1);
        var currentPageSize = pageSize.orElse(5);
        var currentSortColumn = column.orElse("id");
        var currentDirection = sortAscending.orElse(true);

        var students = studentService.GetPage(currentPage - 1, currentPageSize, currentSortColumn, currentDirection);
        model.addAttribute("students", GetStudentViewModels(students.getContent()));

        int totalPages = students.getTotalPages();
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", currentPageSize);
        model.addAttribute("currentPageNumber", currentPage);

        if (totalPages > 0) {
            var pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "students";
    }

    @GetMapping("newStudentForm")
    public String newStudent(Model model) {
        var newStudent = new StudentViewModel();
        model.addAttribute("student", newStudent);

        return "new_student";
    }

    @GetMapping("getFiltered")
    public String getFiltered(@RequestParam(value = "filterValue") String filterValue, Model model) {
        model.addAttribute("students", GetStudentViewModels(studentService.GetByName(filterValue)));

        return "students";
    }

    @PostMapping("saveStudent")
    public String saveStudent(@ModelAttribute("student") @Valid StudentViewModel student, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            if (student.getId() == null) {
                return "new_student";
            } else {
                return "update_student";
            }
        }

        if (student.getId() == null) {
            studentService.Add(student);
        } else {
            studentService.Update(student);
        }

        return "redirect:/";
    }

    @GetMapping("updateStudentForm/{id}")
    public String updateStudent(@PathVariable(value = "id") Long id, Model model) {
        var student = studentService.Get(id);
        var studentViewModel = GetStudentViewModel(student);
        var studentTeachers = student.getTeachers();
        var teachers = GetTeacherIdsViewModels(teacherService.GetAll(), studentTeachers);

        model.addAttribute("student", studentViewModel);
        model.addAttribute("teachers", teachers);

        return "update_student";
    }

    @GetMapping("delete")
    public ResponseEntity<String> deleteStudent(@RequestParam(value = "id") Long id) {
        studentService.Delete(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("getForTeacher")
    public String getForStudent(@RequestParam(value = "teacherId") Long teacherId, Model model) {
        var teacherStudents = GetStudentViewModels(teacherService.GetForTeacher(teacherId));

        model.addAttribute("students", teacherStudents);

        return "students";
    }

    @GetMapping("addTeacherToStudent")
    public String addTeacherToStudent(@RequestParam(value = "teacherId") Long teacherId, @RequestParam(value = "studentId") Long studentId, Model model) {
        var student = studentService.Get(studentId);
        var teacher = teacherService.Get(teacherId);

        var studentTeachers = student.getTeachers();
        studentTeachers.add(teacher);
        student.setTeachers(studentTeachers);

        studentService.Update(GetStudentViewModel(student));

        var teachers = GetTeacherIdsViewModels(teacherService.GetAll(), student.getTeachers());
        model.addAttribute("teachers", teachers);

        return "student_teachers";
    }

    @GetMapping("removeTeacherOfStudent")
    public String removeTeacherOfStudent(@RequestParam(value = "teacherId") Long teacherId, @RequestParam(value = "studentId") Long studentId, Model model) {
        var student = studentService.Get(studentId);
        var teacher = teacherService.Get(teacherId);

        var studentTeachers = student.getTeachers();
        studentTeachers.remove(teacher);
        student.setTeachers(studentTeachers);

        studentService.Update(GetStudentViewModel(student));

        var teachers = GetTeacherIdsViewModels(teacherService.GetAll(), student.getTeachers());
        model.addAttribute("teachers", teachers);

        return "student_teachers";
    }

    @GetMapping("get/{id}")
    public String getStudent(@PathVariable Long id) {
        GetStudentViewModel(studentService.Get(id));
        return "get_student";
    }


    private List<StudentViewModel> GetStudentViewModels(List<Student> students) {
        var studentViewModels = new ArrayList<StudentViewModel>();

        for (var student : students) {
            studentViewModels.add(GetStudentViewModel(student));
        }

        return studentViewModels;
    }

    private StudentViewModel GetStudentViewModel(Student student) {
        return StudentViewModel.builder()
                .id(student.getId())
                .name(student.getName())
                .lastName(student.getLastName())
                .age(student.getAge())
                .email(student.getEmail())
                .field(student.getField())
                .teachers_ids(student.getTeachers().stream().map(s -> s.getId()).collect(Collectors.toList()))
                .build();
    }

    private List<TeacherForStudentViewModel> GetTeacherIdsViewModels(List<Teacher> teachers, List<Teacher> studentTeachers) {
        var teacherViewModels = new ArrayList<TeacherForStudentViewModel>();

        for (var teacher : teachers) {
            teacherViewModels.add(GetTeacherIdsViewModel(teacher, studentTeachers));
        }

        return teacherViewModels;
    }

    private TeacherForStudentViewModel GetTeacherIdsViewModel(Teacher teacher, List<Teacher> studentTeachers) {
        return TeacherForStudentViewModel.builder()
                .id(teacher.getId())
                .name(teacher.getName() + " " + teacher.getLastName())
                .assignedToCurrentStudent(studentTeachers.contains(teacher))
                .build();
    }
}

